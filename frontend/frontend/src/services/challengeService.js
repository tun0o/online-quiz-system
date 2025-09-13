const API_BASE = '/api';

export const challengeService = {
  getTodayChallenges: async () => {
    const response = await fetch(`${API_BASE}/challenges/daily`);
    if (!response.ok) throw new Error('Failed to fetch challenges');
    return response.json();
  },
  
  updateProgress: async (challengeId, progressValue) => {
    const response = await fetch(`${API_BASE}/challenges/${challengeId}/progress`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ progressValue })
    });
    if (!response.ok) throw new Error('Failed to update progress');
    return response.json();
  },
  
  getLeaderboard: async () => {
    const response = await fetch(`${API_BASE}/challenges/leaderboard`);
    if (!response.ok) throw new Error('Failed to fetch leaderboard');
    return response.json();
  }
};