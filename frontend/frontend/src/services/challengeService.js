import api from "./api";

export const challengeService = {
  getTodayChallenges: async () => {
    try {
      const response = await api.get('/api/challenges/daily');
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể tải nhiệm vụ hằng ngày.');
    }
  },
  
  updateProgress: async (challengeId, progressValue) => {
    try {
      const response = await api.post(`/api/challenges/${challengeId}/progress`, { progressValue });
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể cập nhật tiến độ nhiệm vụ.');
    }
  },
  
  getLeaderboard: async () => {
    try {
      const response = await api.get('/api/challenges/leaderboard');
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể tải bảng xếp hạng.');
    }
  }
};