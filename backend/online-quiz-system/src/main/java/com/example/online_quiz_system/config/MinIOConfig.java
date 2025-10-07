package com.example.online_quiz_system.config;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO Configuration
 * Cấu hình kết nối với MinIO Object Storage
 */
@Configuration
@Slf4j
public class MinIOConfig {

    @Value("${app.minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${app.minio.access-key:minio}")
    private String accessKey;

    @Value("${app.minio.secret-key:minio123}")
    private String secretKey;

    @Value("${app.minio.bucket-name:avatars}")
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        try {
            log.info("Initializing MinIO client with endpoint: {}", endpoint);
            
            MinioClient client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            
            // Test connection
            client.listBuckets();
            log.info("MinIO client initialized successfully");
            
            return client;
        } catch (Exception e) {
            log.error("Failed to initialize MinIO client", e);
            throw new RuntimeException("MinIO client initialization failed", e);
        }
    }

    @Bean
    public String bucketName() {
        return bucketName;
    }
}
