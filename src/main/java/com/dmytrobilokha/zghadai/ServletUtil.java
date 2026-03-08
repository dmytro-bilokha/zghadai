package com.dmytrobilokha.zghadai;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

public final class ServletUtil {

    // TODO: make this a configuration
    public static final String CONTENT_ROOT = "/usr/home/dmytro/gallery-demo";
    private static final Set<Path> STATIC_CONTENT_DIRS = Set.of(Path.of("/js"), Path.of("/css"));

    private ServletUtil() {
        // no instance
    }

    public static Path getRequestedRelativePath(HttpServletRequest req) {
        return Path.of(req.getRequestURI().substring(req.getContextPath().length())).normalize();
    }

    public static boolean isStaticContentRequested(HttpServletRequest req) {
        var relativePath = getRequestedRelativePath(req);
        return relativePath.getNameCount() > 1
        && STATIC_CONTENT_DIRS.stream()
                .anyMatch(relativePath::startsWith);
    }

    public static Path getRequestedFilesystemPath(HttpServletRequest req) {
        return Path.of(CONTENT_ROOT + getRequestedRelativePath(req));
    }

    public static String generateFileEtag(Path path, long lastModifiedMillis, long size) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to initialize message digest", e);
        }
        digest.update((size + path.toString()
                + lastModifiedMillis).getBytes(StandardCharsets.UTF_8));
        return "\"" + HexFormat.of().formatHex(digest.digest()) + "\"";
    }

}
