import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Plus, Trash2, Save, UploadCloud, X, XCircle, Sigma, FileAudio, Loader2 } from 'lucide-react';
import { toast } from 'react-toastify';
import { quizService } from '@/services/quizService';
import MathEditorModal from '../common/MathEditorModal';

export default function QuizSubmissionForm({ submission, onSuccess, onCancel }) {
  const [formData, setFormData] = useState({
    id: null,
    title: '',
    description: '',
    subject: '',
    difficultyLevel: 'EASY',
    durationMinutes: 60,
    audioUrl: null,
    questions: [createEmptyQuestion()]
  });
  const [loading, setLoading] = useState(false);
  const [isUploadingAudio, setIsUploadingAudio] = useState(false);
  const questionsEndRef = useRef(null);
  const prevQuestionsLength = useRef(formData.questions.length);
  const { submissionId } = useParams();
  const navigate = useNavigate();

  // State for Math Editor Modal
  const [isMathModalOpen, setIsMathModalOpen] = useState(false);
  const [mathInputTarget, setMathInputTarget] = useState(null); // { type, qIndex, oIndex }
  const [initialMathValue, setInitialMathValue] = useState('');
  const textareaRefs = useRef({});


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
        audioUrl: null,
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
      imageUrl: null,
      imageFile: null, // Dùng để lưu file ảnh tạm thời ở client
      audioUrl: null,
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

  const handleQuestionImageChange = (qIndex, file) => {
    if (file && file.type.startsWith('image/')) {
      // Tạo URL tạm thời để xem trước
      const previewUrl = URL.createObjectURL(file);
      const updatedQuestions = [...formData.questions];
      updatedQuestions[qIndex] = {
        ...updatedQuestions[qIndex],
        imageFile: file,
        imageUrl: previewUrl, // Dùng imageUrl để hiển thị preview
      };
      setFormData(prev => ({ ...prev, questions: updatedQuestions }));
    } else if (file) {
      toast.warn('Vui lòng chọn một file ảnh hợp lệ.');
    }
  };

  const handleAudioFileChange = async (file) => {
    if (!file) return;

    // Frontend validation (optional but recommended)
    const allowedTypes = ['audio/mpeg', 'audio/wav', 'audio/mp3'];
    if (!allowedTypes.includes(file.type)) {
      toast.error('Định dạng file không hợp lệ. Chỉ chấp nhận file MP3 hoặc WAV.');
      return;
    }
    const maxSize = 50 * 1024 * 1024; // 50MB
    if (file.size > maxSize) {
      toast.error('Dung lượng file không được vượt quá 50MB.');
      return;
    }

    setIsUploadingAudio(true);
    try {
      const uploadedUrl = await quizService.uploadQuizAudio(file);
      setFormData(prev => ({ ...prev, audioUrl: uploadedUrl }));
      toast.success('Tải file audio thành công!');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Lỗi khi tải file audio.');
    } finally {
      setIsUploadingAudio(false);
    }
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

    // Xử lý tải ảnh lên
    const questionsWithImageUrls = await Promise.all(
      formData.questions.map(async (q) => {
        let finalImageUrl = q.imageUrl;

        // Nếu có file ảnh mới (imageFile), tải nó lên
        if (q.imageFile) {
          try {
            finalImageUrl = await quizService.uploadQuestionImage(q.imageFile);
          } catch (uploadError) {
            toast.error(`Lỗi tải ảnh cho câu hỏi: ${q.questionText.substring(0, 20)}...`);
            throw uploadError; // Dừng quá trình submit
          }
        }

        // eslint-disable-next-line no-unused-vars
        const { clientKey, imageFile, ...questionData } = q;
        return {
          ...questionData,
          imageUrl: finalImageUrl,
        };
      })
    );

    // Chuẩn bị payload cuối cùng
    const payload = { ...formData, questions: questionsWithImageUrls };

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
          audioUrl: null,
          questions: [createEmptyQuestion()]
        });
      }
    } catch (error) {
      toast.error('Có lỗi xảy ra: ' + (error.response?.data?.message || error.message));
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    // Nếu có hàm onCancel được truyền vào (từ ContributorDashboard), gọi nó
    if (onCancel) {
      onCancel();
    } else {
      // Nếu không, quay lại trang trước (dùng cho trang admin)
      navigate(-1);
    }
  };

  const openMathEditor = (type, qIndex, oIndex = null) => {
    let initialValue = '';
    if (type === 'questionText') initialValue = formData.questions[qIndex].questionText;
    if (type === 'explanation') initialValue = formData.questions[qIndex].explanation;
    if (type === 'optionText') initialValue = formData.questions[qIndex].answerOptions[oIndex].optionText;
    if (type === 'essayGuidelines') initialValue = formData.questions[qIndex].essayGuidelines;
    if (type === 'description') initialValue = formData.description;

    setInitialMathValue(initialValue);
    setMathInputTarget({ type, qIndex, oIndex });
    setIsMathModalOpen(true);
  };

  const handleInsertMath = (latex) => {
    const { type, qIndex, oIndex } = mathInputTarget;
    const formula = `$$${latex}$$`; // Wrap in block format

    if (type === 'description') {
      setFormData(prev => ({ ...prev, description: prev.description + formula }));
      return;
    }

    const targetTextareaRef = textareaRefs.current[`${type}_${qIndex}_${oIndex}`];

    if (targetTextareaRef) {
        const start = targetTextareaRef.selectionStart;
        const end = targetTextareaRef.selectionEnd;
        const currentValue = targetTextareaRef.value;
        const newValue = currentValue.substring(0, start) + formula + currentValue.substring(end);

        if (type === 'optionText') {
            updateAnswerOption(qIndex, oIndex, 'optionText', newValue);
        } else {
            updateQuestion(qIndex, type, newValue);
        }
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
          <div className="relative">
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
              rows="3"
              placeholder="Mô tả ngắn về đề thi..."
            />
             <button
                type="button"
                onClick={() => openMathEditor('description')}
                className="absolute bottom-2 right-2 p-1.5 bg-gray-100 text-white rounded-md hover:bg-gray-200"
                title="Chèn công thức"
              >
                <Sigma size={16} />
              </button>
          </div>
        </div>

        {/* Audio Upload Section */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            File Audio cho bài nghe (tùy chọn)
          </label>
          {isUploadingAudio ? (
            <div className="flex items-center gap-2 p-3 border border-gray-200 rounded-lg bg-gray-50">
              <Loader2 className="animate-spin text-green-600" size={20} />
              <span className="text-gray-600">Đang tải lên...</span>
            </div>
          ) : formData.audioUrl ? (
            <div className="p-3 border border-gray-200 rounded-lg bg-gray-50">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2 text-green-700">
                  <FileAudio size={20} />
                  <span className="font-medium">Đã tải lên file audio.</span>
                </div>
                <button
                  type="button"
                  onClick={() => setFormData(prev => ({ ...prev, audioUrl: null }))}
                  className="text-red-600 hover:text-red-800"
                  title="Xóa file audio"
                >
                  <XCircle size={18} />
                </button>
              </div>
              <audio src={formData.audioUrl} controls className="w-full mt-3" />
            </div>
          ) : (
            <label className="w-full flex flex-col items-center px-4 py-6 bg-white text-blue rounded-lg shadow-sm tracking-wide uppercase border border-blue-500 border-dashed cursor-pointer hover:bg-blue-50 hover:text-blue-700">
              <UploadCloud size={24} />
              <span className="mt-2 text-sm leading-normal">Chọn file MP3, WAV (tối đa 50MB)</span>
              <input
                type='file'
                className="hidden"
                accept="audio/mpeg,audio/wav,audio/mp3"
                onChange={(e) => handleAudioFileChange(e.target.files[0])}
              />
            </label>
          )}
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
                  <div className="relative">
                    <textarea
                      ref={el => textareaRefs.current[`questionText_${qIndex}_null`] = el}
                      required
                      value={question.questionText}
                      onChange={(e) => updateQuestion(qIndex, 'questionText', e.target.value)}                      
                      className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
                      rows="2"
                      placeholder="Nhập câu hỏi..."
                    />
                    <button
                      type="button"
                      onClick={() => openMathEditor('questionText', qIndex)}
                      className="absolute bottom-2 right-2 p-1.5 bg-gray-100 text-white rounded-md hover:bg-gray-200"
                      title="Chèn công thức"
                    >
                      <Sigma size={16} />
                    </button>
                  </div>
                </div>

                {/* Image Upload Section */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Hình ảnh (tùy chọn)
                  </label>
                  {question.imageUrl ? (
                    <div className="relative group w-full max-w-md border border-gray-300 rounded-lg p-2 bg-gray-50">
                      <img src={question.imageUrl} alt="Xem trước" className="w-full h-auto max-h-80 object-contain rounded" />
                      <button
                        type="button"
                        onClick={() => updateQuestion(qIndex, 'imageUrl', null)}
                        className="absolute item-center justify-center top-1 right-1 bg-red-600 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                      >
                        <X size={14} />
                      </button>
                    </div>
                  ) : (
                    <label className="w-full flex flex-col items-center px-4 py-6 bg-white text-blue rounded-lg shadow-sm tracking-wide uppercase border border-blue-500 border-dashed cursor-pointer hover:bg-blue-50 hover:text-blue-700">
                      <UploadCloud size={24} />
                      <span className="mt-2 text-sm leading-normal">Chọn ảnh</span>
                      <input
                        type='file'
                        className="hidden"
                        accept="image/*"
                        onChange={(e) => handleQuestionImageChange(qIndex, e.target.files[0])}
                      />
                    </label>
                  )}
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
                    <div className="relative">
                      <textarea
                        ref={el => textareaRefs.current[`essayGuidelines_${qIndex}_null`] = el}
                        value={question.essayGuidelines}
                        onChange={(e) => updateQuestion(qIndex, 'essayGuidelines', e.target.value)}                        
                        className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
                        rows="3"
                        placeholder="VD: Trả lời trong khoảng 200-300 từ, nêu rõ luận điểm và dẫn chứng..."
                      />
                      <button
                        type="button"
                        onClick={() => openMathEditor('essayGuidelines', qIndex)}
                        className="absolute bottom-2 right-2 p-1.5 bg-gray-100 text-white rounded-md hover:bg-gray-200"
                        title="Chèn công thức"
                      >
                        <Sigma size={16} />
                      </button>
                    </div>
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
                        <div className="relative flex-1">
                          <input
                            ref={el => textareaRefs.current[`optionText_${qIndex}_${oIndex}`] = el}
                            type="text"
                            required
                            value={option.optionText}
                            onChange={(e) => {
                              const newOptions = [...question.answerOptions];
                              newOptions[oIndex].optionText = e.target.value;
                              updateQuestion(qIndex, 'answerOptions', newOptions);
                            }}
                            className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"                            
                            placeholder={question.questionType === 'TRUE_FALSE' ?
                              (oIndex === 0 ? 'Đúng' : 'Sai') :
                              `Đáp án ${String.fromCharCode(65 + oIndex)}`
                            }
                            readOnly={question.questionType === 'TRUE_FALSE'}
                          />
                          {question.questionType !== 'TRUE_FALSE' && <button
                            type="button"
                            onClick={() => openMathEditor('optionText', qIndex, oIndex)}
                            className="absolute top-1/2 right-2 -translate-y-1/2 p-1.5 bg-gray-100 text-white rounded-md hover:bg-gray-200"
                            title="Chèn công thức"
                          >
                            <Sigma size={16} />
                          </button>}
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Giải thích (tùy chọn)
                  </label>
                  <div className="relative">
                    <textarea
                      ref={el => textareaRefs.current[`explanation_${qIndex}_null`] = el}
                      value={question.explanation}
                      onChange={(e) => updateQuestion(qIndex, 'explanation', e.target.value)}                      
                      className="w-full p-3 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-500 text-gray-800"
                      rows="2"
                      placeholder="Giải thích đáp án đúng..."
                    />
                    <button
                      type="button"
                      onClick={() => openMathEditor('explanation', qIndex)}
                      className="absolute bottom-2 right-2 p-1.5 bg-gray-100 text-white rounded-md hover:bg-gray-200"
                      title="Chèn công thức"
                    >
                      <Sigma size={16} />
                    </button>
                  </div>
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
        <div className="flex justify-end gap-4">
          {/* Nút Hủy chỉ hiển thị ở chế độ chỉnh sửa */}
          {formData.id && (
            <button
              type="button"
              onClick={handleCancel}
              className="flex items-center gap-2 px-6 py-3 bg-white text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50 transition"
            >
              <XCircle size={16} />
              Hủy
            </button>
          )}
          <button
            type="submit"
            disabled={loading}
            className="flex items-center gap-2 px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700 transition disabled:opacity-50"
          >
            <Save size={16} />
            {loading ? 'Đang xử lý...' : (formData.id ? 'Lưu thay đổi' : 'Gửi đề thi')}
          </button>
        </div>
      </form>

      <MathEditorModal
        isOpen={isMathModalOpen}
        onClose={() => setIsMathModalOpen(false)}
        onInsert={handleInsertMath}
        initialValue={initialMathValue}
      />
    </div>
  );
}
