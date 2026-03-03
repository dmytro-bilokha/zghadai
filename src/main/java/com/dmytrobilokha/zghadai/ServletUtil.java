package com.dmytrobilokha.zghadai;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ServletUtil {

    // TODO: make this a configuration
    private static final String CONTENT_ROOT = "/usr/home/dmytro/gallery-demo";

    private ServletUtil() {
        // no instance
    }

    public static Path getRequestedFilesystemPath(HttpServletRequest req) {
        var relativePath = Path.of(req.getRequestURI().substring(req.getContextPath().length())).normalize();
        return Path.of(CONTENT_ROOT + relativePath);
    }

    public static String generateFileEtag(Path path, long lastModifiedMillis, long size) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to initialize message digest", e);
        }
        digest.update((size + path.toString()
                + lastModifiedMillis).getBytes());
        return "\"" + HexFormat.of().formatHex(digest.digest()) + "\"";
    }

}
