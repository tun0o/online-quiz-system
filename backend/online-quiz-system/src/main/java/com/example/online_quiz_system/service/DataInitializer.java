package com.example.online_quiz_system.service;

import com.example.online_quiz_system.enums.Role;
import com.example.online_quiz_system.entity.User;
import com.example.online_quiz_system.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String adminEmail = "admin@quiz.com";
        if(!userRepository.existsByEmail(adminEmail)) {
            User adminUser = User.builder().email(adminEmail).passwordHash(passwordEncoder.encode("admin"))
                    .name("Default Admin").role(Role.ADMIN).isVerified(true).build();
            userRepository.save(adminUser);
            logger.info("Tao thanh cong default admin voi email: {}", adminEmail);
        }
    }
}
