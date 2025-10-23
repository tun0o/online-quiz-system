import { useState, useEffect } from 'react';
import { Trophy, Medal, Crown, Star } from 'lucide-react';
import { challengeService } from '@/services/challengeService';
import { useAuth } from '@/hooks/useAuth';

export default function RankingPage() {
  const [rankings, setRankings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [currentUserRanking, setCurrentUserRanking] = useState(null);
  const { user, isAuthenticated } = useAuth();

  useEffect(() => {
    const fetchData = async () => {
      try {
        const leaderboardPromise = challengeService.getLeaderboard();
        const myRankPromise = isAuthenticated() ? challengeService.getMyRank() : Promise.resolve(null);

        const [leaderboardData, myRankData] = await Promise.all([leaderboardPromise, myRankPromise]);

        setRankings(leaderboardData);
        if (myRankData && myRankData.rank > 0) {
          setCurrentUserRanking(myRankData);
        }
      } catch (error) {
        console.error('Error fetching ranking data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isAuthenticated]);


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

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-64">
        <div className="text-gray-500">Đang tải bảng xếp hạng...</div>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full">
      <div className="flex-1 overflow-y-auto p-6 space-y-6">
        {/* Header */}
        <div className="flex items-center gap-3">
          <Trophy className="text-yellow-600" size={32} />
          <div>
            <h1 className="text-2xl font-bold text-gray-800">Bảng xếp hạng</h1>
          </div>
        </div>

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
                  <p className="text-sm text-gray-600">{rankings[1]?.totalPoints.toLocaleString()} điểm</p>
                </div>
              </div>

              {/* 1st Place */}
              <div className="text-center">
                <div className="w-24 h-20 bg-yellow-200 rounded-t-lg flex items-end justify-center pb-2">
                  <Crown className="text-yellow-600" size={28} />
                </div>
                <div className="bg-yellow-100 p-4 rounded-b-lg">
                  <p className="font-bold text-gray-800">{rankings[0]?.userName}</p>
                  <p className="text-sm text-gray-600">{rankings[0]?.totalPoints.toLocaleString()} điểm</p>
                  <p className="text-xs text-yellow-600 font-medium">👑 Vua điểm số</p>
                </div>
              </div>

              {/* 3rd Place */}
              <div className="text-center">
                <div className="w-20 h-12 bg-amber-200 rounded-t-lg flex items-end justify-center pb-2">
                  <Medal className="text-amber-600" size={24} />
                </div>
                <div className="bg-amber-100 p-3 rounded-b-lg">
                  <p className="font-semibold text-gray-800">{rankings[2]?.userName}</p>
                  <p className="text-sm text-gray-600">{rankings[2]?.totalPoints.toLocaleString()} điểm</p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Full Rankings List */}
        <div className="bg-white rounded-lg border border-gray-200 shadow-sm overflow-hidden">
          <div className="p-4 border-b border-gray-200 bg-gray-50">
            <h2 className="text-lg font-semibold text-gray-800">Bảng xếp hạng chi tiết</h2>
          </div>
          <div className="divide-y divide-gray-200">
            {rankings.map((user) => (
              <div key={user.rank} className={`p-4 ${getRankBg(user.rank)} border-l-4 ${isAuthenticated() && user.userName === currentUserRanking?.userName ? 'border-blue-500 bg-blue-50' : ''}`}>
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4">
                    <div className="flex items-center justify-center w-12 h-12">
                      {getRankIcon(user.rank)}
                    </div>
                    <div>
                      <h3 className="font-semibold text-gray-800">{user.userName}</h3>
                      <div className="flex items-center gap-4 text-sm text-gray-600">
                        <span>Tổng: {user.totalPoints.toLocaleString()} điểm</span>
                        <span>Tuần: {user.weeklyPoints.toLocaleString()} điểm</span>
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
                <h3 className="font-semibold text-lg">{currentUserRanking.userName}</h3>
                <div className="flex items-center gap-4 text-sm text-blue-100">
                  <span>Tổng: {currentUserRanking.totalPoints.toLocaleString()} điểm</span>
                  <span>Tuần: {currentUserRanking.weeklyPoints.toLocaleString()} điểm</span>
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