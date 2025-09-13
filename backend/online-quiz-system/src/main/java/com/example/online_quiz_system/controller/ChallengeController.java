package com.example.online_quiz_system.controller;

import com.example.online_quiz_system.dto.DailyChallengeDTO;
import com.example.online_quiz_system.dto.LeaderBoardEntryDTO;
import com.example.online_quiz_system.dto.UpdateProgressDTO;
import com.example.online_quiz_system.entity.UserChallengeProgress;
import com.example.online_quiz_system.service.ChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
@CrossOrigin(origins = "https://localhost:5173")
public class ChallengeController {

    @Autowired
    private ChallengeService challengeService;

    private static final Long MOCK_USER_ID = 1L;

    @GetMapping("/daily")
    public ResponseEntity<List<DailyChallengeDTO>> getTodayChallenge(){
        List<DailyChallengeDTO> challenges = challengeService.getTodayChallenges(MOCK_USER_ID);
        return ResponseEntity.ok(challenges);
    }

    @PostMapping("/{challengeId}/progress")
    public ResponseEntity<UserChallengeProgress> updateProgress(@PathVariable Long challengeId,
                                                                @RequestBody UpdateProgressDTO dto){
        UserChallengeProgress progress = challengeService.updateProgress(challengeId, MOCK_USER_ID, dto.getProgressValue());
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderBoardEntryDTO>> getLeaderBoard() {
        List<LeaderBoardEntryDTO> leaderboard = challengeService.getLeaderBoard();
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/my-rank")
    public ResponseEntity<Long> getMyRank() {
        Long rank = challengeService.getUserRank(MOCK_USER_ID);
        return ResponseEntity.ok(rank != null ? rank : 0L);
    }

//    @PostMapping("/update-quiz-progress")
//    public ResponseEntity<String> updateQuizProgress(@RequestParam Integer correctAnswer,
//                                                     @RequestParam Integer studyTimeMinutes,
//                                                     @RequestParam Integer quizPoints){
//        List<DailyChallengeDTO> challenges = challengeService.getTodayChallenges(MOCK_USER_ID);
//
//        for(DailyChallengeDTO challenge : challenges){
//            if(!challenge.getIsCompleted()){
//                switch (challenge.getChallengeType()){
//                    case CORRECT_ANSWERS:
//                        challengeService.updateProgress(challenge.getId(), MOCK_USER_ID, challenge.getCurrentProgress() + correctAnswer);
//                        break;
//                    case STUDY_TIME_MINUTES:
//                        challengeService.updateProgress(challenge.getId(), MOCK_USER_ID, challenge.getCurrentProgress() + studyTimeMinutes);
//                        break;
//                    case COMPLETE_QUIZZES:
//                        challengeService.updateProgress(challenge.getId(), MOCK_USER_ID, challenge.getCurrentProgress() + 1);
//                        break;
//                }
//            }
//        }
//
//        if(quizPoints > 0) {
//            challengeService.updateUserPoints(MOCK_USER_ID, quizPoints, "QUIZ_COMPLETION", null);
//        }
//
//        return ResponseEntity.ok("Progress updated successfully");
//    }
}
