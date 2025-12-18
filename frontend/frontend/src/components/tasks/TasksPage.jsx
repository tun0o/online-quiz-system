import { useState, useEffect } from 'react';
import { TargetIcon, Trophy, Clock, CheckCircle } from 'lucide-react';
import { challengeService } from '@/services/challengeService';
import { useAuth } from '@/hooks/useAuth';

export default function TasksPage() {
  const [challenges, setChallenges] = useState([]);
  const [loading, setLoading] = useState(true);
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    const loadData = async () => {
      if (isAuthenticated()) {
        await loadChallenges();
      }
    };
    loadData();
  }, [isAuthenticated]); // Vẫn giữ dependency để load lại khi đăng nhập/đăng xuất

  const loadChallenges = async () => {
    try {
      const data = await challengeService.getTodayChallenges();
      // Ensure that `challenges` is always an array to prevent errors.
      setChallenges(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error loading challenges:', error);
    } finally {
      setLoading(false);
    }
  };

  const getDifficultyColor = (level) => {
    switch (level) {
      case 'EASY': return 'text-green-600 bg-green-100';
      case 'MEDIUM': return 'text-yellow-600 bg-yellow-100';
      case 'HARD': return 'text-red-600 bg-red-100';
      default: return 'text-gray-600 bg-gray-100';
    }
  };

  const getDifficultyText = (level) => {
    switch (level) {
      case 'EASY': return 'Dễ';
      case 'MEDIUM': return 'Trung bình';
      case 'HARD': return 'Khó';
      default: return level;
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-64">
        <div className="text-gray-500">Đang tải nhiệm vụ...</div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center gap-3">
        <TargetIcon className="text-green-600" size={32} />
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Nhiệm vụ hằng ngày</h1>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="bg-white rounded-lg p-6 border border-gray-200 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-green-100 rounded-lg">
              <CheckCircle className="text-green-600" size={24} />
            </div>
            <div>
              <p className="text-2xl font-bold text-gray-800">
                {challenges.filter(c => c.isCompleted).length}
              </p>
              <p className="text-sm text-gray-600">Đã hoàn thành</p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg p-6 border border-gray-200 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-blue-100 rounded-lg">
              <Clock className="text-blue-600" size={24} />
            </div>
            <div>
              <p className="text-2xl font-bold text-gray-800">
                {challenges.length - challenges.filter(c => c.isCompleted).length}
              </p>
              <p className="text-sm text-gray-600">Đang thực hiện</p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg p-6 border border-gray-200 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-yellow-100 rounded-lg">
              <Trophy className="text-yellow-600" size={24} />
            </div>
            <div>
              <p className="text-2xl font-bold text-gray-800">
                {challenges.reduce((sum, c) => sum + (c.isCompleted ? c.rewardPoints : 0), 0)}
              </p>
              <p className="text-sm text-gray-600">Điểm đã nhận</p>
            </div>
          </div>
        </div>
      </div>

      {/* Challenges List */}
      <div className="space-y-4">
        {challenges.map((challenge) => (
          <div key={challenge.id} className="bg-white rounded-lg p-6 border border-gray-200 shadow-sm">
            <div className="flex items-start justify-between mb-4">
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-2">
                  <h3 className="text-lg font-semibold text-gray-800">{challenge.title}</h3>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${getDifficultyColor(challenge.difficultyLevel)}`}>
                    {getDifficultyText(challenge.difficultyLevel)}
                  </span>
                  {challenge.isCompleted && (
                    <CheckCircle className="text-green-500" size={20} />
                  )}
                </div>
                <p className="text-gray-600 mb-3">{challenge.description || challenge.title}</p>
              </div>
              <div className="text-right">
                <div className="text-lg font-bold text-green-600">+{challenge.rewardPoints}</div>
                <div className="text-sm text-gray-500">điểm</div>
              </div>
            </div>

            {/* Progress Bar */}
            <div className="mb-3">
              <div className="flex justify-between items-center mb-2">
                <span className="text-sm text-gray-600">Tiến độ</span>
                <span className="text-sm font-medium text-gray-800">
                  {challenge.currentProgress}/{challenge.targetValue}
                </span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-3">
                <div 
                  className={`h-3 rounded-full transition-all duration-500 ${
                    challenge.isCompleted ? 'bg-green-500' : 'bg-green-400'
                  }`}
                  style={{ width: `${challenge.progressPercentage}%` }}
                />
              </div>
            </div>

            {challenge.isCompleted && (
              <div className="flex items-center gap-2 text-green-600 text-sm font-medium">
                <CheckCircle size={16} />
                Nhiệm vụ đã hoàn thành!
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}