import { useState, useEffect } from 'react';
import { Trophy, Medal, Crown, Star, Calendar, CalendarDays, CalendarClock, Gift } from 'lucide-react';
import { challengeService } from '@/services/challengeService';
import { useAuth } from '@/hooks/useAuth';
import { cn } from '@/utils/utils.js';

const periods = [
  { key: 'total', label: 'Toàn bộ', icon: <Trophy size={16} /> },
  { key: 'monthly', label: 'Tháng', icon: <Calendar size={16} /> },
  { key: 'weekly', label: 'Tuần', icon: <CalendarDays size={16} /> },
  { key: 'daily', label: 'Ngày', icon: <CalendarClock size={16} /> },
];

// Định nghĩa phần thưởng tương ứng với backend
const rewards = {
  monthly: { prizes: [1000, 500, 250], label: 'tháng' },
  weekly: { prizes: [250, 150, 75], label: 'tuần' },
  daily: { prizes: [50, 30, 15], label: 'ngày' },
};

export default function RankingPage() {
  const [rankings, setRankings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentUserRanking, setCurrentUserRanking] = useState(null);
  const [period, setPeriod] = useState('total'); // 'total', 'monthly', 'weekly', 'daily'
  const { user, isAuthenticated } = useAuth();

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const leaderboardPromise = challengeService.getLeaderboard(period);
        const myRankPromise = isAuthenticated() ? challengeService.getMyRank(period) : Promise.resolve(null);

        const [leaderboardData, myRankData] = await Promise.all([leaderboardPromise, myRankPromise]);

        setRankings(leaderboardData);

        // Chỉ cập nhật rank nếu người dùng có trong bảng xếp hạng
        const userInLeaderboard = leaderboardData.find(r => r.userId === user?.id);
        if (userInLeaderboard) {
          setCurrentUserRanking(userInLeaderboard);
        } else if (myRankData && myRankData.rank > 0) {
          setCurrentUserRanking(myRankData);
        } else {
          setCurrentUserRanking(null);
        }
      } catch (error) {
        console.error('Error fetching ranking data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isAuthenticated, period, user?.id]);

  const getPointsByPeriod = (user) => {
    switch (period) {
      case 'daily': return user.dailyPoints;
      case 'weekly': return user.weeklyPoints;
      case 'monthly': return user.monthlyPoints;
      default: return user.totalPoints;
    }
  };

  const currentPeriodLabel = periods.find(p => p.key === period)?.label || 'Xếp hạng tổng';

  const getRankIcon = (rank) => {
    switch (rank) {
      case 1: return <Crown className="text-yellow-500" size={24} />;
      case 2: return <Medal className="text-gray-400" size={24} />;
      case 3: return <Medal className="text-amber-600" size={24} />;
      default: return <span className="text-lg font-bold text-gray-600">#{rank}</span>;
    }
  };

  const getRankBg = (rank) => {
    switch (rank) {
      case 1: return 'bg-gradient-to-r from-yellow-50 to-yellow-100 border-yellow-200';
      case 2: return 'bg-gradient-to-r from-gray-50 to-gray-100 border-gray-200';
      case 3: return 'bg-gradient-to-r from-amber-50 to-amber-100 border-amber-200';
      default: return 'bg-white border-gray-200';
    }
  };

  return (
    <div className="flex flex-col h-full">
      <div className="flex-1 overflow-y-auto p-6 space-y-6">
        {/* Header */}
        <div className="flex items-center gap-3">
          <Trophy className="text-yellow-600" size={32} />
          <h1 className="text-2xl font-bold text-gray-800">Bảng xếp hạng</h1>
        </div>

        {/* Period Tabs */}
        <div className="flex space-x-1 rounded-lg bg-white border border-gray-200 p-1">
          {periods.map((p) => (
            <button
              key={p.key}
              onClick={() => setPeriod(p.key)}
              className={cn(
                'w-full rounded-md py-2.5 text-sm font-medium leading-5 text-white',
                'focus:outline-none focus:ring-2 ring-offset-2 ring-offset-blue-400 ring-white ring-opacity-60',
                period === p.key ? 'bg-white shadow' : 'text-gray-500 hover:bg-white/[0.5]'
              )}
            >
              <span className="flex items-center justify-center gap-2">{p.icon} {p.label}</span>
            </button>
          ))}
        </div>

        {loading && (
          <div className="text-sm text-gray-500">Đang cập nhật bảng xếp hạng...</div>
        )}

        {/* Reward Info Banner */}
        {period !== 'total' && (
          <div className="bg-green-50 border-l-4 border-green-500 text-green-800 p-4 rounded-r-lg" role="alert">
            <div className="flex items-center gap-3">
              <Gift size={20} />
              <p className="font-semibold">Phần thưởng sẽ được trao vào cuối {rewards[period]?.label} cho Top 3!</p>
            </div>
          </div>
        )}

        {/* Top 3 Podium */}
        {rankings.length >= 3 && (
          <div className="bg-white rounded-lg p-6 border border-gray-200 shadow-sm">
            <h2 className="text-lg font-semibold text-gray-800 mb-6 text-center">🏆 Top 3 🏆</h2>
            <div className="flex justify-center items-end gap-4">
              {/* 2nd Place */}
              <div className="text-center">
                <div className="w-20 h-16 bg-gray-200 rounded-t-lg flex items-end justify-center pb-2">
                  <Medal className="text-gray-400" size={24} />
                </div>
                <div className="bg-gray-100 p-3 rounded-b-lg">
                  <p className="font-semibold text-gray-800">{rankings[1]?.userName}</p>
                  <p className="text-sm text-gray-600">{getPointsByPeriod(rankings[1]).toLocaleString()} điểm</p>
                  {period !== 'total' && (
                    <p className="text-xs text-green-600 font-medium mt-1">
                      + {rewards[period]?.prizes[1].toLocaleString()} điểm thưởng
                    </p>
                  )}
                </div>
              </div>

              {/* 1st Place */}
              <div className="text-center">
                <div className="w-24 h-20 bg-yellow-200 rounded-t-lg flex items-end justify-center pb-2">
                  <Crown className="text-yellow-600" size={28} />
                </div>
                <div className="bg-yellow-100 p-4 rounded-b-lg">
                  <p className="font-bold text-gray-800">{rankings[0]?.userName}</p>
                  <p className="text-sm text-gray-600">{getPointsByPeriod(rankings[0]).toLocaleString()} điểm</p>
                  <p className="text-xs text-yellow-600 font-medium">👑 Vua điểm số</p>
                  {period !== 'total' && (
                    <p className="text-xs text-green-600 font-medium mt-1">
                      + {rewards[period]?.prizes[0].toLocaleString()} điểm thưởng
                    </p>
                  )}
                </div>
              </div>

              {/* 3rd Place */}
              <div className="text-center">
                <div className="w-20 h-12 bg-amber-200 rounded-t-lg flex items-end justify-center pb-2">
                  <Medal className="text-amber-600" size={24} />
                </div>
                <div className="bg-amber-100 p-3 rounded-b-lg">
                  <p className="font-semibold text-gray-800">{rankings[2]?.userName}</p>
                  <p className="text-sm text-gray-600">{getPointsByPeriod(rankings[2]).toLocaleString()} điểm</p>
                  {period !== 'total' && (
                    <p className="text-xs text-green-600 font-medium mt-1">
                      + {rewards[period]?.prizes[2].toLocaleString()} điểm thưởng
                    </p>
                  )}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Full Rankings List */}
        <div className="bg-white rounded-lg border border-gray-200 shadow-sm overflow-hidden">
          <div className="p-4 border-b border-gray-200 bg-gray-50">
            <h2 className="text-lg font-semibold text-gray-800">Xếp hạng {currentPeriodLabel}</h2>
          </div>
          <div className="divide-y divide-gray-200">
            {rankings.map((user) => (
              <div key={user.rank} className={`p-4 ${getRankBg(user.rank)} border-l-4 ${isAuthenticated() && user.userId === currentUserRanking?.userId ? 'border-blue-500 bg-blue-50' : 'border-transparent'}`}>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="flex items-center justify-center w-12 h-12">
                      {getRankIcon(user.rank)}
                    </div>
                    <div>
                      <h3 className="font-semibold text-gray-800">{user.userName}</h3>
                      <div className="flex items-center gap-4 text-sm text-gray-600">
                        <span>{getPointsByPeriod(user).toLocaleString()} điểm</span>
                        <div className="flex items-center gap-1">
                          <Star className="text-orange-400" size={14} />
                          <span>Streak: {user.currentStreak}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div className="text-right">
                    {/* <div className="text-2xl">{user.medal}</div> */}
                    <div className="text-sm text-gray-500">#{user.rank}</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Footer cố định cho xếp hạng của người dùng hiện tại */}
      {isAuthenticated() && currentUserRanking && (
        <div className="sticky bottom-0 bg-blue-600 text-white p-4 shadow-lg z-10 border-t border-blue-500">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="flex items-center justify-center w-10 h-10 bg-blue-700 rounded-full text-lg font-bold">
                #{currentUserRanking.rank}
              </div>
              <div>
                <h3 className="font-semibold text-lg">Vị trí của bạn ({currentPeriodLabel})</h3>
                <div className="flex items-center gap-4 text-sm text-blue-100">
                  <span>{getPointsByPeriod(currentUserRanking).toLocaleString()} điểm</span>
                  <div className="flex items-center gap-1">
                    <Star className="text-yellow-300" size={14} />
                    <span>Streak: {currentUserRanking.currentStreak}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}