import api from './api';

/**
 * User Profile Service
 * Cung cấp các API để quản lý thông tin profile của user
 */
export const userProfileService = {
    /**
     * Lấy thông tin profile của user hiện tại
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
     * Lấy thông tin cơ bản của profile
     */
    async getProfileInfo() {
        try {
            const response = await api.get('/api/user/profile/info');
            return response.data;
        } catch (error) {
            console.error('Error fetching profile info:', error);
            throw error;
        }
    },

    /**
     * Cập nhật thông tin profile
     */
    async updateProfile(profileData) {
        try {
            console.log('userProfileService.updateProfile called with:', profileData);
            const response = await api.put('/api/user/profile', profileData);
            console.log('userProfileService.updateProfile response:', response.data);
            return response.data;
        } catch (error) {
            console.error('userProfileService.updateProfile error:', error);
            console.error('Error response:', error.response?.data);
            console.error('Error status:', error.response?.status);
            throw error;
        }
    }
};

export default userProfileService;
