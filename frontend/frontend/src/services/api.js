import axios from 'axios';

// Lấy baseURL từ .env, fallback localhost
const baseURL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

// OAuth2 URLs - ENABLED
export const OAUTH2_URLS = {
    GOOGLE: `${baseURL}/oauth2/authorization/google`,
    FACEBOOK: `${baseURL}/oauth2/authorization/facebook`,
    SUCCESS: '/oauth2/success',
    ERROR: '/oauth2/error'
};

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

        // Kiểm tra lỗi network trước
        if (error.code === 'ERR_TOO_MANY_REDIRECTS' || error.code === 'ERR_NETWORK') {
            console.error('Network error detected:', error.code);
            // Clear tokens và redirect để tránh vòng lặp
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('user');
            // Không retry, trả về lỗi ngay
            return Promise.reject(error);
        }

        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;

            try {
                const refreshToken = localStorage.getItem('refreshToken');
                if (!refreshToken) {
                    console.log('No refresh token available');
                    throw new Error('No refresh token');
                }

                console.log('Attempting to refresh token...');
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
                console.error('Token refresh failed:', refreshError);
                // Refresh token thất bại => logout
                localStorage.removeItem('accessToken');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('user');
                // Tránh vòng lặp redirect
                if (window.location.pathname !== '/login') {
                    window.location.href = '/login';
                }
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default api;
