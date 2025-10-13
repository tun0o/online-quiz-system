package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.*;
import com.example.online_quiz_system.entity.QuizAttempt;
import com.example.online_quiz_system.security.UserPrincipal;
import com.example.online_quiz_system.service.QuizAttemptService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
public class QuizTakingController {

    @Autowired
    private QuizAttemptService quizAttemptService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        return principal instanceof UserPrincipal ? ((UserPrincipal) principal).getId() : null;
    }

    @PostMapping("/{quizId}/start")
    public ResponseEntity<QuizStartResponseDTO> startQuiz(@PathVariable Long quizId) {
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        QuizAttempt attempt = quizAttemptService.startQuizAttempt(quizId, userId);
        QuizForTakingDTO quizData = quizAttemptService.getQuizForTaking(quizId);

        QuizStartResponseDTO response = new QuizStartResponseDTO(attempt.getId(), quizData);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/submit/{attemptId}")
    public ResponseEntity<QuizResultDTO> submitQuizForGrading(@PathVariable Long attemptId,
                                                              @Valid @RequestBody QuizAttemptRequestDTO attemptRequestDTO){
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        QuizResultDTO result = quizAttemptService.submitAndGradeQuiz(attemptId, attemptRequestDTO, userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{attemptId}/request-grading")
    public ResponseEntity<?> requestGrading(@PathVariable Long attemptId){
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        quizAttemptService.requestEssayGrading(attemptId, userId);
        return ResponseEntity.ok().body(Map.of("message", "Yêu cầu chấm bài đã được gửi thành công."));
    }
}
