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
};
