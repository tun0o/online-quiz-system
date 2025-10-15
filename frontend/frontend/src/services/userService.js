// frontend/src/services/userService.js
import api from './api';

export const userService = {
    /**
     * Lấy thống kê của người dùng hiện tại (đã đăng nhập).
     */
    getUserStats: async () => {
        const response = await api.get('/api/users/me/stats');
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
};
