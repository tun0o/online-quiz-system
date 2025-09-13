package com.example.online_quiz_system.repository;

import com.example.online_quiz_system.entity.EssayGradingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EssayGradingRequestRepository extends JpaRepository<EssayGradingRequest, Long> {
}
