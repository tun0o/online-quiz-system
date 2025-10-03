import api from "./api";

export const gradingService = {
  /**
   * Fetches pending essay grading requests.
   * @returns {Promise<Array<any>>} A promise that resolves to the list of pending requests.
   */
  getPendingRequests: async () => {
    try {
      const response = await api.get('/api/admin/grading/requests');
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể tải danh sách bài cần chấm.');
    }
  },

  /**
   * Fetches the details of a quiz attempt for grading.
   * @param {number} attemptId - The ID of the quiz attempt.
   * @returns {Promise<any>} A promise that resolves to the attempt details.
   */
  getAttemptDetails: async (attemptId) => {
    try {
      const response = await api.get(`/api/admin/grading/attempts/${attemptId}`);
      return response.data;
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể tải chi tiết bài làm.');
    }
  },

  /**
   * Submits the grades for essay questions.
   * @param {object} submissionDTO - The DTO containing the grades.
   * @returns {Promise<void>} A promise that resolves when the submission is successful.
   */
  submitGrades: async (submissionDTO) => {
    try {
      await api.post('/api/admin/grading/submit', submissionDTO);
    } catch (error) {
      throw new Error(error.response?.data?.message || 'Không thể nộp điểm chấm.');
    }
  },
};