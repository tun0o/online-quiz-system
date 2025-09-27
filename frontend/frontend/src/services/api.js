import axios from 'axios';

// Lấy baseURL từ .env, fallback localhost
const baseURL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

// Tạo instance axios
const api = axios.create({
    baseURL,
    headers: { 'Content-Type': 'application/json' },
});

// Request interceptor: tự động gắn token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            config.headers = {
                ...config.headers,
                Authorization: `Bearer ${token}`,
            };
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor: xử lý khi accessToken hết hạn
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                const refreshToken = localStorage.getItem('refreshToken');
                if (!refreshToken) throw new Error('No refresh token');

                // gọi API refresh
                const response = await axios.post(`${baseURL}/auth/refresh`, { refreshToken });

                const { accessToken, refreshToken: newRefreshToken } = response.data;

                // Lưu token mới
                localStorage.setItem('accessToken', accessToken);
                localStorage.setItem('refreshToken', newRefreshToken);

                // Gắn token mới vào request cũ
                originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                return api(originalRequest);
            } catch (refreshError) {
                // Refresh token thất bại => logout
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('user');
                window.location.href = '/login';
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default api;
