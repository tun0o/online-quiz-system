package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.DailyChallengeDTO;
import com.example.online_quiz_system.dto.LeaderBoardEntryDTO;
import com.example.online_quiz_system.dto.UpdateProgressDTO;
import com.example.online_quiz_system.entity.UserChallengeProgress;
import com.example.online_quiz_system.security.UserPrincipal;
import com.example.online_quiz_system.service.ChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {

    @Autowired
    private ChallengeService challengeService;

    private Long getCurrentUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication == null || !authentication.isAuthenticated())
            return null;

        Object principal = authentication.getPrincipal();
        return principal instanceof UserPrincipal ? ((UserPrincipal) principal).getId() : null;
    }

    @GetMapping("/daily")
    public ResponseEntity<List<DailyChallengeDTO>> getTodayChallenge(){
        Long userId = getCurrentUserId();
        if(userId == null) {
            // Trả về danh sách rỗng thay vì lỗi 401 để tránh lỗi parse JSON ở frontend
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<DailyChallengeDTO> challenges = challengeService.getTodayChallenges(userId);
        return ResponseEntity.ok(challenges);
    }

    @PostMapping("/{challengeId}/progress")
    public ResponseEntity<UserChallengeProgress> updateProgress(@PathVariable Long challengeId,
                                                                @RequestBody UpdateProgressDTO dto){
        Long userId = getCurrentUserId();
        if(userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        UserChallengeProgress progress = challengeService.updateProgress(challengeId, userId, dto.getProgressValue());
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderBoardEntryDTO>> getLeaderBoard() {
        List<LeaderBoardEntryDTO> leaderboard = challengeService.getLeaderBoard();
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/my-rank")
    public ResponseEntity<LeaderBoardEntryDTO> getMyRank() {
        Long userId = getCurrentUserId();
        if(userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        LeaderBoardEntryDTO myRank = challengeService.getUserRank(userId);
        return ResponseEntity.ok(myRank);
    }
}
