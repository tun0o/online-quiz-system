// src/components/admin/AdminDashboard.jsx
import React from 'react';
import { useAuth } from '@/hooks/useAuth';

const AdminDashboard = () => {
    const { user } = useAuth();

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold mb-4">Dashboard Quản trị viên</h1>
            <div className="bg-white rounded-lg shadow-sm p-6">
                <p className="text-lg mb-6">Xin chào, {user?.email}!</p>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
                    <div className="bg-blue-50 p-4 rounded-lg border border-blue-200">
                        <h3 className="font-semibold text-blue-800">Tổng người dùng</h3>
                        <p className="text-2xl font-bold">1,243</p>
                    </div>

                    <div className="bg-green-50 p-4 rounded-lg border border-green-200">
                        <h3 className="font-semibold text-green-800">Đề thi đã duyệt</h3>
                        <p className="text-2xl font-bold">456</p>
                    </div>

                    <div className="bg-yellow-50 p-4 rounded-lg border border-yellow-200">
                        <h3 className="font-semibold text-yellow-800">Đề chờ duyệt</h3>
                        <p className="text-2xl font-bold">23</p>
                    </div>

                    <div className="bg-red-50 p-4 rounded-lg border border-red-200">
                        <h3 className="font-semibold text-red-800">Báo cáo vi phạm</h3>
                        <p className="text-2xl font-bold">5</p>
                    </div>
                </div>

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    <div>
                        <h3 className="font-semibold mb-3">Hoạt động gần đây</h3>
                        <div className="space-y-3">
                            <div className="p-3 bg-gray-50 rounded-lg">
                                <p className="font-medium">Người dùng mới đăng ký</p>
                                <p className="text-sm text-gray-600">user123@email.com - 10 phút trước</p>
                            </div>
                            <div className="p-3 bg-gray-50 rounded-lg">
                                <p className="font-medium">Đề thi mới được tạo</p>
                                <p className="text-sm text-gray-600">Đề Vật lý lớp 12 - 30 phút trước</p>
                            </div>
                        </div>
                    </div>

                    <div>
                        <h3 className="font-semibold mb-3">Thống kê nhanh</h3>
                        <div className="bg-gray-50 p-4 rounded-lg">
                            <p>• 15 người dùng online</p>
                            <p>• 42 đề thi đang hoạt động</p>
                            <p>• 8 đề thi bị từ chối trong tuần</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AdminDashboard;