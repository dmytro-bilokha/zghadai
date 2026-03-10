package com.dmytrobilokha.zghadai;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Set;

@ApplicationScoped
public class FilesystemService {

    private static final Path THUMBNAILS_DIR = Path.of("thumbnails");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "mpo");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("3gp", "avi", "mov", "mp4", "mpg", "mts", "webm");

    private ConfigService configService;

    public FilesystemService() {
        // CDI no-args required constructor
    }

    @Inject
    public FilesystemService(ConfigService configService) {
        this.configService = configService;
    }

    public Path relativeToAbsolutePath(Path relativePath) {
        return Path.of(configService.getContentRoot() + relativePath);
    }

    public DirectoryViewModel buildDirectoryViewModel(Path absolutePath) throws IOException {
        var directories = new ArrayList<String>();
        var imageFiles = new ArrayList<String>();
        var videoFiles = new ArrayList<String>();
        try (var directoryStream = Files.newDirectoryStream(absolutePath)) {
            for (var entryPath : directoryStream) {
                var fileNamePath = entryPath.getFileName();
                var fileNameString = fileNamePath == null ? null : fileNamePath.toString();
                if (fileNameString == null) {
                    // should never happen, but let's skip invalid path just in case
                } else if (isExposableDirectory(entryPath)) {
                    directories.add(fileNameString);
                } else if (isImageFile(entryPath)) {
                    imageFiles.add(fileNameString);
                } else if (isVideoFile(entryPath)) {
                    videoFiles.add(fileNameString);
                }
            }
        }
        return new DirectoryViewModel(
                directories, imageFiles, videoFiles, absolutePath.toString().equals(configService.getContentRoot()));
    }

    public boolean isDirectoryAvailable(Path path) throws IOException {
        return Files.exists(path) && isExposableDirectory(path);
    }

    public boolean isFileAvailable(Path path) throws IOException {
        return Files.exists(path) && (isImageFile(path) || isVideoFile(path));
    }

    public FileTime getLastModifiedTime(Path path) throws IOException {
        return Files.getLastModifiedTime(path);
    }

    public long getFileSize(Path path) throws IOException {
        return Files.size(path);
    }

    public void transferFileToStream(Path path, OutputStream outputStream) throws IOException {
        Files.copy(path, outputStream);
    }

    public void transferFileRangeToStream(
            Path path, long rangeStart, long rangeLength, OutputStream outputStream) throws IOException {
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ)) {
            fileChannel.transferTo(rangeStart, rangeLength, Channels.newChannel(outputStream));
        }
    }

    private boolean isExposableDirectory(Path path) throws IOException {
        return Files.isDirectory(path)
                && Files.isReadable(path)
                && Files.isExecutable(path)
                && !Files.isSymbolicLink(path)
                && !Files.isHidden(path)
                && !THUMBNAILS_DIR.equals(path.getFileName());
    }

    private boolean isImageFile(Path path) throws IOException {
        return isLegitFile(path) && doesExtensionMatch(path, IMAGE_EXTENSIONS);
    }

    private boolean isLegitFile(Path path) throws IOException {
        return Files.isRegularFile(path)
                && Files.isReadable(path)
                && !Files.isSymbolicLink(path)
                && !Files.isHidden(path);
    }

    private boolean doesExtensionMatch(Path path, Set<String> extensions) {
        var fileNamePath = path.getFileName();
        if (fileNamePath == null) {
            return false;
        }
        var fileNameString = fileNamePath.toString();
        int lastDotIndex = fileNameString.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileNameString.length() - 1) {
            return false;
        }
        var extension = fileNameString.substring(lastDotIndex + 1).toLowerCase();
        return extensions.contains(extension);
    }

    private boolean isVideoFile(Path path) throws IOException {
        return isLegitFile(path) && doesExtensionMatch(path, VIDEO_EXTENSIONS);
    }

}
