import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { gradingService } from '@/services/gradingService';
import { Loader2, CheckCircle, ArrowLeft } from 'lucide-react';

export default function GradingDetailPage() {
  const { attemptId } = useParams();
  const navigate = useNavigate();

  const [details, setDetails] = useState(null);
  const [grades, setGrades] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const fetchDetails = async () => {
      try {
        const data = await gradingService.getAttemptDetails(attemptId);
        setDetails(data);
        const initialGrades = data.essayQuestions.reduce((acc, q) => {
          acc[q.userAnswerId] = { score: '', feedback: '' };
          return acc;
        }, {});
        setGrades(initialGrades);
      } catch (error) {
        toast.error("Không thể tải chi tiết bài làm.");
        navigate('/admin/grading');
      } finally {
        setLoading(false);
      }
    };
    fetchDetails();
  }, [attemptId, navigate]);

  const handleGradeChange = (userAnswerId, field, value) => {
    setGrades(prev => ({
      ...prev,
      [userAnswerId]: {
        ...prev[userAnswerId],
        [field]: value,
      },
    }));
  };

  const handleSubmit = async () => {
    setSubmitting(true);

    for (const q of details.essayQuestions) {
      const grade = grades[q.userAnswerId];
      const score = Number(grade.score);
      if (grade.score === '' || isNaN(score) || score < 0 || score > q.maxScore) {
        toast.error(`Điểm cho câu hỏi "${q.questionText.substring(0, 20)}..." không hợp lệ. Phải là số từ 0 đến ${q.maxScore}.`);
        setSubmitting(false);
        return;
      }
    }

    const payload = {
      attemptId: Number(attemptId),
      grades: Object.entries(grades).map(([userAnswerId, grade]) => ({
        userAnswerId: Number(userAnswerId),
        score: Number(grade.score),
        feedback: grade.feedback,
      })),
    };

    try {
      await gradingService.submitGrades(payload);
      toast.success("Chấm bài thành công!");
      navigate('/admin/grading');
    } catch (error) {
      toast.error("Có lỗi xảy ra khi gửi điểm: " + error.message);
      console.error("Failed to submit grades:", error);
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="flex items-center justify-center h-64"><Loader2 className="animate-spin text-gray-500" size={32} /></div>;
  }

  if (!details) {
    return <div className="text-center text-gray-500">Không tìm thấy thông tin bài làm.</div>;
  }

  return (
    <div className="space-y-6">
      <button onClick={() => navigate(-1)} className="flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900">
        <ArrowLeft size={16} /> Quay lại danh sách
      </button>

      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <h1 className="text-2xl font-bold text-gray-800">{details.quizTitle}</h1>
        <p className="text-gray-600">Bài làm của User ID: <span className="font-medium">{details.userId}</span></p>
      </div>

      <div className="space-y-6">
        {details.essayQuestions.map((q, index) => (
          <div key={q.userAnswerId} className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
            <h3 className="font-bold text-lg text-gray-800 mb-2">Câu {index + 1}: {q.questionText}</h3>
            {q.essayGuidelines && <div className="mb-4 p-3 bg-blue-50 border-l-4 border-blue-300 text-sm text-blue-800">{q.essayGuidelines}</div>}
            
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">Câu trả lời của người dùng:</label>
              <div className="p-4 bg-gray-50 text-gray-800 border border-gray-200 rounded-md whitespace-pre-wrap">{q.userAnswerText || <i className="text-gray-400">Không trả lời</i>}</div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="md:col-span-2">
                <label htmlFor={`feedback-${q.userAnswerId}`} className="block text-sm font-medium text-gray-700">Phản hồi</label>
                <textarea id={`feedback-${q.userAnswerId}`} rows="3" value={grades[q.userAnswerId]?.feedback} onChange={(e) => handleGradeChange(q.userAnswerId, 'feedback', e.target.value)} className="mt-1 block w-full text-gray-700 rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm" placeholder="Nhận xét về câu trả lời..."></textarea>
              </div>
              <div>
                <label htmlFor={`score-${q.userAnswerId}`} className="block text-sm font-medium text-gray-700">Điểm (Tối đa: {q.maxScore})</label>
                <input type="number" id={`score-${q.userAnswerId}`} value={grades[q.userAnswerId]?.score} onChange={(e) => handleGradeChange(q.userAnswerId, 'score', e.target.value)} min="0" max={q.maxScore} step="0.5" className="mt-1 block w-full text-gray-700 rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 sm:text-sm" placeholder="Nhập điểm" />
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="flex justify-end mt-6">
        <button onClick={handleSubmit} disabled={submitting || loading} className="inline-flex items-center justify-center gap-2 px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:bg-gray-400 disabled:cursor-not-allowed">
          {submitting ? <Loader2 className="animate-spin" size={20} /> : <CheckCircle size={20} />}
          Hoàn tất chấm điểm
        </button>
      </div>
    </div>
  );
}