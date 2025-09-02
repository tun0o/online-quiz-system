package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.QuizSubmissionDTO;
import com.example.online_quiz_system.dto.RejectSubmissionDTO;
import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.service.QuizSubmissionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/quiz-submissions")
@CrossOrigin(origins = "http://localhost:5173")
public class QuizSubmissionController {

    @Autowired
    private QuizSubmissionService submissionService;

    private static final Long MOCK_CONTRIBUTOR_ID = 1L;
    private static final Long MOCK_ADMIN_ID = 2L;

    @PostMapping
    public ResponseEntity<QuizSubmission> submitQuiz(@Valid @RequestBody QuizSubmissionDTO dto) {
        QuizSubmission submission = submissionService.submitQuiz(dto, MOCK_CONTRIBUTOR_ID);
        return ResponseEntity.ok(submission);
    }

//    @GetMapping()
//    public ResponseEntity<Page<QuizSubmission>> getAllSubmission(@RequestParam(defaultValue = "0") int page,
//                                                                 @RequestParam(defaultValue = "12") int size){
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        Page<QuizSubmission> submissions = submissionService.getAllSubmissions(pageable);
//        return ResponseEntity.ok(submissions);
//    }

    @GetMapping("/public")
    public ResponseEntity<Page<QuizSubmission>> getPublicQuizzes(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String subject,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<QuizSubmission> quizzes = submissionService.findPublicQuizzes(keyword, subject, pageable);
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/contributor/{contributorId}")
    public ResponseEntity<Page<QuizSubmission>> getSubmissionsByContributor(@PathVariable Long contributorId,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<QuizSubmission> submissions = submissionService.getSubmissionsByContributor(contributorId, pageable);
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
        QuizSubmission submission = submissionService.updateSubmission(id, dto, MOCK_CONTRIBUTOR_ID);
        return ResponseEntity.ok(submission);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long id){
        submissionService.deleteSubmission(id, MOCK_CONTRIBUTOR_ID);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<QuizSubmission> approveSubmission(@PathVariable Long id){
        QuizSubmission submission = submissionService.approveSubmission(id, MOCK_ADMIN_ID);
        return ResponseEntity.ok(submission);
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<QuizSubmission> rejectSubmission(@PathVariable Long id,
                                                           @Valid @RequestBody RejectSubmissionDTO dto){
        QuizSubmission submission = submissionService.rejectSubmission(id, dto.getReason(), MOCK_ADMIN_ID);
        return ResponseEntity.ok(submission);
    }

    @GetMapping("/stats/pending-count")
    public ResponseEntity<Long> getPendingCount(){
        long count = submissionService.getPendingCount();
        return ResponseEntity.ok(count);
    }
}
