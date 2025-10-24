import api from './api';

export const adminService = {
    /**
     * Lấy dữ liệu thống kê cho trang dashboard của admin.
     */
    getDashboardStats: async () => {
        const response = await api.get('/api/admin/dashboard-stats');
        return response.data;
    },
};
