package com.dmytrobilokha.zghadai;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

@WebServlet(urlPatterns = {
    "*.jpg", "*.jpeg", "*.png", "*.gif", "*.mpo", "*.3gp", "*.avi", "*.mov", "*.mp4", "*.mpg", "*.mts", "*.webm",
    "*.JPG", "*.JPEG", "*.PNG", "*.GIF", "*.MPO", "*.3GP", "*.AVI", "*.MOV", "*.MP4", "*.MPG", "*.MTS", "*.WEBM"})
public class MediaFileServlet extends HttpServlet {

    private static final String RANGE_HEADER_START = "bytes=";
    private static final Logger logger = LoggerFactory.getLogger(MediaFileServlet.class);

    private final FilesystemService filesystemService;

    @Inject
    public MediaFileServlet(FilesystemService filesystemService) {
        this.filesystemService = filesystemService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        handle(req, resp, true);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        handle(req, resp, false);
    }

    private void handle(HttpServletRequest req,
                        HttpServletResponse resp,
                        boolean sendBody) throws IOException {
        Path path = ServletUtil.getRequestedFilesystemPath(req);
        Path fileNamePath = path.getFileName();
        try {
            if (fileNamePath == null || !filesystemService.isFileAvailable(path)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (IOException e) {
            logger.warn("Failed to check availability of a file '{}'", path, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        long size = filesystemService.getFileSize(path);
        long lastModified = filesystemService.getLastModifiedTime(path).toMillis();
        String etag = ServletUtil.generateFileEtag(path, lastModified, size);
        // Set caching headers
        resp.setHeader("ETag", etag);
        resp.setDateHeader("Last-Modified", lastModified);
        // Conditional GET
        if (etag.equals(req.getHeader("If-None-Match"))
                || req.getDateHeader("If-Modified-Since") >= lastModified) {
            resp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        String contentType = getServletContext().getMimeType(fileNamePath.toString());
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        resp.setContentType(contentType);
        resp.setHeader("Accept-Ranges", "bytes");
        String range = req.getHeader("Range");
        if (range != null && range.startsWith(RANGE_HEADER_START)) {
            handleRange(range, path, resp, size, sendBody);
            return;
        }
        resp.setContentLengthLong(size);
        if (sendBody) {
            filesystemService.transferFileToStream(path, resp.getOutputStream());
        }
    }

    // TODO: make parsing safer by catching exceptions
    private void handleRange(String rangeHeader,
                             Path path,
                             HttpServletResponse resp,
                             long fileSize,
                             boolean sendBody) throws IOException {
        String[] parts = rangeHeader.substring(RANGE_HEADER_START.length()).split("-", 2);
        long start = parts[0].isEmpty() ? 0 : Long.parseLong(parts[0]);
        long end = parts.length > 1 && !parts[1].isEmpty()
                ? Long.parseLong(parts[1])
                : fileSize - 1;
        if (start > end || end >= fileSize) {
            resp.setHeader("Content-Range",
                    "bytes */" + fileSize);
            resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return;
        }
        long contentLength = end - start + 1;
        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        resp.setHeader("Content-Range",
                "bytes " + start + "-" + end + "/" + fileSize);
        resp.setContentLengthLong(contentLength);
        if (sendBody) {
            filesystemService.transferFileRangeToStream(path, start, contentLength, resp.getOutputStream());
        }
    }

}
