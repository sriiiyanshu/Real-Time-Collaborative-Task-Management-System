package com.taskmanager.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.Part;

/**
 * Utility class for handling file operations.
 */
public class FileUtil {
    
    private static final String UPLOAD_DIR = "upload"; // relative to application server
    private static final Map<String, String> MIME_TYPES = new HashMap<>();
    private static final int BUFFER_SIZE = 8192; // 8KB buffer size for file operations
    
    static {
        // Initialize common MIME types
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
        MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPES.put("ppt", "application/vnd.ms-powerpoint");
        MIME_TYPES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("gif", "image/gif");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "text/javascript");
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("xml", "application/xml");
        MIME_TYPES.put("zip", "application/zip");
    }
    
    /**
     * Creates the necessary directories for file uploads.
     * 
     * @param basePath The base path for the upload directory
     * @return The path to the upload directory
     */
    public static String createUploadDirectories(String basePath) {
        String uploadPath = basePath + File.separator + UPLOAD_DIR;
        File uploadDir = new File(uploadPath);
        
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            LogUtil.error("Failed to create upload directory at: " + uploadPath, null);
            return null;
        }
        
        return uploadPath;
    }
    
    /**
     * Saves an uploaded file from a multipart request.
     * 
     * @param filePart The file part from the multipart request
     * @param uploadDir The directory to save the file to
     * @return The saved file information map containing name, path, and size
     * @throws IOException If an I/O error occurs
     */
    public static Map<String, String> saveUploadedFile(Part filePart, String uploadDir) throws IOException {
        String fileName = getSubmittedFileName(filePart);
        String fileExtension = getFileExtension(fileName);
        String uniqueFileName = generateUniqueFileName(fileExtension);
        String filePath = uploadDir + File.separator + uniqueFileName;
        
        try (InputStream input = filePart.getInputStream();
             OutputStream output = new FileOutputStream(filePath)) {
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
        
        Map<String, String> fileInfo = new HashMap<>();
        fileInfo.put("originalName", fileName);
        fileInfo.put("savedName", uniqueFileName);
        fileInfo.put("path", filePath);
        fileInfo.put("size", String.valueOf(filePart.getSize()));
        fileInfo.put("contentType", getMimeType(fileExtension));
        
        return fileInfo;
    }
    
    /**
     * Gets the file name from a Part.
     * 
     * @param part The part containing the file
     * @return The file name
     */
    public static String getSubmittedFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        
        return "unknown";
    }
    
    /**
     * Gets the file extension from a file name.
     * 
     * @param fileName The file name
     * @return The file extension
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
    
    /**
     * Generates a unique file name with the original extension.
     * 
     * @param extension The file extension
     * @return A unique file name
     */
    public static String generateUniqueFileName(String extension) {
        String uniqueId = UUID.randomUUID().toString();
        return uniqueId + (extension.isEmpty() ? "" : "." + extension);
    }
    
    /**
     * Gets the MIME type for a file extension.
     * 
     * @param extension The file extension
     * @return The MIME type, defaults to "application/octet-stream" if not found
     */
    public static String getMimeType(String extension) {
        return MIME_TYPES.getOrDefault(extension.toLowerCase(), "application/octet-stream");
    }
    
    /**
     * Deletes a file.
     * 
     * @param filePath The path to the file to delete
     * @return true if the file was deleted successfully, false otherwise
     */
    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
            return true;
        } catch (IOException e) {
            LogUtil.error("Failed to delete file: " + filePath, e);
            return false;
        }
    }
    
    /**
     * Copies a file from a source to a destination.
     * 
     * @param sourceFilePath The source file path
     * @param destinationFilePath The destination file path
     * @return true if the file was copied successfully, false otherwise
     */
    public static boolean copyFile(String sourceFilePath, String destinationFilePath) {
        try (InputStream in = new FileInputStream(sourceFilePath);
             OutputStream out = new FileOutputStream(destinationFilePath)) {
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (IOException e) {
            LogUtil.error("Failed to copy file from " + sourceFilePath + " to " + destinationFilePath, e);
            return false;
        }
    }
    
    /**
     * Gets the file size in a human-readable format.
     * 
     * @param size The file size in bytes
     * @return A human-readable file size string
     */
    public static String getReadableFileSize(long size) {
        if (size <= 0) {
            return "0 B";
        }
        
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
}