import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Plus, Trash2, Save } from 'lucide-react';
import { toast } from 'react-toastify';
import { quizService } from '../../services/quizService';

export default function QuizSubmissionForm({ submission, onSuccess }) {
  const [formData, setFormData] = useState({
    id: null,
    title: '',
    description: '',
    subject: '',
    durationMinutes: 60,
    questions: [createEmptyQuestion()]
  });
  const [loading, setLoading] = useState(false);
  const questionsEndRef = useRef(null);
  const prevQuestionsLength = useRef(formData.questions.length);
  const { submissionId } = useParams();
  const navigate = useNavigate();

  useEffect(() => {
    const loadSubmissionForEdit = async (id) => {
      setLoading(true);
      try {
        const data = await quizService.getSubmissionDetail(id);
        setFormData(data);
      } catch (error) {
        toast.error('Không thể tải dữ liệu đề thi để chỉnh sửa.');
        if (window.location.pathname.startsWith('/admin')) {
          navigate(-1);
        }
      } finally {
        setLoading(false);
      }
    };

    if (submissionId) {
      loadSubmissionForEdit(submissionId);
    } else if (submission) { // For contributor edit mode (not via URL)
      setFormData(JSON.parse(JSON.stringify(submission)));
    } else {
      setFormData({
        title: '',
        description: '',
        subject: '',
        durationMinutes: 60,
        questions: [createEmptyQuestion()]
      });
    }
  }, [submissionId, submission, navigate]);

  useEffect(() => {
    // Nếu một câu hỏi được thêm vào, tự động cuộn xuống cuối danh sách câu hỏi
    if (formData.questions.length > prevQuestionsLength.current) {
      questionsEndRef.current?.scrollIntoView({ behavior: "smooth", block: 'end' });
    }
    // Cập nhật độ dài của mảng câu hỏi cho lần render tiếp theo
    prevQuestionsLength.current = formData.questions.length;
  }, [formData.questions]);


  const handleChange = (e) => {
    const { name, value, type } = e.target;
    setFormData(prev => ({
      ...prev,
      // Chuyển đổi giá trị cho input number một cách an toàn
      [name]: type === 'number' ? parseInt(value, 10) || 0 : value
    }));
  };

  function createEmptyQuestion() {
    return {
      // Key tạm thời phía client để React nhận diện chính xác các câu hỏi khi thêm/xóa
      clientKey: `q_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`,
      questionText: '',
      questionType: 'MULTIPLE_CHOICE',
      explanation: '',
      difficultyLevel: 1,
      answerOptions: [
        { optionText: '', isCorrect: false },
        { optionText: '', isCorrect: false },
        { optionText: '', isCorrect: false },
        { optionText: '', isCorrect: false }
      ]
    };
  }

  const addQuestion = () => {
    setFormData(prev => ({
      ...prev,
      questions: [...prev.questions, createEmptyQuestion()]
    }));
  };

  const removeQuestion = (index) => {
    setFormData(prev => ({
      ...prev,
      questions: prev.questions.filter((_, i) => i !== index)
    }));
  };

  const updateQuestion = (index, field, value) => {
    setFormData(prev => ({
      ...prev,
      questions: prev.questions.map((q, i) =>
        i === index ? { ...q, [field]: value } : q
      )
    }));
  };

  const updateAnswerOption = (questionIndex, optionIndex, field, value) => {
    setFormData(prev => ({
      ...prev,
      questions: prev.questions.map((q, i) =>
        i === questionIndex ? {
          ...q,
          answerOptions: q.answerOptions.map((opt, j) =>
            j === optionIndex ? { ...opt, [field]: value } : opt
          )
        } : q
      )
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    // Validation: mỗi câu hỏi phải có ít nhất 1 đáp án đúng
    const allQuestionsHaveCorrect = formData.questions.every(q =>
      q.answerOptions.some(a => a.isCorrect)
    );

    if (!allQuestionsHaveCorrect) {
      toast.warn("Vui lòng chọn ít nhất một đáp án đúng cho mỗi câu hỏi!");
      setLoading(false);
      return;
    }

    try {
      if (formData.id) { // Check if we are editing (ID is present)
        await quizService.updateSubmission(formData.id, formData);
        toast.success('Đề thi đã được cập nhật thành công!');
        onSuccess?.();
      } else {
        await quizService.submitQuiz(formData);
        toast.success('Đề thi đã được gửi thành công!');
        onSuccess?.();

        // Reset form
        setFormData({
          title: '',
          description: '',
          subject: '',
          durationMinutes: 60,
          questions: [createEmptyQuestion()]
        });
      }

    } catch (error) {
      toast.error('Có lỗi xảy ra: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };


  return (
    <div className="bg-white rounded-lg p-6 shadow-sm border border-gray-200">
      <h2 className="text-xl font-semibold mb-6 text-gray-800">{formData.id ? 'Chỉnh sửa đề thi' : 'Đóng góp đề thi mới'}</h2>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Basic Info */}
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Tiêu đề đề thi *
            </label>
            <input
              type="text"
              required
              name="title"
              value={formData.title}
              onChange={handleChange}
              className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
              placeholder="VD: Ôn luyện đạo hàm cơ bản"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Môn học *
            </label>
            <select
              required
              name="subject"
              value={formData.subject}
              onChange={handleChange}
              className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
            >
              <option value="">Chọn môn học</option>
              <option value="MATH">Toán học</option>
              <option value="PHYSICS">Vật lý</option>
              <option value="CHEMISTRY">Hóa học</option>
              <option value="BIOLOGY">Sinh học</option>
              <option value="LITERATURE">Ngữ văn</option>
              <option value="ENGLISH">Tiếng Anh</option>
            </select>
          </div>
        </div>

        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Thời gian làm bài (phút) *
            </label>
            <input
              type="number"
              required
              min="1"
              name="durationMinutes"
              value={formData.durationMinutes}
              onChange={handleChange}
              className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
            />
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Mô tả
          </label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleChange}
            className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
            rows="3"
            placeholder="Mô tả ngắn về đề thi..."
          />
        </div>

        {/* Questions */}
        <div>
          <div className="flex justify-between items-center mb-4">
            <h3 className="text-lg font-medium text-gray-800">
              Câu hỏi ({formData.questions.length})
            </h3>
          </div>

          {formData.questions.map((question, qIndex) => (
            <div key={question.id || question.clientKey} className="border border-gray-200 rounded-lg p-4 mb-4">
              <div className="flex justify-between items-start mb-4">
                <h4 className="font-medium text-gray-800">Câu hỏi {qIndex + 1}</h4>
                {formData.questions.length > 1 && (
                  <button
                    type="button"
                    onClick={() => removeQuestion(qIndex)}
                    className="text-red-600 hover:text-red-700"
                  >
                    <Trash2 size={16} />
                  </button>
                )}
              </div>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Nội dung câu hỏi *
                  </label>
                  <textarea
                    required
                    value={question.questionText}
                    onChange={(e) => updateQuestion(qIndex, 'questionText', e.target.value)}
                    className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                    rows="2"
                    placeholder="Nhập câu hỏi..."
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Độ khó
                    </label>
                    <select
                      value={question.difficultyLevel}
                      onChange={(e) => updateQuestion(qIndex, 'difficultyLevel', parseInt(e.target.value))}
                      className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                    >
                      <option value={1}>Dễ</option>
                      <option value={2}>Trung bình</option>
                      <option value={3}>Khó</option>
                    </select>
                  </div>
                </div>

                {/* Answer Options */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Các đáp án
                  </label>
                  {question.answerOptions.map((option, oIndex) => (
                    <div key={oIndex} className="flex items-center gap-3 mb-2">
                      <input
                        type="radio"
                        name={`correct-${qIndex}`}
                        checked={option.isCorrect}
                        onChange={() => {
                          // Set only this option as correct
                          const newOptions = question.answerOptions.map((opt, i) => ({
                            ...opt,
                            isCorrect: i === oIndex
                          }));
                          updateQuestion(qIndex, 'answerOptions', newOptions);
                        }}
                        className="text-green-600"
                      />
                      <input
                        type="text"
                        required
                        value={option.optionText}
                        onChange={(e) => updateAnswerOption(qIndex, oIndex, 'optionText', e.target.value)}
                        className="flex-1 p-2 border border-gray-200 rounded focus:outline-none focus:ring-2 focus:ring-green-500"
                        placeholder={`Đáp án ${String.fromCharCode(65 + oIndex)}`}
                      />
                    </div>
                  ))}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Giải thích (tùy chọn)
                  </label>
                  <textarea
                    value={question.explanation}
                    onChange={(e) => updateQuestion(qIndex, 'explanation', e.target.value)}
                    className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500"
                    rows="2"
                    placeholder="Giải thích đáp án đúng..."
                  />
                </div>
              </div>
            </div>
          ))}

          <div ref={questionsEndRef} />

          <div className="flex justify-start mt-4">
            <button
              type="button"
              onClick={addQuestion}
              className="flex items-center gap-2 px-4 py-2 bg-white text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
            >
              <Plus size={16} />
              Thêm câu hỏi
            </button>
          </div>

        </div>

        {/* Submit Button */}


        <div className="flex justify-end">
          <button
            type="submit"
            disabled={loading}
            className="flex items-center gap-2 px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition disabled:opacity-50"
          >
            <Save size={16} />
            {loading ? 'Đang gửi...' : 'Gửi đề thi'}
          </button>
        </div>
      </form>
    </div>
  );
}
