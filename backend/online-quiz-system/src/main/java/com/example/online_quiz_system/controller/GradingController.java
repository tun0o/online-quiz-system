package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.GradeSubmissionDTO;
import com.example.online_quiz_system.dto.GradingDetailDTO;
import com.example.online_quiz_system.dto.GradingRequestDTO;
import com.example.online_quiz_system.service.GradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/admin/grading")
@CrossOrigin(origins = "https://localhost:5173")
public class GradingController {

    @Autowired
    private GradingService gradingService;

    @GetMapping("/requests")
    public ResponseEntity<List<GradingRequestDTO>> getPendingRequests() {
        List<GradingRequestDTO> requests = gradingService.getPendingGradingRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<GradingDetailDTO> getAttemptDetails(@PathVariable Long attemptId){
        GradingDetailDTO details = gradingService.getAttemptDetailsForGrading(attemptId);
        return ResponseEntity.ok(details);
    }

    @PostMapping("/submit")
    public ResponseEntity<Void> submitGrades(@RequestBody GradeSubmissionDTO submissionDTO){
        gradingService.submitGrades(submissionDTO);
        return ResponseEntity.ok().build();
    }
}
