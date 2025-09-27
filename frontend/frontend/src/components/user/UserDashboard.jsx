// src/components/user/UserDashboard.jsx
import React from 'react';
import { useAuth } from '@/hooks/useAuth';

const UserDashboard = () => {
    const { user } = useAuth();

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold mb-4">Dashboard Người dùng</h1>
            <div className="bg-white rounded-lg shadow-sm p-6">
                <p className="text-lg mb-4">Xin chào, {user?.email}!</p>

                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    <div className="bg-blue-50 p-4 rounded-lg border border-blue-200">
                        <h3 className="font-semibold text-blue-800">Đề thi đã làm</h3>
                        <p className="text-2xl font-bold">12</p>
                    </div>

                    <div className="bg-green-50 p-4 rounded-lg border border-green-200">
                        <h3 className="font-semibold text-green-800">Điểm trung bình</h3>
                        <p className="text-2xl font-bold">8.5</p>
                    </div>

                    <div className="bg-purple-50 p-4 rounded-lg border border-purple-200">
                        <h3 className="font-semibold text-purple-800">Thứ hạng</h3>
                        <p className="text-2xl font-bold">#24</p>
                    </div>
                </div>

                <div className="mt-6">
                    <h3 className="font-semibold mb-3">Đề thi gần đây</h3>
                    <div className="space-y-3">
                        <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                            <span>Toán học - Đề số 1</span>
                            <span className="text-green-600 font-medium">8/10</span>
                        </div>
                        <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                            <span>Vật lý - Đề số 3</span>
                            <span className="text-green-600 font-medium">7/10</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default UserDashboard;