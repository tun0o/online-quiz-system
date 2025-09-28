package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.QuizAttemptRequestDTO;
import com.example.online_quiz_system.dto.QuizForTakingDTO;
import com.example.online_quiz_system.dto.QuizResultDTO;
import com.example.online_quiz_system.service.QuizAttemptService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = "https://localhost:5173")
public class QuizTakingController {

    @Autowired
    private QuizAttemptService quizAttemptService;

    private static final  Long MOCK_USER_ID = 1L;

    @GetMapping("/{quizId}/take")
    public ResponseEntity<QuizForTakingDTO> getQuizForTaking(@PathVariable Long quizId){
        QuizForTakingDTO quiz = quizAttemptService.getQuizForTaking(quizId);
        return ResponseEntity.ok(quiz);
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizResultDTO> submitQuizForGrading(@Valid @RequestBody QuizAttemptRequestDTO attemptRequestDTO){
        QuizResultDTO result = quizAttemptService.submitAndGradeQuiz(attemptRequestDTO, MOCK_USER_ID);
        return ResponseEntity.ok(result);
    }
}
