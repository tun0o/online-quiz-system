package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.*;
import com.example.online_quiz_system.entity.*;
import com.example.online_quiz_system.enums.GradingStatus;
import com.example.online_quiz_system.enums.QuestionType;
import com.example.online_quiz_system.repository.EssayGradingRequestRepository;
import com.example.online_quiz_system.repository.QuizAttemptRepository;
import com.example.online_quiz_system.repository.QuizSubmissionRepository;
import com.example.online_quiz_system.repository.UserAnswerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        int essayQuestionsCount = 0;
        List<UserAnswer> userAnswersToSave = new ArrayList<>();
        BigDecimal calculatedScore = BigDecimal.ZERO;
        List<QuestionResultDTO> questionResults = new ArrayList<>();

        for(UserAnswerRequestDTO userAnswerDTO : attemptDTO.getAnswers()){
            SubmissionQuestion question = questionMap.get(userAnswerDTO.getQuestionId());
            if(question == null) continue;

            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setQuizAttempt(savedAttempt);
            userAnswer.setQuestion(question);

            QuestionResultDTO questionResult = new QuestionResultDTO();
            questionResult.setQuestionId(question.getId());
            questionResult.setQuestionText(questionResult.getQuestionText());
            questionResult.setUserAnswer(userAnswerDTO);
            questionResult.setExplanation(questionResult.getExplanation());

            if(question.getQuestionType() == QuestionType.MULTIPLE_CHOICE || question.getQuestionType() == QuestionType.TRUE_FALSE){
                SubmissionAnswerOption correctOption = question.getAnswerOptions().stream()
                        .filter(SubmissionAnswerOption::getIsCorrect).findFirst().orElse(null);
                questionResult.setCorrectAnswer(correctOption);

                userAnswer.setSelectedOption(correctOption);
                boolean isCorrect = correctOption != null && Objects.equals(userAnswerDTO.getSelectedOptionId(), correctOption.getId());
                userAnswer.setIsCorrect(isCorrect);
                questionResult.setIsCorrect(isCorrect);
                if(isCorrect){
                    correctAnswersCount++;
                    calculatedScore = calculatedScore.add(scorePerMcq);
                }
            } else if (question.getQuestionType() == QuestionType.ESSAY) {
                userAnswer.setAnswerText(userAnswerDTO.getAnswerText());
                userAnswer.setIsCorrect(null);
                questionResult.setIsCorrect(null);
                essayQuestionsCount++;
            }
            userAnswersToSave.add(userAnswer);
            questionResults.add(questionResult);
        }

        userAnswerRepository.saveAll(userAnswersToSave);

        if(essayQuestionsCount > 0 && attemptDTO.isRequestEssayGrading()){
            EssayGradingRequest gradingRequest = new EssayGradingRequest();
            gradingRequest.setUserId(userId);
            gradingRequest.setQuizAttempt(savedAttempt);
            gradingRequest.setStatus(GradingStatus.PENDING);
            gradingRequest.setTotalEssayQuestions(essayQuestionsCount);
            essayGradingRequestRepository.save(gradingRequest);
        }

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
}
