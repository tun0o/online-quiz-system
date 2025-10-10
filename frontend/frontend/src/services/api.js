import axios from 'axios';
import { useAuthStore } from '@/hooks/useAuth';

const api = axios.create({
    baseURL: 'http://localhost:8080',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor để thêm token vào header
api.interceptors.request.use(
    (config) => {
        // Lấy token trực tiếp từ store Zustand thay vì localStorage
        const token = useAuthStore.getState().accessToken;

        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor để xử lý lỗi và refresh token
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        const { logout, refresh } = useAuthStore.getState();

        // Danh sách các đường dẫn không cần xử lý lỗi 401 (vì chúng không yêu cầu xác thực)
        const publicPaths = [
            '/api/auth/login',
            '/api/auth/register',
            '/api/auth/refresh',
            '/api/auth/forgot-password',
            '/api/auth/reset-password'
        ];

        // Nếu là lỗi 401 và không phải từ các trang public và chưa thử lại
        if (error.response.status === 401 && !originalRequest._retry && !publicPaths.some(path => originalRequest.url.includes(path))) {
            originalRequest._retry = true;
            try {
                const newAccessToken = await refresh();
                axios.defaults.headers.common['Authorization'] = 'Bearer ' + newAccessToken;
                return api(originalRequest);
            } catch (refreshError) {
                logout(); // Nếu refresh token thất bại, mới logout
                return Promise.reject(refreshError);
            }
        }

        // Trả về lỗi để component có thể xử lý (ví dụ: hiển thị toast)
        return Promise.reject(error);
    }
);

export default api;