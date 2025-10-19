import api from "./api";

export const quizService = {
  // Public APIs
  /**
   * Fetches publicly available and approved quizzes with search, filter, and pagination.
   * @param {object} params - The query parameters.
   * @param {number} params.page - The page number to fetch.
   * @param {number} params.size - The number of items per page.
   * @param {string} [params.keyword] - The search keyword.
   * @param {string} [params.subject] - The subject to filter by.
   * @returns {Promise<any>} A promise that resolves to the paginated quiz data.
   */
  getPublicQuizzes: async (params) => {
    try {
      const response = await api.get('/api/quiz-submissions/public', { params });
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể tải danh sách đề thi công khai.');
    }
  },

  // Contributor APIs
  createSubmission: async (quizData) => {
    try {
      const response = await api.post('/api/quiz-submissions', quizData);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể tạo đề thi mới.');
    }
  },

  /**
   * Starts a new quiz attempt.
   * @param {number|string} quizId The ID of the quiz to start.
   * @returns {Promise<{attemptId: number, quizData: object}>} A promise that resolves to the attempt ID and quiz data.
   */
  startAttempt: async (quizId) => {
    try {
      const response = await api.post(`/api/quizzes/${quizId}/start`);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể bắt đầu làm bài.');
    }
  },

  /**
   * Submits the answers for a specific quiz attempt.
   * @param {number|string} attemptId The ID of the attempt being submitted.
   * @param {object} payload The submission payload containing answers.
   * @returns {Promise<any>} A promise that resolves to the quiz result.
   */
  submitAttempt: async (attemptId, payload) => {
    try {
      const response = await api.post(`/api/attempts/${attemptId}/submit`, payload);
      return response.data;
    } catch (error) {
      throw error; // Re-throw the original error to be handled in the component
    }
  },

  requestEssayGrading: async (attemptId) => {
    try {
      const response = await api.post(`/api/attempts/${attemptId}/request-grading`);
      return response.data;
    } catch (error) {
      throw error; // Re-throw the original error to be handled in the component
    }
  },

  getMySubmissions: async (page = 0, size = 10) => {
    try {
      const response = await api.get('/api/quiz-submissions/my-submissions', { params: { page, size } });
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể tải danh sách đề thi của bạn.');
    }
  },

  // Admin APIs
  getPendingSubmissions: async (page = 0, size = 10) => {
    try {
      const response = await api.get('/api/quiz-submissions/pending', { params: { page, size } });
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể tải danh sách đề thi chờ duyệt.');
    }
  },

  getSubmissionDetail: async (id) => {
    try {
      const response = await api.get(`/api/quiz-submissions/${id}`);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể tải chi tiết đề thi.');
    }
  },

  // API for admin to search all submissions with filters
  searchSubmissions: async (params) => {
    try {
      const response = await api.get('/api/quiz-submissions/public', { params });
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể tìm kiếm đề thi.');
    }
  },

  updateSubmission: async (id, quizData) => {
    try {
      const response = await api.put(`/api/quiz-submissions/${id}`, quizData);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể cập nhật đề thi.');
    }
  },

  deleteSubmission: async (id) => {
    try {
      await api.delete(`/api/quiz-submissions/${id}`);
      return true;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể xóa đề thi.');
    }
  },

  approveSubmission: async (id) => {
    try {
      const response = await api.put(`/api/quiz-submissions/${id}/approve`);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể duyệt đề thi.');
    }
  },

  rejectSubmission: async (id, reason) => {
    try {
      const response = await api.put(`/api/quiz-submissions/${id}/reject`, { reason });
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể từ chối đề thi.');
    }
  },

  getPendingCount: async () => {
    try {
      const response = await api.get('/api/quiz-submissions/stats/pending-count');
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể lấy số lượng đề chờ duyệt.');
    }
  }
};
