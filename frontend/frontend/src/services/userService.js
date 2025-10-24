// frontend/src/services/userService.js
import api from './api';

export const userService = {
    /**
     * Lấy tất cả dữ liệu thống kê cho trang dashboard của người dùng.
     */
    getDashboardStats: async () => {
        const response = await api.get('/api/user/me/dashboard-stats');
        return response.data;
    },

    /**
     * Lấy danh sách người dùng cho admin (có phân trang).
     * @param {object} params - Các tham số truy vấn (page, size, etc.)
     */
    getUsersForAdmin: async (params) => {
        const response = await api.get('/api/admin/users', { params });
        return response.data;
    },

    /**
     * Lấy thông tin chi tiết một người dùng bằng ID (cho admin).
     * @param {number} userId - ID của người dùng
     */
    getUserByIdForAdmin: async (userId) => {
        const response = await api.get(`/api/admin/users/${userId}`);
        return response.data;
    },

    /**
     * Cập nhật thông tin người dùng (cho admin).
     * @param {number} userId - ID của người dùng
     * @param {object} userData - Dữ liệu cập nhật
     */
    updateUserByAdmin: async (userId, userData) => {
        const response = await api.put(`/api/admin/users/${userId}`, userData);
        return response.data;
    },

    /**
     * Tạo người dùng mới (cho admin).
     * @param {object} userData - Dữ liệu người dùng mới
     */
    createUserByAdmin: async (userData) => {
        const response = await api.post('/api/admin/users', userData);
        return response.data;
    },

    /**
     * Cập nhật thông tin hồ sơ của người dùng hiện tại.
     * @param {object} userData - Dữ liệu hồ sơ cần cập nhật (name, grade, goal)
     */
    updateUserProfile: async (userData) => {
        const response = await api.put('/api/user/me', userData); // Assuming a PUT /api/user/me endpoint for profile updates
        return response.data;
    },
};
