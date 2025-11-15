package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.QuizSubmissionDTO;
import com.example.online_quiz_system.dto.RejectSubmissionDTO;
import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.security.UserPrincipal;
import com.example.online_quiz_system.service.MinioService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/quiz-submissions")
public class QuizSubmissionController {

    @Autowired
    private QuizSubmissionService submissionService;

    @Autowired
    private MinioService minioService;

    // Constants for audio file validation
    private static final long MAX_AUDIO_FILE_SIZE = 50 * 1024 * 1024; // 50 MB
    private static final List<String> ALLOWED_AUDIO_TYPES = List.of("audio/mpeg", "audio/wav", "audio/mp3");

    // Helper để lấy userId từ SecurityContext
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated())
            return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getId();
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<QuizSubmission> submitQuiz(@AuthenticationPrincipal UserPrincipal principal,
                                                    @Valid @RequestBody QuizSubmissionDTO dto) {
        QuizSubmission submission = submissionService.submitQuiz(dto, principal);
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
    public ResponseEntity<Page<QuizSubmission>> getSubmissionsByContributor(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = getCurrentUserId();
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<QuizSubmission> submissions = submissionService.getSubmissionsByContributor(userId, pageable);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/pending")
    public ResponseEntity<Page<QuizSubmission>> getPendingSubmissions(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<QuizSubmission> submissions = submissionService.getPendingSubmissions(pageable);
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizSubmission> getSubmissionDetail(@PathVariable Long id) {
        QuizSubmission submission = submissionService.getSubmissionById(id);
        if (submission == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(submission);
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizSubmission> updateSubmission(@PathVariable Long id,
            @Valid @RequestBody QuizSubmissionDTO dto) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        QuizSubmission submission = submissionService.updateSubmission(id, dto, userId);
        return ResponseEntity.ok(submission);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        submissionService.deleteSubmission(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<QuizSubmission> approveSubmission(@PathVariable Long id) {
        Long adminId = getCurrentUserId();
        if (adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        QuizSubmission submission = submissionService.approveSubmission(id, adminId);
        return ResponseEntity.ok(submission);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<QuizSubmission> rejectSubmission(@PathVariable Long id,
            @Valid @RequestBody RejectSubmissionDTO dto) {
        Long adminId = getCurrentUserId();
        if (adminId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        QuizSubmission submission = submissionService.rejectSubmission(id, dto.getReason(), adminId);
        return ResponseEntity.ok(submission);
    }

    @PostMapping("/questions/image")
    public ResponseEntity<?> uploadQuestionImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File không được để trống"));
        }
        try {
            String imageUrl = minioService.uploadFile(file, "question-images");
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi tải ảnh lên: " + e.getMessage()));
        }
    }

    @PostMapping("/audio")
    public ResponseEntity<?> uploadQuizAudio(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File không được để trống"));
        }

        // === VALIDATION START ===
        // 1. Validate file size
        if (file.getSize() > MAX_AUDIO_FILE_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("message", "Dung lượng file không được vượt quá 50MB."));
        }

        // 2. Validate file type (MIME type)
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_AUDIO_TYPES.contains(contentType.toLowerCase())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Định dạng file không hợp lệ. Chỉ chấp nhận file MP3 hoặc WAV."));
        }
        // === VALIDATION END ===

        try {
            // Tải file lên thư mục 'quiz-audio' trong MinIO bucket
            String audioUrl = minioService.uploadFile(file, "quiz-audio");
            return ResponseEntity.ok(Map.of("audioUrl", audioUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi khi tải file audio lên: " + e.getMessage()));
        }
    }
}