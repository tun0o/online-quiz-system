import axios from 'axios';

// Lấy baseURL từ .env, fallback localhost
const baseURL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

// Tạo instance axios
const api = axios.create({
    baseURL,
    headers: { 'Content-Type': 'application/json' },
    withCredentials: true, // send cookies (httpOnly refresh token)
});

// Request interceptor: tự động gắn token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('accessToken');
        // Device headers
        const fingerprint = localStorage.getItem('deviceFingerprint');
        const deviceName = localStorage.getItem('deviceName');
        if (token) {
            config.headers = {
                ...config.headers,
                Authorization: `Bearer ${token}`,
            };
        }
        config.headers = {
            ...config.headers,
            ...(fingerprint ? { 'X-Device-Fingerprint': fingerprint } : {}),
            ...(deviceName ? { 'X-Device-Name': deviceName } : {}),
        };
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
                // gọi API refresh; refresh token stored as HttpOnly cookie, so no body needed
                const response = await axios.post(`${baseURL}/api/auth/refresh`, {}, { withCredentials: true });

                const { accessToken } = response.data;

                // Lưu token mới
                localStorage.setItem('accessToken', accessToken);

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
