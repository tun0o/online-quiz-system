import React from 'react';
import { subjectDisplayMap, difficultyDisplayMap, getDifficultyColor } from '@/utils/displayMaps';
import { CheckCircle } from 'lucide-react';

export default function SubmissionDetailView({ submission }) {
  if (!submission) {
    return <div className="text-center p-8 text-gray-500">Đang tải chi tiết...</div>;
  }

  const difficultyMap = {
    1: { text: 'Dễ', color: 'text-green-600' },
    2: { text: 'Trung bình', color: 'text-yellow-600' },
    3: { text: 'Khó', color: 'text-red-600' },
  };

  const getQuestionTypeBadge = (questionType) => {
    switch (questionType) {
      case 'MULTIPLE_CHOICE':
        return <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded text-xs font-medium">Trắc nghiệm</span>;
      case 'TRUE_FALSE':
        return <span className="px-2 py-1 bg-green-100 text-green-800 rounded text-xs font-medium">Đúng/Sai</span>;
      case 'ESSAY':
        return <span className="px-2 py-1 bg-purple-100 text-purple-800 rounded text-xs font-medium">Tự luận</span>;
      default:
        return <span className="px-2 py-1 bg-gray-100 text-gray-800 rounded text-xs font-medium">Trắc nghiệm</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header Info */}
      <div>
        <h3 className="text-2xl font-bold text-gray-800">{submission.title}</h3>
        <p className="text-gray-600 mt-1">{submission.description}</p>
        <div className="mt-4 flex flex-wrap gap-x-6 gap-y-2 text-sm text-gray-700">
          <span className="font-medium">Môn học: <span className="font-normal">{subjectDisplayMap[submission.subject] || submission.subject}</span></span>
          {submission.difficultyLevel && (
            <span className="font-medium">Độ khó: <span className={`px-2 py-1 rounded text-xs font-medium ${getDifficultyColor(submission.difficultyLevel)}`}>{difficultyDisplayMap[submission.difficultyLevel]}</span></span>
          )}
          <span className="font-medium">Thời gian: <span className="font-normal">{submission.durationMinutes} phút</span></span>
          <span className="font-medium">Số câu hỏi: <span className="font-normal">{submission.questions?.length || 0}</span></span>
        </div>
      </div>

      <hr />

      {/* Questions List */}
      <div className="space-y-8">
        {submission.questions?.map((question, qIndex) => (
          <div key={question.id || question.clientKey} className="bg-gray-50 p-4 rounded-lg border border-gray-200">
            <div className="flex justify-between items-start mb-3">
              <h4 className="font-semibold text-gray-800">Câu {qIndex + 1}:</h4>
              <div className="flex gap-2 items-center">
                {getQuestionTypeBadge(question.questionType)}
                <span className={`font-medium text-xs ${difficultyMap[question.difficultyLevel]?.color || 'text-gray-500'}`}>
                  {difficultyMap[question.difficultyLevel]?.text || 'Không xác định'}
                </span>
                {question.questionType === 'ESSAY' && (
                  <span className="px-2 py-1 bg-orange-100 text-orange-800 rounded text-xs font-medium">
                    {question.maxScore} điểm
                  </span>
                )}
              </div>
            </div>
            <p className="mb-4 whitespace-pre-wrap">{question.questionText}</p>
            
            {/* Hiển thị đáp án cho câu trắc nghiệm và đúng/sai */}
            {question.questionType !== 'ESSAY' && question.answerOptions && (
              <div className="space-y-2">
                <p className="text-sm font-medium text-gray-700 mb-2">Các đáp án:</p>
                {question.answerOptions.map((option, oIndex) => (
                  <div key={option.id || oIndex} className={`flex items-start gap-3 p-2 rounded border ${ 
                    option.isCorrect ? 'bg-green-100 border-green-300 text-green-900' : 'bg-white border-gray-200' 
                  }`}>
                    {option.isCorrect && <CheckCircle size={18} className="flex-shrink-0 mt-0.5 text-green-600" />}
                    <span className={`font-mono text-sm font-bold ${option.isCorrect ? '' : 'text-gray-500'}`}>
                      {String.fromCharCode(65 + oIndex)}.
                    </span>
                    <p className="flex-1">{option.optionText}</p>
                    {option.isCorrect && <span className="text-xs text-green-600 font-medium">✓ Đáp án đúng</span>}
                  </div>
                ))}
              </div>
            )}

            {/* Hiển thị thông tin câu tự luận */}
            {question.questionType === 'ESSAY' && (
              <div className="space-y-3">
                <div className="flex items-center gap-2">
                  <span className="text-sm font-medium text-gray-700">Điểm tối đa:</span>
                  <span className="px-2 py-1 bg-orange-100 text-orange-800 rounded text-sm font-medium">
                    {question.maxScore} điểm
                  </span>
                </div>
                
                {question.essayGuidelines && (
                  <div>
                    <p className="text-sm font-medium text-gray-700 mb-2">Hướng dẫn trả lời:</p>
                    <div className="p-3 bg-blue-50 border-l-4 border-blue-200 rounded text-sm text-gray-700">
                      <p className="whitespace-pre-wrap">{question.essayGuidelines}</p>
                    </div>
                  </div>
                )}
                
                <div className="p-3 bg-yellow-50 border border-yellow-200 rounded">
                  <p className="text-sm text-yellow-800">
                    <strong>Lưu ý:</strong> Câu hỏi tự luận sẽ được admin chấm điểm thủ công khi có yêu cầu từ người làm bài.
                  </p>
                </div>
              </div>
            )}

            {/* Giải thích */}
            {question.explanation && (
              <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded">
                <h5 className="font-semibold text-sm text-blue-800 mb-1">Giải thích:</h5>
                <p className="text-sm text-blue-700 whitespace-pre-wrap">{question.explanation}</p>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
