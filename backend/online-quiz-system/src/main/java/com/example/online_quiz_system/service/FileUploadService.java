package com.example.online_quiz_system.service;

import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * File Upload Service
 * Xử lý upload file lên MinIO Object Storage
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final MinioClient minioClient;
    
    @Value("${app.minio.bucket-name:avatars}")
    private String bucketName;
    
    @Value("${app.minio.endpoint:http://localhost:9000}")
    private String endpoint;

    /**
     * Upload avatar image to MinIO
     */
    public String uploadAvatar(MultipartFile file, Long userId) {
        try {
            // Validate file
            validateImageFile(file);
            
            // Ensure bucket exists
            ensureBucketExists();
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = String.format("avatar_%d_%s%s", userId, UUID.randomUUID(), extension);
            
            // Upload file
            try (InputStream inputStream = file.getInputStream()) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filename)
                        .stream(inputStream, file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
                );
            }
            
            // Generate public URL
            String fileUrl = generateFileUrl(filename);
            
            log.info("Avatar uploaded successfully for user {}: {}", userId, fileUrl);
            return fileUrl;
            
        } catch (Exception e) {
            log.error("Failed to upload avatar for user {}", userId, e);
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage(), e);
        }
    }

    /**
     * Delete avatar from MinIO
     */
    public void deleteAvatar(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return;
            }
            
            // Extract object name from URL
            String objectName = extractObjectNameFromUrl(fileUrl);
            if (objectName == null) {
                log.warn("Cannot extract object name from URL: {}", fileUrl);
                return;
            }
            
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            
            log.info("Avatar deleted successfully: {}", objectName);
            
        } catch (Exception e) {
            log.error("Failed to delete avatar: {}", fileUrl, e);
            // Don't throw exception for delete operations
        }
    }

    /**
     * Validate image file
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required");
        }
        
        // Check file size (max 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 5MB");
        }
        
        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
        
        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null || !isValidImageExtension(filename)) {
            throw new IllegalArgumentException("Invalid image file format");
        }
    }

    /**
     * Check if file extension is valid for images
     */
    private boolean isValidImageExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals(".jpg") || extension.equals(".jpeg") || 
               extension.equals(".png") || extension.equals(".gif") || 
               extension.equals(".webp");
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Extract object name from MinIO URL
     */
    private String extractObjectNameFromUrl(String fileUrl) {
        try {
            // URL format: http://localhost:9000/bucket-name/object-name
            String[] parts = fileUrl.split("/");
            if (parts.length >= 2) {
                return parts[parts.length - 1];
            }
        } catch (Exception e) {
            log.warn("Failed to extract object name from URL: {}", fileUrl);
        }
        return null;
    }

    /**
     * Generate file URL
     */
    private String generateFileUrl(String filename) {
        // Remove trailing slash from endpoint if present
        String baseUrl = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return String.format("%s/%s/%s", baseUrl, bucketName, filename);
    }

    /**
     * Ensure bucket exists
     */
    private void ensureBucketExists() throws Exception {
        boolean bucketExists = minioClient.bucketExists(
            BucketExistsArgs.builder().bucket(bucketName).build()
        );
        
        if (!bucketExists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(bucketName).build()
            );
            log.info("Created bucket: {}", bucketName);
        }
    }
}
