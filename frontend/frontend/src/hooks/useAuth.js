import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { useEffect, useState } from 'react';
import api from '@/services/api.js';

const useAuthStore = create(
    persist(
        (set, get) => ({
            user: null,
            accessToken: null,
            refreshToken: null,
            loading: true,

            // --- ACTIONS ---

            // Đăng nhập bằng email/password
            login: async (email, password) => {
                try {
                    const response = await api.post('/api/auth/login', { email, password });
                    const { accessToken, refreshToken, user } = response.data;

                    set({ user, accessToken, refreshToken });
                    // Interceptor sẽ tự động xử lý việc thêm header

                    return { success: true, user: user }; // Trả về đối tượng user đã được làm phẳng
                } catch (error) {
                    console.error('Login error:', error);
                    return {
                        success: false,
                        error: error.response?.data?.error || 'Đăng nhập thất bại',
                    };
                }
            },

            // Đăng nhập bằng token (dùng cho OAuth2 và xác thực email)
            loginWithTokens: (accessToken, refreshToken, user) => {
                // Chỉ cần cập nhật state. Middleware `persist` sẽ tự động lưu vào localStorage.
                // Cơ chế isHydrated trong AppLayout sẽ xử lý việc chờ đợi khi tải lại trang.
                set({ user, accessToken, refreshToken });
            },

            // Đăng ký
            register: async (formData) => {
                try {
                    const response = await api.post('/api/auth/register', formData);
                    return { success: true, message: response.data.message };
                } catch (error) {
                    return {
                        success: false,
                        error: error.response?.data?.error || 'Đăng ký thất bại',
                    };
                }
            },

            // Đăng xuất
            logout: () => {
                set({ user: null, accessToken: null, refreshToken: null });
                delete api.defaults.headers.common['Authorization'];
                // Middleware `persist` sẽ tự động xóa khỏi localStorage
            },

            // Làm mới token
            refresh: async () => {
                const currentRefreshToken = get().refreshToken;
                if (!currentRefreshToken) {
                    get().logout();
                    throw new Error("No refresh token available");
                }
                try {
                    const response = await api.post('/api/auth/refresh', { refreshToken: currentRefreshToken });
                    const { accessToken, refreshToken, user } = response.data;
                    
                    set({ user, accessToken, refreshToken });
                    api.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
                    
                    return accessToken;
                } catch (error) {
                    console.error('Refresh token failed:', error);
                    get().logout(); // Đăng xuất nếu refresh thất bại
                    throw error;
                }
            },

            // --- GETTERS / HELPERS ---

            isAuthenticated: () => !!get().user,

            hasRole: (roleName) => {
                const user = get().user;
                if (!user || !user.roles) return false;
                const upperRoleName = roleName.toUpperCase();
                return user.roles.some(role =>
                    role.toUpperCase().includes(upperRoleName)
                );
            },

            // Hàm này được gọi khi store được khởi tạo lại từ localStorage
            rehydrate: () => {
                const state = get();
                if (state.accessToken) {
                    api.defaults.headers.common['Authorization'] = `Bearer ${state.accessToken}`;
                }
                set({ loading: false });
            }
        }),
        {
            name: 'auth-storage', // Tên key trong localStorage
            storage: createJSONStorage(() => localStorage),
            // Chỉ lưu những field này vào localStorage
            partialize: (state) => ({
                user: state.user,
                accessToken: state.accessToken,
                refreshToken: state.refreshToken,
            }),
            // Hàm được gọi sau khi state được lấy lại từ storage
            // onRehydrateStorage đã được chứng minh là không đáng tin cậy cho việc set state.
            // Chúng ta sẽ sử dụng một hook riêng để xử lý việc này.
        }
    )
);

export { useAuthStore };
export const useAuth = useAuthStore;
