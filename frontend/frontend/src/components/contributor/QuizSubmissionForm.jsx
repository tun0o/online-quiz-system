import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Plus, Trash2, Save } from 'lucide-react';
import { toast } from 'react-toastify';
import { quizService } from '@/services/quizService';

export default function QuizSubmissionForm({ submission, onSuccess }) {
  const [formData, setFormData] = useState({
    id: null,
    title: '',
    description: '',
    subject: '',
    difficultyLevel: 'EASY',
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
        difficultyLevel: 'EASY',
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
      clientKey: `q_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`,
      questionText: '',
      questionType: 'MULTIPLE_CHOICE',
      explanation: '',
      maxScore: 10.0,
      essayGuidelines: '',
      answerOptions: [
        { id: null, optionText: '', isCorrect: false },
        { id: null, optionText: '', isCorrect: false },
        { id: null, optionText: '', isCorrect: false },
        { id: null, optionText: '', isCorrect: false }
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

    // Validation cho từng loại câu hỏi
    const validationErrors = [];

    formData.questions.forEach((q, index) => {
      if (q.questionType === 'ESSAY') {
        // Câu tự luận không cần đáp án
        return;
      } else {
        // Câu trắc nghiệm và đúng/sai phải có ít nhất 1 đáp án đúng
        const hasCorrectAnswer = q.answerOptions.some(a => a.isCorrect);
        if (!hasCorrectAnswer) {
          validationErrors.push(`Câu hỏi ${index + 1}: Vui lòng chọn đáp án đúng`);
        }
      }
    });

    if (validationErrors.length > 0) {
      toast.warn(validationErrors.join('\n'));
      setLoading(false);
      return;
    }

    // Chuẩn bị dữ liệu để gửi đi: loại bỏ các trường chỉ dùng ở client (như clientKey)
    const payload = {
      ...formData,
      questions: formData.questions.map(q => {
        // Loại bỏ clientKey và difficultyLevel khỏi mỗi câu hỏi
        // eslint-disable-next-line no-unused-vars
        const { clientKey, difficultyLevel, ...questionData } = q;
        return questionData;
      })
    };

    try {
      if (formData.id) {
        await quizService.updateSubmission(formData.id, payload);
        toast.success('Đề thi đã được cập nhật thành công!');
        onSuccess?.();
      } else {
        await quizService.createSubmission(payload);
        toast.success('Đề thi đã được gửi thành công!');
        onSuccess?.();

        // Reset form
        setFormData({
          title: '',
          description: '',
          subject: '',
          difficultyLevel: 'EASY',
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
              className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
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
              className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
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
              Độ khó *
            </label>
            <select
              required
              name="difficultyLevel"
              value={formData.difficultyLevel}
              onChange={handleChange}
              className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
            >
              <option value="EASY">Dễ</option>
              <option value="MEDIUM">Trung bình</option>
              <option value="HARD">Khó</option>
            </select>
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
                className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
              />
            </div>
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
            className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
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
                    className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
                    rows="2"
                    placeholder="Nhập câu hỏi..."
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Loại câu hỏi
                    </label>
                    <select
                      value={question.questionType}
                      onChange={(e) => {
                        const newType = e.target.value;
                        let newOptions = question.answerOptions;

                        // Cập nhật answerOptions dựa trên loại câu hỏi
                        if (newType === 'TRUE_FALSE') {
                          newOptions = [
                            { id: null, optionText: 'Đúng', isCorrect: false, clientKey: `ans_${Date.now()}_1` },
                            { id: null, optionText: 'Sai', isCorrect: false, clientKey: `ans_${Date.now()}_2` }
                          ];
                        } else if (newType === 'ESSAY') {
                          newOptions = [];
                        } else if (newType === 'MULTIPLE_CHOICE' && question.answerOptions.length < 4) {
                          newOptions = [
                            { id: null, optionText: '', isCorrect: false, clientKey: `ans_${Date.now()}_3` },
                            { id: null, optionText: '', isCorrect: false, clientKey: `ans_${Date.now()}_4` },
                            { id: null, optionText: '', isCorrect: false, clientKey: `ans_${Date.now()}_5` },
                            { id: null, optionText: '', isCorrect: false, clientKey: `ans_${Date.now()}_6` }
                          ];
                        }

                        updateQuestion(qIndex, 'questionType', newType);
                        updateQuestion(qIndex, 'answerOptions', newOptions);
                      }}
                      className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
                    >
                      <option value="MULTIPLE_CHOICE">Trắc nghiệm</option>
                      <option value="TRUE_FALSE">Đúng/Sai</option>
                      <option value="ESSAY">Tự luận</option>
                    </select>
                  </div>

                </div>

                {/* Điểm tối đa cho câu tự luận */}
                {question.questionType === 'ESSAY' && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Điểm tối đa
                    </label>
                    <input
                      type="number"
                      min="1"
                      max="100"
                      step="0.5"
                      value={question.maxScore}
                      onChange={(e) => updateQuestion(qIndex, 'maxScore', parseFloat(e.target.value) || 10)}
                      className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
                    />
                  </div>
                )}

                {/* Hướng dẫn cho câu tự luận */}
                {question.questionType === 'ESSAY' && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Hướng dẫn trả lời (tùy chọn)
                    </label>
                    <textarea
                      value={question.essayGuidelines}
                      onChange={(e) => updateQuestion(qIndex, 'essayGuidelines', e.target.value)}
                      className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
                      rows="3"
                      placeholder="VD: Trả lời trong khoảng 200-300 từ, nêu rõ luận điểm và dẫn chứng..."
                    />
                  </div>
                )}

                {/* Answer Options - chỉ hiển thị cho MULTIPLE_CHOICE và TRUE_FALSE */}
                {question.questionType !== 'ESSAY' && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Các đáp án
                    </label>
                    {question.answerOptions.map((option, oIndex) => (
                      <div key={option.id || option.clientKey || oIndex} className="flex items-center gap-3 mb-2">
                        <input
                          type="radio"
                          name={`correct-${qIndex}`}
                          checked={option.isCorrect}
                          onChange={() => {
                            const newOptions = question.answerOptions.map((opt, i) => ({
                              ...opt,
                              isCorrect: i === oIndex
                            }));
                            updateQuestion(qIndex, 'answerOptions', newOptions);
                          }}
                          className="text-green-600 focus:ring-green-500"
                        />
                        <input
                          type="text"
                          required
                          value={option.optionText}
                          onChange={(e) => {
                            const newOptions = [...question.answerOptions];
                            newOptions[oIndex].optionText = e.target.value;
                            updateQuestion(qIndex, 'answerOptions', newOptions);
                          }}
                          className="flex-1 p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
                          placeholder={question.questionType === 'TRUE_FALSE' ?
                            (oIndex === 0 ? 'Đúng' : 'Sai') :
                            `Đáp án ${String.fromCharCode(65 + oIndex)}`
                          }
                          readOnly={question.questionType === 'TRUE_FALSE'}
                        />
                      </div>
                    ))}
                  </div>
                )}

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Giải thích (tùy chọn)
                  </label>
                  <textarea
                    value={question.explanation}
                    onChange={(e) => updateQuestion(qIndex, 'explanation', e.target.value)}
                    className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
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
              className="flex items-center gap-2 px-4 py-2 bg-white text-white border border-gray-300 rounded-lg hover:bg-gray-50 transition"
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
