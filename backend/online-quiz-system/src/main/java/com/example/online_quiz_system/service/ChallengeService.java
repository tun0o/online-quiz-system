package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.DailyChallengeDTO;
import com.example.online_quiz_system.dto.LeaderBoardEntryDTO;
import com.example.online_quiz_system.entity.*;
import com.example.online_quiz_system.enums.DifficultyLevel;
import com.example.online_quiz_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

@Service
@Transactional
public class ChallengeService {

    @Autowired
    private ChallengeTemplateRepository templateRepository;

    @Autowired
    private DailyChallengeRepository dailyChallengeRepository;

    @Autowired
    private UserChallengeProgressRepository progressRepository;

    @Autowired
    private UserRankingRepository rankingRepository;

    @Autowired
    private DailyPointHistoryRepository pointHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    public List<DailyChallengeDTO> getTodayChallenges(Long userId) {
        LocalDate today = LocalDate.now();

        generateDailyChallengeIfNeeded(today);

        List<DailyChallenge> todayChallenges = dailyChallengeRepository.findTodayChallengeWithTemplate(today);

        List<UserChallengeProgress> userProgress = progressRepository.findUserProgressForDate(userId, today);

        return todayChallenges.stream().map(challenge -> {
            DailyChallengeDTO dto = new DailyChallengeDTO();
            dto.setId(challenge.getId());
            dto.setTitle(challenge.getTemplate().getTitle());
            dto.setDescription(challenge.getTemplate().getDescription());
            dto.setChallengeType(challenge.getTemplate().getChallengeType());
            dto.setDifficultyLevel(challenge.getTemplate().getDifficultyLevel());
            dto.setTargetValue(challenge.getTemplate().getTargetValue());
            dto.setRewardPoints(challenge.getTemplate().getRewardPoints());

            Optional<UserChallengeProgress> progress = userProgress.stream()
                    .filter(p -> p.getDailyChallenge().getId().equals(challenge.getId()))
                    .findFirst();

            if(progress.isPresent()){
                dto.setCurrentProgress(progress.get().getCurrentProgress());
                dto.setIsCompleted(progress.get().getIsCompleted());
            } else {
                dto.setCurrentProgress(0);
                dto.setIsCompleted(false);
            }

            int percentage = (dto.getCurrentProgress() * 100) / dto.getTargetValue();
            dto.setProgressPercentage(Math.min(percentage, 100));

            return dto;
        }).toList();
    }

    private void generateDailyChallengeIfNeeded(LocalDate date){
        List<DailyChallenge> existingChallenges = dailyChallengeRepository.findByChallengeDate(date);

        if(existingChallenges.size() < 3){
            dailyChallengeRepository.deleteAll(existingChallenges);

            List<ChallengeTemplate> easyTemplates = templateRepository.findActiveByDifficultyLevel(DifficultyLevel.EASY);
            List<ChallengeTemplate> mediumTemplates = templateRepository.findActiveByDifficultyLevel(DifficultyLevel.MEDIUM);
            List<ChallengeTemplate> hardTemplates = templateRepository.findActiveByDifficultyLevel(DifficultyLevel.HARD);

            Random random = new Random();

            if(!easyTemplates.isEmpty()){
                ChallengeTemplate easyTemplate = easyTemplates.get(random.nextInt(easyTemplates.size()));
                createDailyChallenge(easyTemplate, date);
            }

            if(!mediumTemplates.isEmpty()){
                ChallengeTemplate mediumTemplate = mediumTemplates.get(random.nextInt(mediumTemplates.size()));
                createDailyChallenge(mediumTemplate, date);
            }

            if(!hardTemplates.isEmpty()){
                ChallengeTemplate hardTemplate = hardTemplates.get(random.nextInt(hardTemplates.size()));
                createDailyChallenge(hardTemplate, date);
            }
        }
    }

    private void createDailyChallenge(ChallengeTemplate template, LocalDate date){
        DailyChallenge dailyChallenge = new DailyChallenge();
        dailyChallenge.setTemplate(template);
        dailyChallenge.setChallengeDate(date);
        dailyChallenge.setIsActive(true);
        dailyChallengeRepository.save(dailyChallenge);
    }

    public UserChallengeProgress updateProgress(Long challengeId, Long userId, Integer progressValue){
        DailyChallenge challenge = dailyChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge không tồn tại"));

        UserChallengeProgress progress = progressRepository.findByUserIdAndDailyChallengeId(userId, challengeId)
                .orElse(new UserChallengeProgress());

        if(progress.getId() == null){
            progress.setUserId(userId);
            progress.setDailyChallenge(challenge);
        }

        progress.setCurrentProgress(Math.min(progressValue, challenge.getTemplate().getTargetValue()));

        boolean wasCompleted = progress.getIsCompleted();
        boolean isNowCompleted = progress.getCurrentProgress() >= challenge.getTemplate().getTargetValue();

        if(!wasCompleted && isNowCompleted){
            progress.setIsCompleted(true);
            progress.setCompletedAt(LocalDateTime.now());
            progress.setPointsEarned(challenge.getTemplate().getRewardPoints());

            updateUserPoints(userId, challenge.getTemplate().getRewardPoints(), "CHALLENGE", challengeId);
        }

        return progressRepository.save(progress);
    }

    public void updateUserPoints(Long userId, Integer points, String source, Long sourceId){
        UserRanking ranking = rankingRepository.findByUserId(userId)
                .orElse(new UserRanking());

        if(ranking.getId() == null){
            ranking.setUserId(userId);
        }

        ranking.setTotalPoints(ranking.getTotalPoints() + points);
        ranking.setDailyPoints(ranking.getDailyPoints() + points);
        ranking.setWeeklyPoints(ranking.getWeeklyPoints() + points);
        ranking.setMonthlyPoints(ranking.getMonthlyPoints() + points);
        ranking.setConsumptionPoints(ranking.getConsumptionPoints() + points);

        LocalDate today = LocalDate.now();
        if(!today.equals(ranking.getLastActivityDate())){
            if(ranking.getLastActivityDate() != null && ranking.getLastActivityDate().equals(today.minusDays(1))){
                ranking.setCurrentStreak(ranking.getCurrentStreak() + 1);
            } else {
                ranking.setCurrentStreak(1);
            }
            ranking.setMaxStreak(Math.max(ranking.getMaxStreak(), ranking.getCurrentStreak()));
        }
        ranking.setLastActivityDate(today);

        rankingRepository.save(ranking);

        DailyPointHistory history = new DailyPointHistory();
        history.setUserId(userId);
        history.setPointsEarned(points);
        history.setActivityDate(today);
        history.setSource(source);
        history.setSourceId(sourceId);
        pointHistoryRepository.save(history);
    }

    public List<LeaderBoardEntryDTO> getLeaderBoard(){
        List<UserRanking> rankings = rankingRepository.findTop10ByOrderByTotalPointsDesc();
        return IntStream.range(0, rankings.size())
                .mapToObj(i -> {
                    UserRanking ranking = rankings.get(i);
                    String userName = userRepository.findById(ranking.getUserId()).get().getName();
                    LeaderBoardEntryDTO dto = new LeaderBoardEntryDTO();
                    dto.setRank(i + 1);
                    dto.setUserId(ranking.getUserId());
                    dto.setUserName(userName);
                    dto.setTotalPoints(ranking.getTotalPoints());
                    dto.setWeeklyPoints(ranking.getWeeklyPoints());
                    dto.setCurrentStreak(ranking.getCurrentStreak());

                    switch (i) {
                        case 0 : dto.setMedal("🥇");
                        break;
                        case 1 : dto.setMedal("🥈");
                        break;
                        case 2 : dto.setMedal("🥉");
                        break;
                        default: dto.setMedal("");
                    }

                    return dto;
                }).toList();
    }

    public LeaderBoardEntryDTO getUserRank(Long userId){
        UserRanking ranking = rankingRepository.findByUserId(userId)
                .orElse(new UserRanking());

        if(ranking.getUserId() == null) ranking.setUserId(userId);

        Integer userRank = rankingRepository.findUserRankByUserId(userId);
        LeaderBoardEntryDTO dto = new LeaderBoardEntryDTO();
        dto.setRank(userRank);
        dto.setUserId(ranking.getUserId());
        dto.setUserName(userRepository.findById(userId).get().getName());
        dto.setTotalPoints(ranking.getTotalPoints());
        dto.setWeeklyPoints(ranking.getWeeklyPoints());
        dto.setCurrentStreak(ranking.getCurrentStreak());

        switch (userRank) {
            case 0 : dto.setMedal("🥇");
                break;
            case 1 : dto.setMedal("🥈");
                break;
            case 2 : dto.setMedal("🥉");
                break;
            default: dto.setMedal("");
        }


        return dto;
    }

    public void updateQuizCompletionProgress(Long userId, int correctAnswersCount, int studyTimeMinutes, int quizPoints, Long sourceId){
        List<DailyChallengeDTO> challenges = getTodayChallenges(userId);

        for(DailyChallengeDTO challenge : challenges){
            if(!challenge.getIsCompleted()) {
                switch (challenge.getChallengeType()) {
                    case CORRECT_ANSWERS:
                        updateProgress(challenge.getId(), userId, challenge.getCurrentProgress() + correctAnswersCount);
                        break;
                    case STUDY_TIME_MINUTES:
                        if(studyTimeMinutes > 0) {
                            updateProgress(challenge.getId(), userId, challenge.getCurrentProgress() + studyTimeMinutes);
                        }
                        break;
                    case COMPLETE_QUIZZES:
                        updateProgress(challenge.getId(), userId, challenge.getCurrentProgress() + 1);
                        break;
                }
            }
        }

        if(quizPoints > 0){
            updateUserPoints(userId, quizPoints, "QUIZ_COMPLETION", sourceId);
        }
    }
}
