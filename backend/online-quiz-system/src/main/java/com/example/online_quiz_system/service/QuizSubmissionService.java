package com.example.online_quiz_system.service;

import com.example.online_quiz_system.dto.AnswerOptionDTO;
import com.example.online_quiz_system.dto.QuestionDTO;
import com.example.online_quiz_system.dto.QuizSubmissionDTO;
import com.example.online_quiz_system.entity.*;
import com.example.online_quiz_system.repository.QuizSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    public Page<QuizSubmission> getAllSubmissions(Pageable pageable){
        return submissionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<QuizSubmission> findPublicQuizzes(String keyword, String subject, Pageable pageable){
        Specification<QuizSubmission> spec = isApproved();

        if(StringUtils.hasText(keyword)){
            spec = spec.and(hasKeyword(keyword));
        }
        if(StringUtils.hasText(subject)){
            spec = spec.and(hasSubject(subject));
        }

        return submissionRepository.findAll(spec, pageable);
    }

    private Specification<QuizSubmission> isApproved(){
        return ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), SubmissionStatus.APPROVED));
    }

    private Specification<QuizSubmission> hasKeyword(String keyword){
        return (root, query, criteriaBuilder) -> {
                String likePattern = "%" + keyword.toLowerCase() +"%";
                return criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likePattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern)
                );
        };
    }

    private Specification<QuizSubmission> hasSubject(String subject){
        return (root, query, criteriaBuilder) -> {
            try {
                Subject subjectEnum = Subject.valueOf(subject.toUpperCase());
                return criteriaBuilder.equal(root.get("subject"), subjectEnum.toString());
            } catch (IllegalArgumentException e) {
                return criteriaBuilder.disjunction();
            }
        };
    }

    private QuizSubmissionDTO convertToPublicDTO(QuizSubmission entity) {
        QuizSubmissionDTO dto = new QuizSubmissionDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setSubject(entity.getSubject());
        dto.setDurationMinutes(entity.getDurationMinutes());

        if(entity.getQuestions() != null){
            dto.setQuestions(entity.getQuestions().stream()
                    .map(this::convertQuestionToDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private QuestionDTO convertQuestionToDTO(SubmissionQuestion question){
        QuestionDTO dto = new QuestionDTO();
        return dto;
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
//
//        if(!submission.getContributorId().equals(contributorId))
//            throw new RuntimeException("Bạn không có quyền xóa đề thi này");
//        if(!submission.getStatus().equals(SubmissionStatus.PENDING))
//            throw new RuntimeException("Chỉ có thể xóa đề thi đang chờ duyệt");

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
