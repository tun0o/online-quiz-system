package com.example.online_quiz_system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Data
public class VnpayConfig {
    private String url;
    private String returnUrl;
    private String tmnCode;
    private String hashSecret;
    private String version;
}
