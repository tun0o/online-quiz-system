// Use Vite proxy in development and allow overriding via env
const API_BASE = (import.meta?.env?.VITE_API_BASE) || '/api';

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
    // Lọc ra các tham số có giá trị (không phải null, undefined, hoặc chuỗi rỗng)
    const cleanParams = Object.fromEntries(
      Object.entries(params).filter(([_, v]) => v != null && v !== '')
    );
    const query = new URLSearchParams(cleanParams).toString();
    const response = await fetch(`${API_BASE}/quiz-submissions/public?${query}`);
    if (!response.ok) throw new Error('Failed to fetch public quizzes');
    return response.json();
  },

  // Contributor APIs
  submitQuiz: async (quizData) => {
    const response = await fetch(`${API_BASE}/quiz-submissions`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(quizData)
    });
    if (!response.ok) throw new Error('Failed to submit quiz');
    return response.json();
  },

  getMySubmissions: async (contributorId = 1, page = 0, size = 10) => {
    const response = await fetch(
      `${API_BASE}/quiz-submissions/contributor/${contributorId}?page=${page}&size=${size}`
    );
    if (!response.ok) throw new Error('Failed to fetch submissions');
    return response.json();
  },

  // Admin APIs
  getPendingSubmissions: async (page = 0, size = 10) => {
    const response = await fetch(
      `${API_BASE}/quiz-submissions/pending?page=${page}&size=${size}`
    );
    if (!response.ok) throw new Error('Failed to fetch pending submissions');
    return response.json();
  },

  getSubmissionDetail: async (id) => {
    const response = await fetch(`${API_BASE}/quiz-submissions/${id}`);
    if (!response.ok) throw new Error('Failed to fetch submission detail');
    return response.json();
  },

  // API for admin to search all submissions with filters
  searchSubmissions: async (params) => {
    // Lọc ra các tham số có giá trị (không phải null, undefined, hoặc chuỗi rỗng)
    const cleanParams = Object.fromEntries(
      Object.entries(params).filter(([_, v]) => v != null && v !== '')
    );
    const query = new URLSearchParams(cleanParams).toString();
    const response = await fetch(`${API_BASE}/quiz-submissions/public?${query}`);
    if (!response.ok) {
      // Try to parse error message from backend
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to search submissions');
    }
    return response.json();
  },

  updateSubmission: async (id, quizData) => {
    const response = await fetch(`${API_BASE}/quiz-submissions/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(quizData)
    });
    if (!response.ok) throw new Error('Failed to update submission');
    return response.json();
  },

  deleteSubmission: async (id) => {
    const response = await fetch(`${API_BASE}/quiz-submissions/${id}`, {
      method: 'DELETE'
    });
    if (!response.ok) throw new Error('Failed to delete submission');
    return response.ok;
  },

  approveSubmission: async (id) => {
    const response = await fetch(`${API_BASE}/quiz-submissions/${id}/approve`, {
      method: 'PUT'
    });
    if (!response.ok) throw new Error('Failed to approve submission');
    return response.json();
  },

  rejectSubmission: async (id, reason) => {
    const response = await fetch(`${API_BASE}/quiz-submissions/${id}/reject`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ reason })
    });
    if (!response.ok) throw new Error('Failed to reject submission');
    return response.json();
  },

  getPendingCount: async () => {
    const response = await fetch(`${API_BASE}/quiz-submissions/stats/pending-count`);
    if (!response.ok) throw new Error('Failed to fetch pending count');
    return response.json();
  }
};