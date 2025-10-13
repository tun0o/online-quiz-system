import { useState, useEffect, useCallback, useMemo, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { toast } from 'react-toastify';
import { Loader2, Clock, Check, X, Send, ArrowLeft, ArrowRight, Info, LogOut } from "lucide-react";
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts';
import { subjectDisplayMap, difficultyDisplayMap, getDifficultyColor } from "@/utils/displayMaps";
import { quizService } from "@/services/quizService";
import ConfirmationModal from "@/components/common/ConfirmationModal";

export default function QuizTakingPage() {
  const { quizId } = useParams();
  const navigate = useNavigate();

  // State management
  const [quiz, setQuiz] = useState(null);
  const [userAnswers, setUserAnswers] = useState({});
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [attemptId, setAttemptId] = useState(null);
  const [timeLeft, setTimeLeft] = useState(null);
  const [quizState, setQuizState] = useState('LOADING'); // LOADING, IN_PROGRESS, SUBMITTING, COMPLETED
  const [result, setResult] = useState(null);
  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [isExitModalOpen, setIsExitModalOpen] = useState(false);
  const [isGradingRequested, setIsGradingRequest] = useState(false);
  const hasStartedAttempt = useRef(false); // Ref để theo dõi việc gọi API

  // Memoize to check if the quiz has essay questions
  const hasEssayQuestions = useMemo(() => {
    return quiz?.questions?.some(q => q.questionType === 'ESSAY') ?? false;
  }, [quiz]);

  const handleConfirmSubmit = useCallback(async () => {
    setIsConfirmModalOpen(false);
    setQuizState('SUBMITTING');

    const payload = {
      // quizId is now inferred from attemptId on the backend
      answers: Object.entries(userAnswers).map(([questionId, answer]) => {
        const question = quiz.questions.find(q => q.id.toString() === questionId);
        if (question.questionType === 'ESSAY') {
          return { questionId: Number(questionId), answerText: answer };
        } else {
          return { questionId: Number(questionId), selectedOptionId: Number(answer) };
        }
      })
    };

    try {
      const response = await quizService.submitAttempt(attemptId, payload);
      setResult(response);
      setQuizState('COMPLETED');
      toast.success(`Nộp bài thành công! Bạn nhận được ${response.pointsEarned} điểm.`);
    } catch (error) {
      if (error.response && error.response.data.message.includes("Không đủ điểm")) {
        toast.error(error.response.data.message);
      } else {
        toast.error("Có lỗi xảy ra khi nộp bài. Vui lòng thử lại.");
      }
      console.error(error);
      setQuizState('IN_PROGRESS');
    }
  }, [quiz, userAnswers, attemptId]);

  const handleConfirmExit = useCallback(() => {
    setIsExitModalOpen(false);
    toast.info("Bạn đã thoát khỏi bài làm.");
    navigate('/');
  }, [navigate]);

  // Fetch quiz data
  useEffect(() => {
    const loadQuiz = async () => {
      // Ngăn chặn việc gọi API lần thứ hai do StrictMode
      if (hasStartedAttempt.current) return;
      hasStartedAttempt.current = true;

      try {
        // Step 1: Start the attempt and get quiz data + attemptId
        const startResponse = await quizService.startAttempt(quizId);
        const { quizData, attemptId: newAttemptId } = startResponse;

        setQuiz(quizData);
        setAttemptId(newAttemptId);
        if (quizData.durationMinutes) {
          setTimeLeft(quizData.durationMinutes * 60);
        }
        setQuizState('IN_PROGRESS');
      } catch (error) {
        toast.error("Không thể tải đề thi. Vui lòng thử lại.");
        console.error(error);
        navigate('/');
      }
    };
    loadQuiz();
  }, [quizId, navigate]);

  // Timer effect
  useEffect(() => {
    if (quizState !== 'IN_PROGRESS' || timeLeft === null) return;

    if (timeLeft <= 0) {
      toast.warn("Hết giờ! Bài làm của bạn sẽ được nộp tự động.");
      handleConfirmSubmit(); // Auto-submit when time is up
      return;
    }

    const timerId = setInterval(() => {
      setTimeLeft(prevTime => prevTime - 1);
    }, 1000);

    return () => clearInterval(timerId);
  }, [timeLeft, quizState, handleConfirmSubmit]);

  // Warn user before leaving page
  useEffect(() => {
    const handleBeforeUnload = (event) => {
      if (quizState === 'IN_PROGRESS') {
        event.preventDefault();
        event.returnValue = ''; // Required for Chrome
      }
    };

    window.addEventListener('beforeunload', handleBeforeUnload);
    return () => {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    };
  }, [quizState]);

  // Handlers
  const handleAnswerChange = (questionId, value) => {
    setUserAnswers(prev => ({ ...prev, [questionId]: value }));
  };

  const handleNextQuestion = () => {
    if (currentQuestionIndex < quiz.questions.length - 1) {
      setCurrentQuestionIndex(prev => prev + 1);
    }
  };

  const handlePrevQuestion = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex(prev => prev - 1);
    }
  };

  const handleSubmitRequest = () => {
    setIsConfirmModalOpen(true);
  };

  const jumpToQuestion = (index) => {
    setCurrentQuestionIndex(index);
  };

  // Render functions for different states
  const renderLoading = () => (
    <div className="flex flex-col items-center justify-center h-96 bg-white rounded-lg shadow-sm">
      <Loader2 className="animate-spin text-green-600" size={48} />
      <p className="mt-4 text-lg text-gray-600">Đang tải đề thi...</p>
    </div>
  );

  const renderInProgress = () => {
    if (!quiz) return null;
    const currentQuestion = quiz.questions[currentQuestionIndex];
    const formatTime = (seconds) => {
      const mins = Math.floor(seconds / 60);
      const secs = seconds % 60;
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    };

    return (
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Panel: Info, Palette & Exit */}
        <div className="lg:col-span-1 bg-white rounded-lg shadow-sm border border-gray-200 p-6 self-start">
          <h2 className="text-xl font-bold text-gray-800 mb-1">{quiz.title}</h2>
          <p className="text-sm text-gray-600 mb-4">{quiz.description}</p>

          <div className="space-y-2 text-sm text-gray-600 border-t border-b border-gray-200 py-4 mb-4">
            <div className="flex justify-between"><span>Môn học:</span> <span className="font-medium">{subjectDisplayMap[quiz.subject] || quiz.subject}</span></div>
            <div className="flex justify-between"><span>Độ khó:</span> <span className={`px-2 py-0.5 rounded text-xs font-medium ${getDifficultyColor(quiz.difficultyLevel)}`}>{difficultyDisplayMap[quiz.difficultyLevel]}</span></div>
            <div className="flex justify-between"><span>Số câu hỏi:</span> <span className="font-medium">{quiz.questions.length}</span></div>
            <div className="flex justify-between"><span>Thời gian:</span> <span className="font-medium">{quiz.durationMinutes} phút</span></div>
          </div>

          <h3 className="font-semibold mb-3">Câu hỏi</h3>
          <div className="grid grid-cols-5 gap-2">
            {quiz.questions.map((q, index) => (
              <button
                key={q.id}
                onClick={() => jumpToQuestion(index)}
                className={`h-10 w-10 rounded flex items-center justify-center font-medium border transition ${index === currentQuestionIndex
                  ? 'bg-green-600 text-white border-green-600 ring-2 ring-green-300'
                  : userAnswers[q.id]
                    ? 'bg-blue-100 text-blue-800 border-blue-200'
                    : 'bg-gray-100 text-gray-700 border-gray-200 hover:bg-gray-200'
                  }`}
              >
                {index + 1}
              </button>
            ))}
          </div>

          <div className="mt-8 border-t border-gray-200 pt-6">
            <button
              onClick={() => setIsExitModalOpen(true)}
              className="w-full flex items-center justify-center gap-2 px-4 py-2 bg-white border border-gray-300 text-white rounded-lg hover:bg-gray-50 hover:border-gray-400 transition"
            >
              <LogOut size={16} />
              Thoát
            </button>
          </div>
        </div>

        {/* Right Panel: Question */}
        <div className="lg:col-span-2 bg-white rounded-lg shadow-sm border border-gray-200">
          {/* Header */}
          <div className="p-6 border-b border-gray-200">
            <div className="flex justify-between items-center">
              <div className="text-lg font-medium text-gray-700">
                Câu {currentQuestionIndex + 1} / {quiz.questions.length}
              </div>
              {timeLeft !== null && (
                <div className="flex items-center gap-2 px-3 py-1 bg-red-100 text-red-700 rounded-full text-sm font-bold">
                  <Clock size={16} />
                  <span>{formatTime(timeLeft)}</span>
                </div>
              )}
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2 mt-2">
              <div className="bg-green-600 h-2 rounded-full" style={{ width: `${((Object.keys(userAnswers).length) / quiz.questions.length) * 100}%` }}></div>
            </div>
          </div>

          {/* Question Body */}
          <div className="p-6 min-h-[300px]">
            <h2 className="text-xl font-semibold mb-6 text-gray-800">{currentQuestion.questionText}</h2>
            {renderAnswerOptions(currentQuestion)}
          </div>

          {/* Footer/Navigation */}
          <div className="p-6 border-t border-gray-200 flex justify-between items-center">
            <button onClick={handlePrevQuestion} disabled={currentQuestionIndex === 0} className="flex items-center gap-2 px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed">
              <ArrowLeft size={16} /> Câu trước
            </button>
            <div className="flex items-center gap-4">

              {currentQuestionIndex === quiz.questions.length - 1 ? (
                <button onClick={handleSubmitRequest} className="flex items-center gap-2 px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 shadow-sm">
                  <Send size={16} /> Nộp bài
                </button>
              ) : (
                <button onClick={handleNextQuestion} className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700">
                  Câu sau <ArrowRight size={16} />
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    );
  };

  const renderAnswerOptions = (question) => {
    const answer = userAnswers[question.id];
    switch (question.questionType) {
      case 'MULTIPLE_CHOICE':
      case 'TRUE_FALSE':
        return (
          <div className="space-y-3">
            {question.answerOptions.map(option => (
              <label key={option.id} className={`flex items-center p-4 border rounded-lg cursor-pointer transition ${answer == option.id ? 'bg-green-50 border-green-400 shadow-sm' : 'bg-white border-gray-200 hover:border-gray-400'}`}>
                <input type="radio" name={`question-${question.id}`} value={option.id} checked={answer == option.id} onChange={(e) => handleAnswerChange(question.id, e.target.value)} className="h-4 w-4 text-green-600 border-gray-300 focus:ring-green-500" />
                <span className="ml-3 text-gray-700">{option.optionText}</span>
              </label>
            ))}
          </div>
        );
      case 'ESSAY':
        return (
          <div>
            <textarea
              value={answer || ''}
              onChange={(e) => handleAnswerChange(question.id, e.target.value)}
              rows="8"
              className="w-full p-3 text-gray-600 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
              placeholder="Nhập câu trả lời của bạn..."
            ></textarea>
          </div>
        );
      default:
        return null;
    }
  };

  const renderCompleted = () => {
    if (!result) return null;

    const score = result.score || 0;
    const maxScore = result.maxScore || 10;
    const scorePercentage = maxScore > 0 ? (score / maxScore) * 100 : 0;

    const chartData = [
      { name: 'Score', value: score },
      { name: 'Remaining', value: Math.max(0, maxScore - score) },
    ];
    const chartColors = ['#10B981', '#E5E7EB']; // green-500, gray-200

    const handleRequestGrading = async () => {
      try {
        await quizService.requestEssayGrading(result.attemptId);
        toast.success("Yêu cầu chấm bài đã được gửi thành công!");
        setIsGradingRequest(true);
      } catch (error) {
        if (error.response && error.response.data?.message) {
          toast.error(error.response.data.message);
        } else {
          toast.error("Có lỗi xảy ra khi gửi yêu cầu chấm bài. Vui lòng thử lại.");
        }
      }
    };

    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-8">
        <h1 className="text-3xl font-bold text-center text-green-600">Kết quả bài làm</h1>

        {/* Summary */}
        <div className="mt-8 flex flex-col md:flex-row items-center justify-center gap-8">
          <div className="relative w-48 h-48">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={chartData} dataKey="value" cx="50%" cy="50%" innerRadius={60} outerRadius={80} startAngle={90} endAngle={-270} paddingAngle={0} cornerRadius={5}>
                  {chartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={chartColors[index % chartColors.length]} stroke="none" />
                  ))}
                </Pie>
              </PieChart>
            </ResponsiveContainer>
            <div className="absolute inset-0 flex flex-col items-center justify-center">
              <span className="text-4xl font-bold text-gray-800">{score.toFixed(1)}</span>
              <span className="text-gray-500">/ {maxScore}</span>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4 text-center">
            <div className="bg-green-50 p-4 rounded-lg border border-green-200"><p className="text-sm text-green-800">Câu đúng</p><p className="text-2xl font-bold text-green-900 mt-1">{result.correctAnswers}</p></div>
            <div className="bg-red-50 p-4 rounded-lg border border-red-200"><p className="text-sm text-red-800">Câu sai</p><p className="text-2xl font-bold text-red-900 mt-1">{result.totalQuestions - result.correctAnswers - (result.results.filter(r => r.isCorrect === null).length)}</p></div>
            <div className="bg-blue-50 p-4 rounded-lg border border-blue-200"><p className="text-sm text-blue-800">Tỷ lệ đúng</p><p className="text-2xl font-bold text-blue-900 mt-1">{scorePercentage.toFixed(0)}%</p></div>
            <div className="bg-yellow-50 p-4 rounded-lg border border-yellow-200"><p className="text-sm text-yellow-800">Điểm thưởng</p><p className="text-2xl font-bold text-yellow-900 mt-1">+{result.pointsEarned}</p></div>
          </div>
        </div>

        {result.results.some(r => r.isCorrect === null) && (
          isGradingRequested ? (
            <div className="mt-6 p-4 bg-indigo-50 text-indigo-800 rounded-lg border border-indigo-200 flex items-center gap-3">
              <Info size={20} />
              <span>Yêu cầu chấm bài đã được gửi. Điểm số cuối cùng sẽ được cập nhật sau khi admin chấm xong.</span>
            </div>
          ) : (
            <div className="mt-8 p-6 bg-yellow-50 border-2 border-dashed border-yellow-300 rounded-lg text-center">
              <h3 className="text-lg font-semibold text-yellow-900">Bạn có câu hỏi tự luận chưa được chấm!</h3>
              <p className="text-yellow-800 mt-2">
                Yêu cầu admin chấm bài để hoàn thiện điểm số của bạn.
              </p>
              <button onClick={handleRequestGrading} className="mt-4 px-6 py-2 bg-yellow-500 text-white font-bold rounded-lg hover:bg-yellow-600 transition shadow-md">
                Yêu cầu chấm bài (Chi phí: 100 điểm)
              </button>
            </div>
          )
        )}

        {/* Detailed Results */}
        <div className="mt-10">
          <h2 className="text-2xl text-gray-600 font-semibold mb-6">Xem lại chi tiết</h2>
          <div className="space-y-6">
            {result.results.map((res, index) => {
              const wasAnswered = !!res.userAnswer;
              return (
                <div key={res.questionId} className={`border text-gray-600 border-gray-200 rounded-lg p-5 ${!wasAnswered ? 'bg-gray-200' : 'bg-white'}`}>
                  <p className="font-semibold mb-3">Câu {index + 1}: {res.questionText}</p>

                  {res.correctAnswer ? ( // Multiple Choice or True/False
                    quiz.questions.find(q => q.id === res.questionId)?.answerOptions.map(option => {
                      const isUserAnswer = wasAnswered && res.userAnswer.selectedOptionId === option.id;
                      const isCorrectAnswer = res.correctAnswer.id === option.id;
                      let style = 'bg-white border-gray-200'; // Default for neutral options in an answered question
                      let icon = null;

                      if (isCorrectAnswer) {
                        style = 'bg-green-100 border-green-300 text-green-900';
                        icon = <Check size={16} className="text-green-600" />;
                      } else if (isUserAnswer) { // User's answer, but not correct
                        style = 'bg-red-100 border-red-300 text-red-900';
                        icon = <X size={16} className="text-red-600" />;
                      } else if (!wasAnswered) { // Other options in an unanswered question
                        // For unanswered questions, other options are just neutral within the gray background
                        style = 'bg-white border-gray-300 text-gray-500';
                      }

                      return (
                        <div key={option.id} className={`flex items-center justify-between p-3 mt-2 border rounded-lg ${style}`}>
                          <span>{option.optionText}</span>
                          {icon}
                        </div>
                      );
                    })
                  ) : ( // Essay
                    <div className="space-y-3">
                      <p className="text-sm font-medium text-gray-600">Câu trả lời của bạn:</p>
                      <div className="p-3 bg-gray-50 border border-gray-200 rounded-md text-gray-800 whitespace-pre-wrap">
                        {res.userAnswer?.answerText || <i className="text-gray-400">Không trả lời</i>}
                      </div>
                    </div>
                  )}

                  {res.explanation && (
                    <div className="mt-4 p-3 bg-blue-50 border-l-4 border-blue-300">
                      <p className="font-semibold text-blue-800">Giải thích:</p>
                      <p className="text-blue-700 mt-1">{res.explanation}</p>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>

        {/* Actions */}
        <div className="mt-10 flex justify-center gap-4">
          <button onClick={() => navigate('/')} className="px-6 py-3 bg-white border border-gray-300 rounded-lg hover:bg-gray-100">
            Về trang chủ
          </button>
          <button onClick={() => window.location.reload()} className="px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700">
            Làm lại
          </button>
        </div>
      </div>
    );
  };

  // Main render logic
  return (
    <div className="max-w-7xl mx-auto">
      {quizState === 'LOADING' && renderLoading()}
      {quizState === 'IN_PROGRESS' && renderInProgress()}
      {quizState === 'SUBMITTING' && (
        <div className="relative">
          {renderInProgress()}
          <div className="absolute inset-0 bg-white bg-opacity-75 flex flex-col items-center justify-center rounded-lg">
            <Loader2 className="animate-spin text-green-600" size={48} />
            <p className="mt-4 text-lg text-gray-600">Đang nộp bài...</p>
          </div>
        </div>
      )}
      {quizState === 'COMPLETED' && renderCompleted()}

      <ConfirmationModal
        isOpen={isConfirmModalOpen}
        onClose={() => setIsConfirmModalOpen(false)}
        onConfirm={handleConfirmSubmit}
        title="Xác nhận nộp bài"
        message="Bạn có chắc chắn muốn nộp bài không? Bạn sẽ không thể thay đổi câu trả lời sau khi nộp."
        confirmText="Nộp bài"
        variant="primary"
      />

      <ConfirmationModal
        isOpen={isExitModalOpen}
        onClose={() => setIsExitModalOpen(false)}
        onConfirm={handleConfirmExit}
        title="Xác nhận thoát"
        message="Bạn có chắc chắn muốn thoát không? Mọi tiến trình làm bài sẽ bị mất."
        confirmText="Thoát"
        variant="danger"
      />
    </div>
  );
}