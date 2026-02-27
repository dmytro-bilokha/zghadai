package com.dmytrobilokha.zghadai;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/"})
public class DirectoryViewServlet extends HttpServlet {

    // TODO: make this a configuration
    private static final String CONTENT_ROOT = "/usr/home/dmytro/gallery-demo";
    private static final Logger LOG = LoggerFactory.getLogger(DirectoryViewServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        var relativePath = Path.of(req.getRequestURI().substring(req.getContextPath().length())).normalize();
        var absolutePath = Path.of(CONTENT_ROOT + relativePath);
        var directories = new ArrayList<String>();
        var files = new ArrayList<String>();
        try (var directoryStream = Files.newDirectoryStream(absolutePath)) {
            for (var entryPath : directoryStream) {
                if (Files.isDirectory(entryPath)) {
                    directories.add(entryPath.getFileName().toString());
                } else if (Files.isRegularFile(entryPath)) {
                    files.add(entryPath.getFileName().toString());
                }
            }
        } catch (IOException e) {
            LOG.warn("Failed to list directory '{}'", absolutePath, e);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        var viewModel = new DirectoryViewModel(
                List.of(relativePath.toString(), absolutePath.toString(), "directory3"),
                directories,
                files
        );
        viewModel.forwardToView(req, resp);
    }

}
