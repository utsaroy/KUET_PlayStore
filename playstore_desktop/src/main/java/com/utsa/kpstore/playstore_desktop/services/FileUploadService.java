package com.utsa.kpstore.playstore_desktop.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUploadService {

    // Base directory for storing uploaded files
    private static final String BASE_UPLOAD_DIR = "playstore_uploads";
    private static final String ICONS_DIR = "icons";
    private static final String PACKAGES_DIR = "packages";
    private static final String SCREENSHOTS_DIR = "screenshots";

    // Maximum file sizes (in bytes)
    private static final long MAX_ICON_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final long MAX_PACKAGE_SIZE = 100 * 1024 * 1024; // 100 MB
    private static final long MAX_SCREENSHOT_SIZE = 10 * 1024 * 1024; // 10 MB

    /**
     * Initialize upload directories if they don't exist
     */
    public static void initializeUploadDirectories() {
        try {
            Files.createDirectories(Paths.get(BASE_UPLOAD_DIR, ICONS_DIR));
            Files.createDirectories(Paths.get(BASE_UPLOAD_DIR, PACKAGES_DIR));
            Files.createDirectories(Paths.get(BASE_UPLOAD_DIR, SCREENSHOTS_DIR));
            System.out.println("Upload directories initialized successfully");
        } catch (IOException e) {
            System.err.println("Error creating upload directories: " + e.getMessage());
        }
    }

    public static String uploadIcon(File iconFile, int appId) throws IOException {
        if (iconFile == null || !iconFile.exists()) {
            throw new IllegalArgumentException("Icon file does not exist");
        }

        if (iconFile.length() > MAX_ICON_SIZE) {
            throw new IllegalArgumentException("Icon file size exceeds maximum limit of 5 MB");
        }

        String fileExtension = getFileExtension(iconFile.getName());
        if (!isValidImageExtension(fileExtension)) {
            throw new IllegalArgumentException("Invalid icon file format. Only PNG, JPG, JPEG allowed");
        }

        String fileName = "icon_" + appId + "_" + System.currentTimeMillis() + fileExtension;
        Path targetPath = Paths.get(BASE_UPLOAD_DIR, ICONS_DIR, fileName);

        Files.copy(iconFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path for database storage
        return ICONS_DIR + "/" + fileName;
    }

    public static String uploadPackage(File packageFile, int appId) throws IOException {
        if (packageFile == null || !packageFile.exists()) {
            throw new IllegalArgumentException("Package file does not exist");
        }

        if (packageFile.length() > MAX_PACKAGE_SIZE) {
            throw new IllegalArgumentException("Package file size exceeds maximum limit of 100 MB");
        }

        String fileExtension = getFileExtension(packageFile.getName());
        String fileName = "package_" + appId + "_" + System.currentTimeMillis() + fileExtension;
        Path targetPath = Paths.get(BASE_UPLOAD_DIR, PACKAGES_DIR, fileName);

        Files.copy(packageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Return relative path for database storage
        return PACKAGES_DIR + "/" + fileName;
    }


    public static Path getFullPath(String relativePath) {
        return Paths.get(BASE_UPLOAD_DIR, relativePath);
    }


    public static boolean deleteFile(String relativePath) {
        try {
            Path filePath = getFullPath(relativePath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Error deleting file: " + e.getMessage());
            return false;
        }
    }


    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    private static boolean isValidImageExtension(String extension) {
        String ext = extension.toLowerCase();
        return ext.equals(".png") || ext.equals(".jpg") || ext.equals(".jpeg");
    }


    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Check if a file exists
     */
    public static boolean fileExists(String relativePath) {
        return Files.exists(getFullPath(relativePath));
    }

    /**
     * Get file size
     */
    public static long getFileSize(File file) {
        return file.length();
    }
}
