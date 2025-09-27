package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.LoginSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginSessionRepository extends JpaRepository<LoginSession, Long> {
    java.util.List<LoginSession> findAllByUser_Id(Long userId);
}


