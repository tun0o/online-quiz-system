package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.*;
import com.example.online_quiz_system.entity.*;
import com.example.online_quiz_system.enums.GradingStatus;
import com.example.online_quiz_system.enums.QuestionType;
import com.example.online_quiz_system.exception.InsufficientPointsException;
import com.example.online_quiz_system.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuizAttemptService {

    @Autowired
    private QuizSubmissionRepository quizSubmissionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private UserAnswerRepository userAnswerRepository;

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private EssayGradingRequestRepository essayGradingRequestRepository;

    @Autowired
    private UserRankingRepository userRankingRepository;

    private static final int ESSAY_GRADING_COST = 100;

    public QuizForTakingDTO getQuizForTaking(Long quizId){
        QuizSubmission submission = quizSubmissionRepository.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found with id: " + quizId));

        QuizForTakingDTO quizDTO = new QuizForTakingDTO();
        quizDTO.setId(submission.getId());
        quizDTO.setTitle(submission.getTitle());
        quizDTO.setDescription(submission.getDescription());
        quizDTO.setSubject(submission.getSubject());
        quizDTO.setDifficultyLevel(submission.getDifficultyLevel());
        quizDTO.setDurationMinutes(submission.getDurationMinutes());

        List<QuestionForTakingDTO> questionDTOs = submission.getQuestions().stream()
                .map(q -> {
                    QuestionForTakingDTO qDTO = new QuestionForTakingDTO();
                    qDTO.setId(q.getId());
                    qDTO.setQuestionText(q.getQuestionText());
                    qDTO.setQuestionType(q.getQuestionType());

                    List<AnswerOptionForTakingDTO> optionDTOs = q.getAnswerOptions().stream()
                            .map(o -> {
                                AnswerOptionForTakingDTO oDTO = new AnswerOptionForTakingDTO();
                                oDTO.setId(o.getId());
                                oDTO.setOptionText(o.getOptionText());
                                return oDTO;
                            }).collect(Collectors.toList());
                    qDTO.setAnswerOptions(optionDTOs);

                    return qDTO;
                }).collect(Collectors.toList());

        quizDTO.setQuestions(questionDTOs);
        return quizDTO;
    }

    @Transactional
    public QuizResultDTO submitAndGradeQuiz(QuizAttemptRequestDTO attemptDTO, Long userId){
        QuizSubmission quiz = quizSubmissionRepository.findById(attemptDTO.getQuizId())
                .orElseThrow(() -> new EntityNotFoundException("Quiz ot found with id: " + attemptDTO.getQuizId()));

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(userId);
        attempt.setQuizSubmission(quiz);
        attempt.setStartTime(LocalDateTime.now());
        attempt.setTotalQuestions(quiz.getQuestions().size());
        attempt.setStatus("IN_PROGRESS");
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        List<SubmissionQuestion> allQuestions = quiz.getQuestions();
        BigDecimal totalEssayMaxScore = allQuestions.stream()
                .filter(q -> q.getQuestionType() == QuestionType.ESSAY)
                .map(SubmissionQuestion::getMaxScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long mcqCount = allQuestions.stream()
                .filter(q -> q.getQuestionType() != QuestionType.ESSAY)
                .count();

        BigDecimal totalMcqMaxScore = BigDecimal.TEN.subtract(totalEssayMaxScore);
        BigDecimal scorePerMcq = BigDecimal.ZERO;
        if(mcqCount > 0){
            scorePerMcq = totalMcqMaxScore.divide(BigDecimal.valueOf(mcqCount), 2, RoundingMode.HALF_UP);
        }

        Map<Long, SubmissionQuestion> questionMap = quiz.getQuestions().stream()
                .collect(Collectors.toMap(SubmissionQuestion::getId, Function.identity()));

        int correctAnswersCount = 0;
        List<UserAnswer> userAnswersToSave = new ArrayList<>();
        BigDecimal calculatedScore = BigDecimal.ZERO;

        Map<Long, UserAnswerRequestDTO> userAnswerMap = Optional.ofNullable(attemptDTO.getAnswers()).orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(UserAnswerRequestDTO::getQuestionId, Function.identity()));

        for(SubmissionQuestion question : allQuestions) {
            UserAnswerRequestDTO userAnswerDTO = userAnswerMap.get(question.getId());

            if (userAnswerDTO != null) {
                UserAnswer userAnswer = new UserAnswer();
                userAnswer.setQuizAttempt(savedAttempt);
                userAnswer.setQuestion(question);

                if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE || question.getQuestionType() == QuestionType.TRUE_FALSE) {
                    Optional<SubmissionAnswerOption> selectedOption = question.getAnswerOptions().stream()
                            .filter(o -> Objects.equals(o.getId(), userAnswerDTO.getSelectedOptionId()))
                            .findFirst();
                    selectedOption.ifPresent(userAnswer::setSelectedOption);

                    SubmissionAnswerOption correctOption = question.getAnswerOptions().stream()
                            .filter(SubmissionAnswerOption::getIsCorrect).findFirst().orElse(null);

                    boolean isCorrect = correctOption != null && Objects.equals(userAnswerDTO.getSelectedOptionId(), correctOption.getId());
                    userAnswer.setIsCorrect(isCorrect);
                    if (isCorrect) {
                        correctAnswersCount++;
                        calculatedScore = calculatedScore.add(scorePerMcq);
                    }
                } else if (question.getQuestionType() == QuestionType.ESSAY) {
                    userAnswer.setAnswerText(userAnswerDTO.getAnswerText());
                    userAnswer.setIsCorrect(null);
                }
                userAnswersToSave.add(userAnswer);
            }
        }

        List<QuestionResultDTO> questionResults = allQuestions.stream().map(question -> {
                    QuestionResultDTO questionResult = new QuestionResultDTO();
                    questionResult.setQuestionId(question.getId());
                    questionResult.setQuestionText(question.getQuestionText());
                    questionResult.setExplanation(question.getExplanation());
                    questionResult.setUserAnswer(userAnswerMap.get(question.getId()));

                    SubmissionAnswerOption correctOption = question.getAnswerOptions().stream()
                            .filter(SubmissionAnswerOption::getIsCorrect).findFirst().orElse(null);
                    questionResult.setCorrectAnswer(correctOption);

                    boolean isCorrect = correctOption != null && userAnswerMap.containsKey(question.getId()) && Objects.equals(userAnswerMap.get(question.getId()).getSelectedOptionId(), correctOption.getId());
                    questionResult.setIsCorrect(question.getQuestionType() == QuestionType.ESSAY ? null : isCorrect);
                    return questionResult;
                }).collect(Collectors.toList());

        userAnswerRepository.saveAll(userAnswersToSave);

        savedAttempt.setEndTime(LocalDateTime.now());
        savedAttempt.setCorrectAnswers(correctAnswersCount);
        savedAttempt.setScore(calculatedScore);
        savedAttempt.setStatus("COMPLETED");
        quizAttemptRepository.save(savedAttempt);

        int pointsEarned = correctAnswersCount;
        long studyTimeMinutes = Duration.between(savedAttempt.getStartTime(), attempt.getEndTime()).toMinutes();

        challengeService.updateQuizCompletionProgress(
                userId,
                correctAnswersCount,
                (int) studyTimeMinutes,
                pointsEarned,
                savedAttempt.getId()
        );

        QuizResultDTO finalResult = new QuizResultDTO();
        finalResult.setAttemptId(savedAttempt.getId());
        finalResult.setScore(calculatedScore);
        finalResult.setTotalQuestions(quiz.getQuestions().size());
        finalResult.setCorrectAnswers(correctAnswersCount);
        finalResult.setResults(questionResults);
        finalResult.setPointsEarned(pointsEarned);
        finalResult.setMaxScore(BigDecimal.TEN);

        return finalResult;
    }

    @Transactional
    public void requestEssayGrading(Long attemptId, Long userId){
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài làm với ID: " + attemptId));

        if(!attempt.getUserId().equals(userId))
            throw new AccessDeniedException("Bạn không có quyền thực hiện hành động này.");

        if(essayGradingRequestRepository.existsByQuizAttemptId(attemptId))
            throw new IllegalStateException("Yêu cầu chấm bài cho bài làm này đã tồn tại.");

        UserRanking userRanking = userRankingRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông tin xếp hạng của người dùng."));

        if(userRanking.getConsumptionPoints() < ESSAY_GRADING_COST)
            throw new InsufficientPointsException("Không đủ điểm để yêu cầu chấm bài. Bạn cần " + ESSAY_GRADING_COST + " điểm.");

        userRanking.setConsumptionPoints(userRanking.getConsumptionPoints() - ESSAY_GRADING_COST);
        userRankingRepository.save(userRanking);

        long essayQuestionCount = attempt.getQuizSubmission().getQuestions().stream()
                .filter(q -> q.getQuestionType() == QuestionType.ESSAY).count();

        EssayGradingRequest essayGradingRequest = new EssayGradingRequest();
        essayGradingRequest.setUserId(userId);
        essayGradingRequest.setTotalEssayQuestions((int) essayQuestionCount);
        essayGradingRequest.setQuizAttempt(attempt);
        essayGradingRequest.setStatus(GradingStatus.PENDING);
        essayGradingRequestRepository.save(essayGradingRequest);
    }
}
