import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { User, Mail, Calendar, BookOpen, Target, Award, Star, TrendingUp, DollarSign, ChartBar, Edit, Link2, Chrome, Facebook } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import { userService } from '@/services/userService';
import EditProfileModal from './EditProfileModal'; // Import the new modal component
import { goalDisplayMap } from '@/utils/displayMaps';

const UserProfilePage = () => {
    // Get basic user info and updateUser from auth context.
    // Assumes useAuth provides an `updateUser` function to update the global user state.
    const { user, updateUser } = useAuth();
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isSavingProfile, setIsSavingProfile] = useState(false);

    useEffect(() => {
        const fetchDashboardData = async () => {
            try {
                const data = await userService.getDashboardStats();
                setStats(data);
            } catch (error) {
                console.error("Failed to fetch user dashboard stats:", error);
                toast.error("Không thể tải dữ liệu hồ sơ.");
            } finally {
                setLoading(false);
            }
        };

        fetchDashboardData();
    }, []);

    const handleSaveProfile = async (updatedData) => {
        setIsSavingProfile(true);
        try {
            const response = await userService.updateUserProfile(updatedData);
            // Update the user in the auth context
            updateUser(response); // Assuming response contains the updated user object
            toast.success("Cập nhật hồ sơ thành công!");
            setIsEditModalOpen(false);
        } catch (error) {
            console.error("Failed to update user profile:", error);
            toast.error(error.response?.data?.message || "Không thể cập nhật hồ sơ.");
        } finally {
            setIsSavingProfile(false);
        }
    };

    if (loading) {
        return <div className="p-6 text-center text-gray-600">Đang tải dữ liệu hồ sơ...</div>;
    }

    if (!user) {
        return <div className="p-6 text-center text-red-500">Bạn cần đăng nhập để xem hồ sơ.</div>;
    }

    // Helper for displaying stats/info items
    const InfoItem = ({ icon, label, value, valueClassName = "text-gray-700" }) => (
        <div className="flex items-center justify-between py-3 border-b border-gray-100 last:border-b-0">
            {icon}
            <div className="flex-1 ml-4">
                <p className="text-sm font-medium text-gray-600">{label}</p>
                <p className={`text-sm font-semibold ${value}`.trim() ? valueClassName : 'text-gray-400 italic'}>{value || 'Chưa cập nhật'}</p>
            </div>
        </div>
    );

    return (
        <div className="min-h-screen bg-gray-50 p-6">
            <div className="max-w-3xl mx-auto bg-white rounded-lg shadow-xl p-8">
                <h1 className="text-3xl font-extrabold text-gray-900 mb-8 text-center">Hồ sơ của tôi</h1>

                <div className="space-y-8">
                    {/* Thông tin cá nhân */}
                    <div className="border border-gray-200 rounded-xl p-6">
                        <h2 className="text-xl font-bold text-gray-800 mb-5 flex items-center pb-3 border-b border-gray-200">
                            <User className="mr-2 text-green-600" size={24} /> Thông tin cơ bản
                        </h2>
                        <div className="space-y-2">
                            <InfoItem icon={<Mail className="h-5 w-5 text-gray-400" />} label="Email" value={user.email} />
                            <InfoItem icon={<User className="h-5 w-5 text-gray-400" />} label="Tên hiển thị" value={user.name} />
                            <InfoItem icon={<BookOpen className="h-5 w-5 text-gray-400" />} label="Lớp học" value={user.grade} />
                            <InfoItem icon={<Target className="h-5 w-5 text-gray-400" />} label="Mục tiêu" value={goalDisplayMap[user.goal] || 'Chưa cập nhật'} />
                            <InfoItem
                                icon={<Calendar className="h-5 w-5 text-gray-400" />}
                                label="Ngày tham gia"
                                value={user.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : 'Không xác định'}
                            />
                            {user.provider && user.provider !== 'local' && (
                                <div className="!my-4"> {/* Thêm khoảng trống trên và dưới */}
                                    <div className="w-full flex items-center justify-center px-4 py-3 border border-gray-300 rounded-lg shadow-sm bg-white text-sm font-medium text-gray-700">
                                        {user.provider === 'google' && (
                                            <svg className="w-5 h-5 mr-2" viewBox="0 0 24 24">
                                                <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                                                <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                                                <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                                                <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
                                            </svg>
                                        )}
                                        {user.provider === 'facebook' && (
                                            <svg className="w-5 h-5 mr-2 text-blue-600" fill="currentColor" viewBox="0 0 24 24">
                                                <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z" />
                                            </svg>
                                        )}
                                        Đã liên kết với <span className="capitalize font-semibold ml-1">{user.provider}</span>
                                    </div>
                                </div>
                            )}
                        </div>
                        <div className="mt-8 grid grid-cols-1 sm:grid-cols-2 gap-4">
                            <Link
                                to="/user/dashboard"
                                className="w-full flex items-center justify-center px-4 py-2.5 border border-gray-300 rounded-lg shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 transition-colors duration-200"
                            >
                                <ChartBar size={16} className="mr-2" /> Thống kê
                            </Link>
                            <button
                                onClick={() => setIsEditModalOpen(true)}
                                className="w-full flex items-center justify-center px-4 py-2.5 border border-gray-300 rounded-lg shadow-sm text-sm font-medium text-white bg-white hover:bg-gray-50 transition-colors duration-200"
                            >
                                <Edit size={16} className="mr-2" />
                                Chỉnh sửa hồ sơ
                            </button>
                        </div>
                    </div>

                    {/* Điểm & Giao dịch */}
                    <div className="bg-gradient-to-r from-indigo-50 to-purple-50 border border-indigo-200 rounded-xl p-6 text-center">
                        <h2 className="text-xl font-bold text-gray-800 mb-2 flex items-center justify-center">
                            <DollarSign className="mr-2 text-indigo-600" size={24} /> Điểm & Giao dịch
                        </h2>
                        <p className="text-gray-600 mb-4">Quản lý điểm tiêu dùng của bạn.</p>
                        <div className="my-6">
                            <p className="text-sm text-gray-500">Điểm có thể dùng</p>
                            <p className="text-5xl font-extrabold text-indigo-600">{stats?.consumptionPoints?.toLocaleString() || 0}</p>
                        </div>
                        <Link to="/purchase-points" className="inline-flex items-center justify-center px-8 py-2.5 bg-indigo-700 text-white rounded-lg font-bold hover:bg-indigo-800 transition duration-200 shadow-md">
                            <DollarSign size={18} className="mr-2" />
                            Mua thêm điểm
                        </Link>
                    </div>
                </div>
            </div>

            <EditProfileModal
                user={user}
                isOpen={isEditModalOpen}
                onClose={() => setIsEditModalOpen(false)}
                onSave={handleSaveProfile}
                isLoading={isSavingProfile}
            />
        </div>
    );
};

export default UserProfilePage;
