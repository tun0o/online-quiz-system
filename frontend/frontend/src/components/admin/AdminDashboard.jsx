import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { Users, CheckSquare, Clock, Inbox, LineChart as LineChartIcon } from 'lucide-react';
import { Link } from 'react-router-dom'; // Import Link
import { useAuth } from '@/hooks/useAuth';
import { adminService } from '@/services/adminService';
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend,
    Filler,
} from 'chart.js';
import { Line } from 'react-chartjs-2';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend, Filler);

const StatCard = ({ title, value, icon, colorClass, linkTo }) => {
    const content = (
        // Nội dung thẻ thống kê
    <div className={`bg-white p-6 rounded-lg border-l-4 ${colorClass} shadow-sm`}>
        <div className="flex items-center">
            <div className="flex-shrink-0">{icon}</div>
            <div className="ml-5 w-0 flex-1">
                <dl>
                    <dt className="text-sm font-medium text-gray-500 truncate">{title}</dt>
                    <dd className="text-3xl font-bold text-gray-900">{value?.toLocaleString() ?? 'N/A'}</dd>
                </dl>
            </div>
        </div>
    </div>
    );

    // Nếu có linkTo, bọc nội dung bằng Link
    if (linkTo) {
        return <Link to={linkTo} className="block hover:shadow-md transition-shadow duration-200">{content}</Link>;
    }
    // Nếu không có linkTo, trả về nội dung thẻ bình thường
    return content;
};


const AdminDashboard = () => {
    const { user } = useAuth();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const data = await adminService.getDashboardStats();
                setStats(data);
            } catch (error) {
                toast.error("Không thể tải dữ liệu dashboard.");
                console.error("Failed to fetch admin dashboard stats:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchStats();
    }, []);

    if (loading) {
        return <div className="p-6 text-center">Đang tải dữ liệu...</div>;
    }

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: {
                position: 'top',
            },
            tooltip: {
                backgroundColor: '#fff',
                titleColor: '#333',
                bodyColor: '#666',
                borderColor: '#ddd',
                borderWidth: 1,
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                ticks: {
                    stepSize: 1,
                }
            }
        },
    };

    const userChartData = {
        labels: stats?.userRegistrations?.map(d => new Date(d.date).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' })) || [],
        datasets: [{
            label: 'Người dùng mới',
            data: stats?.userRegistrations?.map(d => d.count) || [],
            fill: true,
            borderColor: 'rgb(59, 130, 246)',
            backgroundColor: 'rgba(59, 130, 246, 0.2)',
            tension: 0.3,
            pointBackgroundColor: 'rgb(59, 130, 246)',
        }],
    };

    const submissionChartData = {
        labels: stats?.quizSubmissions?.map(d => new Date(d.date).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' })) || [],
        datasets: [{
            label: 'Đề thi mới',
            data: stats?.quizSubmissions?.map(d => d.count) || [],
            fill: true,
            borderColor: 'rgb(16, 185, 129)',
            backgroundColor: 'rgba(16, 185, 129, 0.2)',
            tension: 0.3,
            pointBackgroundColor: 'rgb(16, 185, 129)',
        }],
    };

    return (
        <div className="p-6">
            <h1 className="text-3xl font-bold text-gray-800 mb-6">Dashboard Quản trị viên</h1>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
                <StatCard title="Đề chờ duyệt" value={stats?.totalPendingSubmissions} icon={<Clock size={32} className="text-yellow-500" />} colorClass="border-yellow-500" linkTo="/admin/moderation" />
                <StatCard title="Bài cần chấm" value={stats?.totalPendingGradings} icon={<Inbox size={32} className="text-purple-500" />} colorClass="border-purple-500" linkTo="/admin/grading" />
                <StatCard title="Tổng người dùng" value={stats?.totalUsers} icon={<Users size={32} className="text-blue-500" />} colorClass="border-blue-500" linkTo="/admin/users" />
                <StatCard title="Tổng đề thi" value={stats?.totalApprovedQuizzes} icon={<CheckSquare size={32} className="text-green-500" />} colorClass="border-green-500" linkTo="/admin/management" />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
                    <h3 className="font-semibold mb-4 text-gray-800 text-lg flex items-center"><LineChartIcon size={20} className="mr-2 text-blue-500" /> Người dùng mới trong 7 ngày</h3>
                    <div style={{ height: '300px' }}>
                        <Line options={chartOptions} data={userChartData} />
                    </div>
                </div>
                <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
                    <h3 className="font-semibold mb-4 text-gray-800 text-lg flex items-center"><LineChartIcon size={20} className="mr-2 text-green-500" /> Đề thi được nộp trong 7 ngày</h3>
                    <div style={{ height: '300px' }}>
                        <Line options={chartOptions} data={submissionChartData} />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;