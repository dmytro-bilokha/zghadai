package com.dmytrobilokha.zghadai;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

public class DirectoryViewModel {

    private static final String VIEW_MODEL_ATTRIBUTE = "vm";
    private static final String LOCATION = "/WEB-INF/jspx/directoryView.jspx";
    private final List<String> directories;
    private final List<String> images;
    private final List<String> videos;

    public DirectoryViewModel(List<String> directories, List<String> images, List<String> videos) {
        this.directories = directories;
        this.images = images;
        this.videos = videos;
    }

    public List<String> getDirectories() {
        return directories;
    }

    public List<String> getImages() {
        return images;
    }

    public List<String> getVideos() {
        return videos;
    }

    public void forwardToView(HttpServletRequest request, HttpServletResponse response) {
        request.setAttribute(VIEW_MODEL_ATTRIBUTE, this);
        try {
            request.getRequestDispatcher(LOCATION).forward(request, response);
        } catch (ServletException | IOException e) {
            throw new IllegalStateException("Failed to forward request to page: " + LOCATION, e);
        }
    }

}
