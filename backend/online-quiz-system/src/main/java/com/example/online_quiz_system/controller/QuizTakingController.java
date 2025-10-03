package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.QuizAttemptRequestDTO;
import com.example.online_quiz_system.dto.QuizForTakingDTO;
import com.example.online_quiz_system.dto.QuizResultDTO;
import com.example.online_quiz_system.security.UserPrincipal;
import com.example.online_quiz_system.service.QuizAttemptService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{quizId}/take")
    public ResponseEntity<QuizForTakingDTO> getQuizForTaking(@PathVariable Long quizId){
        QuizForTakingDTO quiz = quizAttemptService.getQuizForTaking(quizId);
        return ResponseEntity.ok(quiz);
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizResultDTO> submitQuizForGrading(@Valid @RequestBody QuizAttemptRequestDTO attemptRequestDTO){
        Long userId = getCurrentUserId();
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        QuizResultDTO result = quizAttemptService.submitAndGradeQuiz(attemptRequestDTO, userId);
        return ResponseEntity.ok(result);
    }
}
