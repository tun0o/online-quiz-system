import api from './api';

/**
 * Enhanced User Profile Service với Real-time Sync
 * - Tự động sync dữ liệu từ User và OAuth2Account
 * - Đảm bảo tính nhất quán dữ liệu
 * - Cung cấp unified profile data
 */
export const enhancedUserProfileService = {
    /**
     * Lấy profile với real-time sync
     */
    async getMyProfile() {
        try {
            const response = await api.get('/api/user/profile');
            return response.data;
        } catch (error) {
            console.error('Error fetching user profile:', error);
            throw error;
        }
    },

    /**
     * Lấy unified profile data từ tất cả nguồn
     */
    async getUnifiedProfile() {
        try {
            const response = await api.get('/api/user/profile/unified');
            return response.data;
        } catch (error) {
            console.error('Error fetching unified profile:', error);
            throw error;
        }
    },

    /**
     * Cập nhật profile với sync
     */
    async updateProfile(profileData) {
        try {
            console.log('Enhanced updateProfile called with:', profileData);
            const response = await api.put('/api/user/profile', profileData);
            console.log('Enhanced updateProfile response:', response.data);
            return response.data;
        } catch (error) {
            console.error('Enhanced updateProfile error:', error);
            throw error;
        }
    },

    /**
     * Lấy thông tin hoàn thiện profile
     */
    async getProfileCompletion() {
        try {
            const response = await api.get('/api/user/profile/completion');
            return response.data;
        } catch (error) {
            console.error('Error fetching profile completion:', error);
            throw error;
        }
    },

    /**
     * Kiểm tra trạng thái sync
     */
    async getSyncStatus() {
        try {
            const response = await api.get('/api/user/profile/sync-status');
            return response.data;
        } catch (error) {
            console.error('Error checking sync status:', error);
            throw error;
        }
    },

    /**
     * Buộc sync dữ liệu
     */
    async forceSync() {
        try {
            const response = await api.post('/api/user/profile/force-sync');
            return response.data;
        } catch (error) {
            console.error('Error forcing sync:', error);
            throw error;
        }
    }
};

export default enhancedUserProfileService;



