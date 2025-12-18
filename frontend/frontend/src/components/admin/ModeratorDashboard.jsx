import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { Clock, Inbox, CheckSquare, XSquare, LineChart as LineChartIcon } from 'lucide-react';
import { Link } from 'react-router-dom';
import { adminService } from '@/services/adminService'; // Giả sử có service này
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

    if (linkTo) {
        return <Link to={linkTo} className="block hover:shadow-md transition-shadow duration-200">{content}</Link>;
    }
    return content;
};

const ModeratorDashboard = () => {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchStats = async () => {
            try {
                // Chúng ta sẽ cần tạo một endpoint mới ở backend cho việc này
                const data = await adminService.getModeratorDashboardStats();
                setStats(data);
            } catch (error) {
                toast.error("Không thể tải dữ liệu dashboard cho Moderator.");
                console.error("Failed to fetch moderator dashboard stats:", error);
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
        plugins: { legend: { position: 'top' } },
        scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } },
    };

    const moderationChartData = {
        labels: stats?.moderationActivity?.map(d => new Date(d.date).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' })) || [],
        datasets: [
            {
                label: 'Đề đã duyệt',
                data: stats?.moderationActivity?.map(d => d.approvedCount) || [],
                borderColor: 'rgb(16, 185, 129)',
                backgroundColor: 'rgba(16, 185, 129, 0.2)',
                fill: true,
                tension: 0.3,
            },
            {
                label: 'Đề đã từ chối',
                data: stats?.moderationActivity?.map(d => d.rejectedCount) || [],
                borderColor: 'rgb(239, 68, 68)',
                backgroundColor: 'rgba(239, 68, 68, 0.2)',
                fill: true,
                tension: 0.3,
            },
        ],
    };

    return (
        <div className="p-6">
            <h1 className="text-3xl font-bold text-gray-800 mb-6">Dashboard Kiểm duyệt viên</h1>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-2 gap-6 mb-6">
                <StatCard
                    title="Đề chờ duyệt"
                    value={stats?.totalPendingSubmissions}
                    icon={<Clock size={32} className="text-yellow-500" />}
                    colorClass="border-yellow-500"
                    linkTo="/admin/moderation"
                />
                <StatCard
                    title="Bài cần chấm"
                    value={stats?.totalPendingGradings}
                    icon={<Inbox size={32} className="text-purple-500" />}
                    colorClass="border-purple-500"
                    linkTo="/admin/grading"
                />
            </div>

            <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
                <h3 className="font-semibold mb-4 text-gray-800 text-lg flex items-center"><LineChartIcon size={20} className="mr-2 text-blue-500" /> Hoạt động kiểm duyệt trong 7 ngày</h3>
                <div style={{ height: '350px' }}>
                    <Line options={chartOptions} data={moderationChartData} />
                </div>
            </div>
        </div>
    );
};

export default ModeratorDashboard;