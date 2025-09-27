// src/services/authService.js
import api from './api';

export const authService = {
    login: (credentials) => {
        return api.post('/api/auth/login', credentials);
    },

    register: (userData) => {
        return api.post('/api/auth/register', userData);
    },

    logout: () => {
        return api.post('/api/auth/logout');
    },

    refreshToken: (refreshToken) => {
        return api.post('/api/auth/refresh', { refreshToken });
    },

    verifyEmail: (token) => {
        return api.get(`/api/auth/verify?token=${token}`);
    },

    resendVerification: (email) => {
        return api.post('/api/auth/resend-verification', { email });
    },

    getProfile: () => {
        return api.get('/api/auth/profile');
    }
};

export default authService;