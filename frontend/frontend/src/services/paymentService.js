import api from './api';

export const paymentService = {
    createPayment: async (data) => {
        try {
            const response = await api.post('/api/payment/create-vnpay', data);
            return response.data;
        } catch (error) {
            throw new Error(error.response?.data?.message || 'Không thể tạo giao dịch thanh toán.');
        }
    },
};