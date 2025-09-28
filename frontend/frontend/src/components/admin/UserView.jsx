import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { 
    User, 
    Settings, 
    LogOut, 
    Eye, 
    EyeOff, 
    Lock, 
    Mail, 
    Calendar,
    BookOpen,
    Target,
    ArrowLeft
} from 'lucide-react';
import { useAuth } from '../../hooks/useAuth';
import { useAdminView } from '../../contexts/AdminViewContext';
import api from '@/services/api.js';

const UserView = () => {
    const { user, logout } = useAuth();
    const { switchToAdminView } = useAdminView();
    const navigate = useNavigate();
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    const handleChangePassword = () => {
        navigate('/change-password');
    };

    const handleViewProfile = () => {
        // Có thể navigate đến profile page nếu có
        toast.info('Tính năng xem profile sẽ được triển khai');
    };

    const handleViewSubmissions = () => {
        // Navigate đến trang submissions của user
        navigate('/user/submissions');
    };

    const handleCreateSubmission = () => {
        // Navigate đến trang tạo submission
        navigate('/user/create-submission');
    };

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Header */}
            <div className="bg-white shadow">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between items-center py-4">
                        <div className="flex items-center">
                            <button
                                onClick={switchToAdminView}
                                className="flex items-center text-gray-600 hover:text-gray-900 mr-4"
                            >
                                <ArrowLeft size={20} className="mr-2" />
                                Quay lại Admin
                            </button>
                            <h1 className="text-2xl font-bold text-gray-900">
                                Giao diện Người dùng
                            </h1>
                        </div>
                        <div className="flex items-center space-x-4">
                            <span className="text-sm text-gray-500">
                                Đang xem với quyền: <span className="font-medium text-green-600">USER</span>
                            </span>
                            <button
                                onClick={handleLogout}
                                className="flex items-center px-3 py-2 text-sm text-red-600 hover:text-red-800"
                            >
                                <LogOut size={16} className="mr-1" />
                                Đăng xuất
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                    {/* User Info Card */}
                    <div className="lg:col-span-1">
                        <div className="bg-white rounded-lg shadow p-6">
                            <div className="flex items-center mb-6">
                                <div className="bg-green-100 p-3 rounded-full">
                                    <User className="h-8 w-8 text-green-600" />
                                </div>
                                <div className="ml-4">
                                    <h2 className="text-lg font-semibold text-gray-900">
                                        Thông tin cá nhân
                                    </h2>
                                    <p className="text-sm text-gray-500">
                                        Quản lý thông tin tài khoản
                                    </p>
                                </div>
                            </div>

                            <div className="space-y-4">
                                <div className="flex items-center">
                                    <Mail className="h-5 w-5 text-gray-400 mr-3" />
                                    <div>
                                        <p className="text-sm font-medium text-gray-900">Email</p>
                                        <p className="text-sm text-gray-500">{user?.email}</p>
                                    </div>
                                </div>

                                <div className="flex items-center">
                                    <BookOpen className="h-5 w-5 text-gray-400 mr-3" />
                                    <div>
                                        <p className="text-sm font-medium text-gray-900">Lớp học</p>
                                        <p className="text-sm text-gray-500">{user?.grade || 'Chưa cập nhật'}</p>
                                    </div>
                                </div>

                                <div className="flex items-center">
                                    <Target className="h-5 w-5 text-gray-400 mr-3" />
                                    <div>
                                        <p className="text-sm font-medium text-gray-900">Mục tiêu</p>
                                        <p className="text-sm text-gray-500">{user?.goal || 'Chưa cập nhật'}</p>
                                    </div>
                                </div>

                                <div className="flex items-center">
                                    <Calendar className="h-5 w-5 text-gray-400 mr-3" />
                                    <div>
                                        <p className="text-sm font-medium text-gray-900">Tham gia</p>
                                        <p className="text-sm text-gray-500">
                                            {user?.createdAt ? new Date(user.createdAt).toLocaleDateString('vi-VN') : 'Không xác định'}
                                        </p>
                                    </div>
                                </div>
                            </div>

                            <div className="mt-6 space-y-3">
                                <button
                                    onClick={handleViewProfile}
                                    className="w-full flex items-center justify-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                                >
                                    <User size={16} className="mr-2" />
                                    Xem Profile
                                </button>
                                
                                <button
                                    onClick={handleChangePassword}
                                    className="w-full flex items-center justify-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
                                >
                                    <Lock size={16} className="mr-2" />
                                    Đổi mật khẩu
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* User Actions */}
                    <div className="lg:col-span-2">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            {/* Quiz Submissions */}
                            <div className="bg-white rounded-lg shadow p-6">
                                <div className="flex items-center mb-4">
                                    <div className="bg-blue-100 p-3 rounded-full">
                                        <BookOpen className="h-6 w-6 text-blue-600" />
                                    </div>
                                    <div className="ml-3">
                                        <h3 className="text-lg font-semibold text-gray-900">
                                            Quiz Submissions
                                        </h3>
                                        <p className="text-sm text-gray-500">
                                            Quản lý bài nộp của bạn
                                        </p>
                                    </div>
                                </div>
                                
                                <div className="space-y-3">
                                    <button
                                        onClick={handleViewSubmissions}
                                        className="w-full flex items-center justify-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition duration-200"
                                    >
                                        <Eye size={16} className="mr-2" />
                                        Xem bài nộp
                                    </button>
                                    
                                    <button
                                        onClick={handleCreateSubmission}
                                        className="w-full flex items-center justify-center px-4 py-2 border border-blue-600 text-blue-600 rounded-md hover:bg-blue-50 transition duration-200"
                                    >
                                        <BookOpen size={16} className="mr-2" />
                                        Tạo bài nộp mới
                                    </button>
                                </div>
                            </div>

                            {/* Quick Stats */}
                            <div className="bg-white rounded-lg shadow p-6">
                                <div className="flex items-center mb-4">
                                    <div className="bg-green-100 p-3 rounded-full">
                                        <Target className="h-6 w-6 text-green-600" />
                                    </div>
                                    <div className="ml-3">
                                        <h3 className="text-lg font-semibold text-gray-900">
                                            Thống kê nhanh
                                        </h3>
                                        <p className="text-sm text-gray-500">
                                            Tổng quan hoạt động
                                        </p>
                                    </div>
                                </div>
                                
                                <div className="space-y-3">
                                    <div className="flex justify-between items-center">
                                        <span className="text-sm text-gray-600">Bài nộp đã tạo:</span>
                                        <span className="text-sm font-medium text-gray-900">0</span>
                                    </div>
                                    <div className="flex justify-between items-center">
                                        <span className="text-sm text-gray-600">Bài được duyệt:</span>
                                        <span className="text-sm font-medium text-green-600">0</span>
                                    </div>
                                    <div className="flex justify-between items-center">
                                        <span className="text-sm text-gray-600">Bài đang chờ:</span>
                                        <span className="text-sm font-medium text-yellow-600">0</span>
                                    </div>
                                </div>
                            </div>

                            {/* Recent Activity */}
                            <div className="bg-white rounded-lg shadow p-6 md:col-span-2">
                                <div className="flex items-center mb-4">
                                    <div className="bg-purple-100 p-3 rounded-full">
                                        <Calendar className="h-6 w-6 text-purple-600" />
                                    </div>
                                    <div className="ml-3">
                                        <h3 className="text-lg font-semibold text-gray-900">
                                            Hoạt động gần đây
                                        </h3>
                                        <p className="text-sm text-gray-500">
                                            Lịch sử hoạt động của bạn
                                        </p>
                                    </div>
                                </div>
                                
                                <div className="text-center py-8">
                                    <Calendar className="mx-auto h-12 w-12 text-gray-400 mb-4" />
                                    <p className="text-gray-500">Chưa có hoạt động nào</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UserView;

