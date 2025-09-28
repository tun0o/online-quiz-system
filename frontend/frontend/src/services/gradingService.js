const API_BASE = (import.meta?.env?.VITE_API_BASE) || '/api';

export const gradingService = {
  getPendingRequests: async () => {
    const response = await fetch(`${API_BASE}/admin/grading/requests`);
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to fetch pending requests');
    }
    return response.json();
  },

  getAttemptDetails: async (attemptId) => {
    const response = await fetch(`${API_BASE}/admin/grading/attempts/${attemptId}`);
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to fetch attempt details');
    }
    return response.json();
  },

  submitGrades: async (payload) => {
    const response = await fetch(`${API_BASE}/admin/grading/submit`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.message || 'Failed to submit grades');
    }
    // Successful response has no body, so we don't return anything.
  },
};