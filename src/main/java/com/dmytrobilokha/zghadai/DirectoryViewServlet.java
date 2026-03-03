package com.dmytrobilokha.zghadai;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet(urlPatterns = {"/"})
public class DirectoryViewServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryViewServlet.class);

    private final FilesystemService filesystemService;

    @Inject
    public DirectoryViewServlet(FilesystemService filesystemService) {
        this.filesystemService = filesystemService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var absolutePath = ServletUtil.getRequestedFilesystemPath(req);
        try {
            if (!filesystemService.isDirectoryAvailable(absolutePath)) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        } catch (IOException e) {
            logger.warn("Failed to check availability of a directory '{}'", absolutePath, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        DirectoryViewModel viewModel;
        try {
            viewModel = filesystemService.buildDirectoryViewModel(absolutePath);
        } catch (IOException e) {
            logger.warn("Failed to list directory '{}'", absolutePath, e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        viewModel.forwardToView(req, resp);
    }

}
