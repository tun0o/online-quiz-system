// src/components/user/UserDashboard.jsx
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { BookOpen, Award, Star, TrendingUp, Send, CheckCircle, Clock, XCircle, LineChart as LineChartIcon } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import { userService } from '@/services/userService'; // Giả sử bạn sẽ thêm hàm mới vào đây
import { ResponsiveContainer, LineChart, CartesianGrid, XAxis, YAxis, Tooltip, Legend, Line } from 'recharts';

// Component Card thống kê chung
const StatCard = ({ icon, title, value, color, unit }) => (
    <div className={`bg-${color}-50 p-4 rounded-lg border border-${color}-200 flex items-start`}>
        <div className={`p-2 bg-${color}-100 rounded-full mr-4`}>
            {icon}
        </div>
        <div>
            <h3 className={`font-semibold text-sm text-${color}-800`}>{title}</h3>
            <p className="text-2xl font-bold text-gray-800">
                {value ?? 'N/A'}
                {unit && <span className="text-base font-medium ml-1">{unit}</span>}
            </p>
        </div>
    </div>
);

// Component cho từng mục thống kê đóng góp
const ContributionStatItem = ({ icon, label, value, color }) => (
    <div className="flex items-center p-3 bg-gray-50 rounded-lg">
        <div className={`p-2 rounded-full bg-${color}-100 mr-3`}>
            {icon}
        </div>
        <span className="text-sm text-gray-600 flex-1">{label}</span>
        <span className={`font-bold text-gray-800 text-lg`}>{value ?? 0}</span>
    </div>
);

const UserDashboard = () => {
    const { user } = useAuth();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                // Giả sử bạn tạo một hàm mới `getDashboardStats` trong userService
                // để lấy tất cả dữ liệu cần thiết trong một lần gọi API.
                const data = await userService.getDashboardStats();
                setStats(data);
            } catch (error) {
                console.error("Failed to fetch user dashboard stats:", error);
                toast.error("Không thể tải dữ liệu dashboard.");
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    if (loading) {
        return <div className="p-6 text-center">Đang tải dữ liệu...</div>;
    }

    if (!stats) {
        return <div className="p-6 text-center text-red-500">Không thể tải dữ liệu. Vui lòng thử lại.</div>;
    }

    return (
        <div className="p-6 space-y-6">
            <div>
                <h1 className="text-2xl font-bold text-gray-800">Tổng quan</h1>
                <p className="text-gray-600">Chào mừng trở lại, {user?.name || user?.email}!</p>
            </div>

            {/* Các chỉ số chính */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                <StatCard
                    icon={<Award size={24} className="text-yellow-600" />}
                    title="Tổng điểm"
                    value={stats.totalPoints?.toLocaleString()}
                    color="yellow"
                    unit="điểm"
                />
                <StatCard
                    icon={<Star size={24} className="text-orange-600" />}
                    title="Chuỗi ngày"
                    value={stats.currentStreak}
                    color="orange"
                    unit="ngày"
                />
                <StatCard
                    icon={<TrendingUp size={24} className="text-green-600" />}
                    title="Thứ hạng"
                    value={`#${stats.rank}`}
                    color="green"
                />
                <StatCard
                    icon={<BookOpen size={24} className="text-blue-600" />}
                    title="Đề đã làm"
                    value={stats.quizzesTaken}
                    color="blue"
                />
            </div>

            {/* Charts Section */}
            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
                <h3 className="font-semibold mb-4 text-gray-800 text-lg flex items-center"><LineChartIcon size={20} className="mr-2 text-blue-500" /> Hoạt động làm bài trong 7 ngày</h3>
                <ResponsiveContainer width="100%" height={300}>
                    <LineChart data={stats?.quizAttemptsOverTime}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="date" tickFormatter={(date) => new Date(date).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' })} />
                        <YAxis allowDecimals={false} />
                        <Tooltip />
                        <Legend />
                        <Line type="monotone" dataKey="count" name="Số bài đã làm" stroke="#3b82f6" strokeWidth={2} />
                    </LineChart>
                </ResponsiveContainer>
            </div>

            {/* Thống kê đóng góp và Lịch sử gần đây */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Thống kê đóng góp */}
                <div className="lg:col-span-1 bg-white rounded-lg shadow-sm p-6 border border-gray-200">
                    <h3 className="font-semibold mb-4 text-gray-800">Thống kê đóng góp</h3>
                    <div className="space-y-2">
                        <ContributionStatItem
                            icon={<Send size={18} className="text-gray-600" />}
                            label="Đã gửi"
                            value={stats.contributions?.submitted}
                            color="gray"
                        />
                        <ContributionStatItem
                            icon={<CheckCircle size={18} className="text-green-600" />}
                            label="Được duyệt"
                            value={stats.contributions?.approved}
                            color="green"
                        />
                        <ContributionStatItem
                            icon={<Clock size={18} className="text-yellow-600" />}
                            label="Đang chờ"
                            value={stats.contributions?.pending}
                            color="yellow"
                        />
                        <ContributionStatItem
                            icon={<XCircle size={18} className="text-red-600" />}
                            label="Bị từ chối"
                            value={stats.contributions?.rejected}
                            color="red"
                        />
                    </div>
                </div>

                {/* Lịch sử làm bài */}
                <div className="lg:col-span-2 bg-white rounded-lg shadow-sm p-6 border border-gray-200">
                    <h3 className="font-semibold mb-4 text-gray-800">Lịch sử làm bài gần đây</h3>
                    <div className="space-y-3">
                        {stats.recentAttempts?.length > 0 ? (
                            stats.recentAttempts.map(attempt => (
                                <div key={attempt.id} className="flex flex-col sm:flex-row justify-between sm:items-center gap-3 p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition">
                                    <div>
                                        <p className="font-medium text-gray-800">{attempt.quizTitle}</p>
                                        <p className="text-xs text-gray-500">
                                            {new Date(attempt.completedAt).toLocaleString('vi-VN')}
                                        </p>
                                    </div>
                                    <div className="flex items-center gap-4">
                                        <div className='text-right'>
                                            <span className='text-green-600 font-bold text-lg'>{attempt.score.toFixed(1)}/10</span>
                                            <p className='text-xs text-gray-500'>{attempt.correctAnswers}/{attempt.totalQuestions} câu</p>
                                        </div>
                                        <Link to={`/attempts/${attempt.id}/result`} className="px-4 py-2 text-sm font-bold text-white bg-blue-700 rounded-md hover:bg-blue-800 transition-colors whitespace-nowrap">
                                            Xem chi tiết
                                        </Link>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <p className="text-center text-gray-500 py-4">Bạn chưa làm bài thi nào.</p>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UserDashboard;