package com.example.online_quiz_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Thread pool cho UserProfile sync events
     */
    @Bean(name = "userProfileSyncExecutor")
    public Executor userProfileSyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 🔥 Production-ready ThreadPool configuration
        executor.setCorePoolSize(4);                    // Core threads for steady load
        executor.setMaxPoolSize(20);                    // Max threads for peak load
        executor.setQueueCapacity(500);                 // Larger queue for burst handling
        executor.setKeepAliveSeconds(60);               // Keep idle threads alive
        executor.setThreadNamePrefix("UserProfileSync-");
        
        // 🔥 CallerRunsPolicy: Tránh thả request khi queue đầy
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 🔥 Allow core threads to timeout (for better resource management)
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.initialize();
        return executor;
    }

    /**
     * Thread pool cho Email verification events
     */
    @Bean(name = "emailVerificationExecutor")
    public Executor emailVerificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Email verification specific configuration
        executor.setCorePoolSize(2);                    // Core threads for email sending
        executor.setMaxPoolSize(10);                    // Max threads for email burst
        executor.setQueueCapacity(100);                 // Queue for pending emails
        executor.setKeepAliveSeconds(60);               // Keep idle threads alive
        executor.setThreadNamePrefix("EmailVerification-");
        
        // CallerRunsPolicy: Tránh thả email requests khi queue đầy
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Allow core threads to timeout
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.initialize();
        return executor;
    }
}
