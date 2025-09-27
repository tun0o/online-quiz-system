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
        // refresh token is stored as httpOnly cookie; call logout endpoint and server will clear cookie
        return api.post('/api/auth/logout', {});
    },

    refreshToken: () => {
        // refresh token will be sent automatically via cookie (withCredentials)
        return api.post('/api/auth/refresh', {});
    },

    verifyEmail: (token) => {
        return api.get(`/api/auth/verify?token=${token}`);
    },

    resendVerification: (email) => {
        return api.post('/api/auth/resend-verification', { email });
    },

    requestPasswordReset: (email) => {
        return api.post('/api/auth/forgot-password', { email });
    },

    resetPassword: ({ token, newPassword }) => {
        return api.post('/api/auth/reset-password', { token, newPassword });
    },

    getProfile: () => {
        return api.get('/api/auth/profile');
    },

    getSessions: () => {
        return api.get('/api/user/sessions');
    }
};

export default authService;