import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { CheckCircle, XCircle, HelpCircle, ArrowLeft } from 'lucide-react';
import { quizService } from '@/services/quizService';

const getAnswerColor = (option, question, userAnswer) => {
    const isSelected = userAnswer?.selectedOptionId === option.id;
    const isCorrect = option.isCorrect;

    if (isCorrect) {
        return 'border-green-500 bg-green-50'; // Correct answer
    }
    if (isSelected && !isCorrect) {
        return 'border-red-500 bg-red-50'; // User's wrong choice
    }
    return 'border-gray-200 bg-white'; // Default
};

const AttemptResultPage = () => {
    const { attemptId } = useParams();
    const [result, setResult] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchResult = async () => {
            try {
                const data = await quizService.getAttemptResult(attemptId);
                setResult(data);
            } catch (error) {
                toast.error("Không thể tải kết quả bài làm.");
                console.error("Failed to fetch attempt result:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchResult();
    }, [attemptId]);

    if (loading) {
        return <div className="text-center p-10">Đang tải kết quả...</div>;
    }

    if (!result) {
        return <div className="text-center p-10 text-red-500">Không tìm thấy kết quả.</div>;
    }

    return (
        <div className="max-w-4xl mx-auto">
            <Link to="/user/dashboard" className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-800 mb-4">
                <ArrowLeft size={16} />
                Quay lại Dashboard
            </Link>

            <div className="bg-white p-6 rounded-lg shadow-md mb-6">
                <h1 className="text-2xl font-bold mb-2">{result.quizTitle}</h1>
                <p className="text-gray-600">Hoàn thành lúc: {new Date(result.completedAt).toLocaleString('vi-VN')}</p>
                <div className="mt-4 flex flex-wrap gap-4 text-center">
                    <div className="flex-1 bg-blue-50 p-3 rounded-lg">
                        <p className="text-sm text-blue-800 font-semibold">Điểm số</p>
                        <p className="text-2xl font-bold text-blue-900">{result.score.toFixed(1)}/10</p>
                    </div>
                    <div className="flex-1 bg-green-50 p-3 rounded-lg">
                        <p className="text-sm text-green-800 font-semibold">Số câu đúng</p>
                        <p className="text-2xl font-bold text-green-900">{result.correctAnswers} / {result.totalQuestions}</p>
                    </div>
                </div>
            </div>

            <div className="space-y-6">
                {result.questions.map((question, index) => {
                    const userAnswer = result.userAnswers.find(a => a.questionId === question.id);
                    const isCorrect = userAnswer?.isCorrect;

                    return (
                        <div key={question.id} className="bg-white p-6 rounded-lg shadow-md">
                            <div className="flex justify-between items-start mb-4">
                                <p className="font-semibold text-gray-800">Câu {index + 1}: {question.questionText}</p>
                                {isCorrect === true && <CheckCircle className="text-green-500 flex-shrink-0 ml-4" />}
                                {isCorrect === false && <XCircle className="text-red-500 flex-shrink-0 ml-4" />}
                                {isCorrect === null && <HelpCircle className="text-yellow-500 flex-shrink-0 ml-4" />}
                            </div>

                            <div className="space-y-3">
                                {question.options.map(option => (
                                    <div
                                        key={option.id}
                                        className={`p-3 border rounded-lg ${getAnswerColor(option, question, userAnswer)}`}
                                    >
                                        {option.optionText}
                                    </div>
                                ))}
                            </div>

                            {question.explanation && (
                                <div className="mt-4 p-3 bg-gray-100 border-l-4 border-gray-400 rounded-r-lg">
                                    <p className="font-semibold text-sm text-gray-700">Giải thích:</p>
                                    <p className="text-sm text-gray-600">{question.explanation}</p>
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export default AttemptResultPage;
