import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { History, Loader2, BookOpen } from 'lucide-react';
import { userService } from '@/services/userService';
import Pagination from '@/components/common/Pagination.jsx';

const AttemptHistoryPage = () => {
    const [attempts, setAttempts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [pagination, setPagination] = useState({
        page: 0,
        size: 10,
        totalPages: 0,
        totalElements: 0,
    });

    useEffect(() => {
        const fetchHistory = async () => {
            setLoading(true);
            try {
                const data = await userService.getAttemptHistory({
                    page: pagination.page,
                    size: pagination.size,
                });
                setAttempts(data.content);
                setPagination(prev => ({
                    ...prev,
                    totalPages: data.totalPages,
                    totalElements: data.totalElements,
                }));
            } catch (error) {
                toast.error("Không thể tải lịch sử làm bài.");
                console.error("Failed to fetch attempt history:", error);
            } finally {
                setLoading(false);
            }
        };

        fetchHistory();
    }, [pagination.page, pagination.size]);

    const handlePageChange = (newPage) => {
        setPagination(prev => ({ ...prev, page: newPage }));
    };

    return (
        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200 min-h-[600px] flex flex-col">
            <div className="flex items-center gap-3 mb-6">
                <History className="text-blue-600" size={28} />
                <h1 className="text-2xl font-bold text-gray-800">Lịch sử làm bài</h1>
            </div>

            {loading ? (
                <div className="flex-grow flex justify-center items-center">
                    <Loader2 className="animate-spin text-gray-500" size={32} />
                </div>
            ) : attempts.length > 0 ? (
                <div className="flex-grow flex flex-col justify-between">
                    <div className="space-y-3">
                        {attempts.map(attempt => (
                            <div key={attempt.id} className="flex flex-col sm:flex-row justify-between sm:items-center gap-3 p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition border border-gray-200">
                                <div>
                                    <p className="font-medium text-gray-800">{attempt.quizTitle}</p>
                                    <p className="text-xs text-gray-500">
                                        Hoàn thành lúc: {new Date(attempt.completedAt).toLocaleString('vi-VN')}
                                    </p>
                                </div>
                                <div className="flex items-center gap-4">
                                    <div className='text-right'>
                                        <span className='text-green-600 font-bold text-lg'>{attempt.score.toFixed(1)}/10</span>
                                        <p className='text-xs text-gray-500'>{attempt.correctAnswers}/{attempt.totalQuestions} câu</p>
                                    </div>
                                    <Link
                                        to={`/attempts/${attempt.id}/result`}
                                        className="px-4 py-2 text-sm font-medium text-blue-600 bg-blue-50 rounded-md border border-blue-200 hover:bg-blue-100 hover:text-blue-700 transition-all duration-200 whitespace-nowrap"
                                    >
                                        Xem chi tiết
                                    </Link>
                                </div>
                            </div>
                        ))}
                    </div>
                    {pagination.totalPages > 1 && (
                        <Pagination currentPage={pagination.page} totalPages={pagination.totalPages} onPageChange={handlePageChange} />
                    )}
                </div>
            ) : (
                <div className="text-center text-gray-500 py-10 flex-grow flex flex-col justify-center items-center">
                    <BookOpen size={48} className="text-gray-400 mb-4" />
                    <p className="font-medium">Bạn chưa làm bài thi nào.</p>
                    <p className="text-sm">Hãy bắt đầu làm một bài thi để xem lịch sử của bạn tại đây.</p>
                </div>
            )}
        </div>
    );
};

export default AttemptHistoryPage;
