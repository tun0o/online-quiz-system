package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.QuizSubmissionDTO;
import com.example.online_quiz_system.dto.RejectSubmissionDTO;
import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.security.UserPrincipal;
import com.example.online_quiz_system.service.QuizSubmissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/quiz-submissions")
public class QuizSubmissionController {

    @Autowired
    private QuizSubmissionService submissionService;

    // Helper để lấy userId từ SecurityContext
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<QuizSubmission> submitQuiz(@Valid @RequestBody QuizSubmissionDTO dto) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        QuizSubmission submission = submissionService.submitQuiz(dto, userId);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/public")
    public ResponseEntity<Page<QuizSubmission>> getPublicQuizzes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<QuizSubmission> quizzes = submissionService.findPublicQuizzes(keyword, subject, difficulty, pageable);
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/my-submissions")
    public ResponseEntity<Page<QuizSubmission>> getSubmissionsByContributor(
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        if(userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<QuizSubmission> submissions = submissionService.getSubmissionsByContributor(userId, pageable);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<QuizSubmission>> getPendingSubmissions(@RequestParam(defaultValue = "0") int page,
                                                                      @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<QuizSubmission> submissions = submissionService.getPendingSubmissions(pageable);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizSubmission> getSubmissionDetail(@PathVariable Long id){
        QuizSubmission submission = submissionService.getSubmissionById(id);
        if(submission == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(submission);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizSubmission> updateSubmission(@PathVariable Long id,
                                                           @Valid @RequestBody QuizSubmissionDTO dto){
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        QuizSubmission submission = submissionService.updateSubmission(id, dto, userId);
        return ResponseEntity.ok(submission);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id){
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        submissionService.deleteSubmission(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<QuizSubmission> approveSubmission(@PathVariable Long id){
        Long adminId = getCurrentUserId();
        if (adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        QuizSubmission submission = submissionService.approveSubmission(id, adminId);
        return ResponseEntity.ok(submission);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<QuizSubmission> rejectSubmission(@PathVariable Long id,
                                                           @Valid @RequestBody RejectSubmissionDTO dto){
        Long adminId = getCurrentUserId();
        if (adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        QuizSubmission submission = submissionService.rejectSubmission(id, dto.getReason(), adminId);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/stats/pending-count")
    public ResponseEntity<Long> getPendingCount(){
        long count = submissionService.getPendingCount();
        return ResponseEntity.ok(count);
    }
}