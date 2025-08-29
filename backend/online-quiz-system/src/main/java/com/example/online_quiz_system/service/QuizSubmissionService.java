package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.AnswerOptionDTO;
import com.example.online_quiz_system.dto.QuestionDTO;
import com.example.online_quiz_system.dto.QuizSubmissionDTO;
import com.example.online_quiz_system.entity.QuizSubmission;
import com.example.online_quiz_system.entity.SubmissionAnswerOption;
import com.example.online_quiz_system.entity.SubmissionQuestion;
import com.example.online_quiz_system.entity.SubmissionStatus;
import com.example.online_quiz_system.repository.QuizSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuizSubmissionService {

    @Autowired
    private QuizSubmissionRepository submissionRepository;

    public QuizSubmission submitQuiz(QuizSubmissionDTO dto, Long contributorId){
        QuizSubmission submission = new QuizSubmission();
        submission.setTitle(dto.getTitle());
        submission.setDescription(dto.getDescription());
        submission.setSubject(dto.getSubject());
        submission.setDurationMinutes(dto.getDurationMinutes());
        submission.setContributorId(contributorId);
        submission.setStatus(SubmissionStatus.PENDING);

        if(dto.getQuestions() != null){
            List<SubmissionQuestion> questions = dto.getQuestions().stream()
                    .map(q -> mapToQuestion(q, submission))
                    .collect(Collectors.toList());
            submission.setQuestions(questions);
        }

        return submissionRepository.save(submission);
    }

    private SubmissionQuestion mapToQuestion(QuestionDTO dto, QuizSubmission submission){
        SubmissionQuestion question = new SubmissionQuestion();
        question.setSubmission(submission);
        question.setQuestionText(dto.getQuestionText());
        question.setQuestionType(dto.getQuestionType());
        question.setCorrectAnswer(dto.getCorrectAnswer());
        question.setExplanation(dto.getExplanation());
        question.setDifficultyLevel(dto.getDifficultyLevel());

        if(dto.getAnswerOptions() != null){
            List<SubmissionAnswerOption> options = dto.getAnswerOptions().stream()
                    .map(o -> mapToAnswerOption(o, question))
                    .collect(Collectors.toList());
            question.setAnswerOptions(options);
        }

        return question;
    }

    private SubmissionAnswerOption mapToAnswerOption(AnswerOptionDTO dto, SubmissionQuestion question){
        SubmissionAnswerOption option = new SubmissionAnswerOption();
        option.setQuestion(question);
        option.setOptionText(dto.getOptionText());
        option.setIsCorrect(dto.getIsCorrect());
        return option;
    }

    @Transactional(readOnly = true)
    public Page<QuizSubmission> getPendingSubmissions(Pageable pageable){
        return submissionRepository.findByStatus(SubmissionStatus.PENDING, pageable);
    }

    @Transactional(readOnly = true)
    public Page<QuizSubmission> getSubmissionsByContributor(Long contributorId, Pageable pageable){
        return submissionRepository.findByContributorId(contributorId, pageable);
    }

    @Transactional(readOnly = true)
    public QuizSubmission getSubmissionById(Long id){
        return submissionRepository.findByIdWithQuestions(id);
    }

    @Transactional
    public QuizSubmission updateSubmission(Long id, QuizSubmissionDTO dto, Long contributorId){
        QuizSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi"));

        if(!submission.getContributorId().equals(contributorId))
            throw new RuntimeException("Bạn không có quyền sửa đề thi này");

        if(!submission.getStatus().equals(SubmissionStatus.PENDING))
            throw new RuntimeException("Chỉ có thể sửa đề thi đang chờ duyệt");

        submission.setTitle(dto.getTitle());
        submission.setDescription(dto.getDescription());
        submission.setSubject(dto.getSubject());
        submission.setDurationMinutes(dto.getDurationMinutes());

        submission.getQuestions().clear();
        if(dto.getQuestions() != null){
            List<SubmissionQuestion> questions = dto.getQuestions().stream()
                    .map(q -> mapToQuestion(q, submission))
                    .collect(Collectors.toList());
            submission.getQuestions().addAll(questions);
        }

        return submissionRepository.save(submission);
    }

    @Transactional
    public void deleteSubmission(Long id, Long contributorId){
        QuizSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi"));

        if(!submission.getContributorId().equals(contributorId))
            throw new RuntimeException("Bạn không có quyền xóa đề thi này");
        if(!submission.getStatus().equals(SubmissionStatus.PENDING))
            throw new RuntimeException("Chỉ có thể xóa đề thi đang chờ duyệt");

        submissionRepository.delete(submission);
    }

    public QuizSubmission approveSubmission(Long id, Long adminId){
        QuizSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi"));

        submission.setStatus(SubmissionStatus.APPROVED);
        submission.setApprovedBy(adminId);
        submission.setApprovedAt(LocalDateTime.now());

        return submissionRepository.save(submission);
    }

    public QuizSubmission rejectSubmission(Long id, String reason, Long adminId){
        QuizSubmission submission = submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đề thi"));
        submission.setStatus(SubmissionStatus.REJECTED);
        submission.setAdminFeedback(reason);
        submission.setApprovedBy(adminId);

        return submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return submissionRepository.countByStatus(SubmissionStatus.PENDING);
    }
}
