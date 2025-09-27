// hooks/useAuth.js
import { useState, useEffect, useCallback } from 'react';
import api from '@/services/api.js';

export const useAuth = () => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Kiểm tra authentication state khi component mount
    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        const userData = localStorage.getItem('user');

        if (token && userData) {
            try {
                const parsedUser = JSON.parse(userData);
                setUser(parsedUser);
            } catch (error) {
                console.error('Error parsing user data:', error);
                localStorage.removeItem('user');
                localStorage.removeItem('accessToken');
            }
        }

        setLoading(false);
    }, []);

    // Hàm login
    const login = useCallback(async (email, password) => {
        try {
            const response = await api.post('/api/auth/login', { email, password });

            const { accessToken, refreshToken, ...userData } = response.data;

            // Lưu tokens và user data
            localStorage.setItem('accessToken', accessToken);
            localStorage.setItem('refreshToken', refreshToken);
            localStorage.setItem('user', JSON.stringify(userData));

            setUser(userData);

            return { success: true };
        } catch (error) {
            console.error('Login error:', error);
            return {
                success: false,
                error: error.response?.data?.error || 'Đăng nhập thất bại',
            };
        }
    }, []);

    // Hàm logout
    const logout = useCallback(() => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('user');
        setUser(null);

        return Promise.resolve();
    }, []);

    // Kiểm tra đã đăng nhập hay chưa
    const isAuthenticated = useCallback(() => {
        return !!user;
    }, [user]);

    // Kiểm tra role
    const hasRole = (roleName) => {
        if (!user || !user.roles) return false;

        // Kiểm tra cả hai định dạng role
        return user.roles.some(role =>
            role.includes(roleName) ||
            role.includes(`ROLE_${roleName}`)
        );
    };

    return {
        user,
        loading,
        login,
        logout,
        isAuthenticated,
        hasRole,
    };
};

