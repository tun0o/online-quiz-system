import api from './api';

/**
 * File Upload Service
 * Cung cấp các API để upload file lên server
 */
export const fileUploadService = {
    /**
     * Upload avatar image
     */
    async uploadAvatar(file) {
        try {
            console.log('Uploading avatar file:', file);
            
            // Validate file
            if (!file) {
                throw new Error('File is required');
            }
            
            if (!file.type.startsWith('image/')) {
                throw new Error('File must be an image');
            }
            
            if (file.size > 5 * 1024 * 1024) { // 5MB
                throw new Error('File size must be less than 5MB');
            }
            
            // Create FormData
            const formData = new FormData();
            formData.append('file', file);
            
            // Upload file
            const response = await api.post('/api/upload/avatar', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });
            
            console.log('Avatar upload response:', response.data);
            return response.data;
            
        } catch (error) {
            console.error('Error uploading avatar:', error);
            throw error;
        }
    },

    /**
     * Delete avatar
     */
    async deleteAvatar() {
        try {
            console.log('Deleting avatar...');
            
            const response = await api.delete('/api/upload/avatar');
            console.log('Avatar delete response:', response.data);
            return response.data;
            
        } catch (error) {
            console.error('Error deleting avatar:', error);
            throw error;
        }
    },

    /**
     * Validate image file
     */
    validateImageFile(file) {
        const errors = [];
        
        if (!file) {
            errors.push('File is required');
            return errors;
        }
        
        if (!file.type.startsWith('image/')) {
            errors.push('File must be an image');
        }
        
        if (file.size > 5 * 1024 * 1024) { // 5MB
            errors.push('File size must be less than 5MB');
        }
        
        const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'];
        if (!allowedTypes.includes(file.type)) {
            errors.push('File type must be JPEG, PNG, GIF, or WebP');
        }
        
        return errors;
    },

    /**
     * Create preview URL for image
     */
    createPreviewUrl(file) {
        return URL.createObjectURL(file);
    },

    /**
     * Revoke preview URL
     */
    revokePreviewUrl(url) {
        if (url && url.startsWith('blob:')) {
            URL.revokeObjectURL(url);
        }
    }
};

export default fileUploadService;
