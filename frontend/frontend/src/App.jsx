// App.jsx
import { Routes, Route, NavLink, Outlet, useLocation, Navigate, useNavigate, Link, useParams } from "react-router-dom";
import { useState, useEffect, useCallback } from "react";
import { Home, Star, Shield, ClipboardList, User, Settings, UserPlus, LogIn, TrophyIcon, TargetIcon, ServerCrash, LogOut } from "lucide-react";
import { QuizProvider } from "@/contexts/QuizContext";
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

// Import components
import ContributorDashboard from "@/components/contributor/ContributorDashboard";
import ProtectedRoute from "@/components/auth/ProtectedRoute";
import { useAuth } from "@/hooks/useAuth";
import AdminLayout from "@/components/admin/AdminLayout";
import ModerationPanel from "@/components/admin/ModerationPanel";
import AllSubmissionsTable from "@/components/admin/AllSubmissionsTable";
import QuizSubmissionForm from "@/components/contributor/QuizSubmissionForm";
import { quizService } from "@/services/quizService";
import QuizTakingPage from "@/components/quiz/QuizTakingPage";
import { subjectDisplayMap, difficultyDisplayMap, getDifficultyColor } from "@/utils/displayMaps";
import { challengeService } from "@/services/challengeService";
import TasksPage from "@/components/tasks/TasksPage";
import RankingPage from "@/components/ranking/RankingPage";
import GradingListPage from "@/components/admin/GradingListPage";
import GradingDetailPage from "@/components/admin/GradingDetailPage";
import Login from "@/components/auth/Login";
import Register from "@/components/auth/Register";
import ConfirmEmail from "@/components/auth/ConfirmEmail";
import ForgotPassword from "@/components/auth/ForgotPassword";
import ResetPassword from "@/components/auth/ResetPassword";
import ChangePassword from "@/components/auth/ChangePassword";
import OAuth2Success from "@/components/auth/OAuth2Success";
import OAuth2Error from "@/components/auth/OAuth2Error";
import Logout from "@/components/auth/Logout";
import NotFoundPage from "@/components/common/NotFoundPage";
import UserDashboard from "@/components/user/UserDashboard";
import AdminDashboard from "@/components/admin/AdminDashboard";
import UserView from "@/components/admin/UserView";

/**
 * Layout chung cho toàn bộ ứng dụng, bao gồm Sidebar và khu vực nội dung chính.
 */
function AppLayout() {
  const location = useLocation();
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const showRightSidebar = !['/contribute'].includes(location.pathname);

  // State cho challenges và rankings thật
  const [challenges, setChallenges] = useState([]);
  const [rankings, setRankings] = useState([]);
  const [loadingChallenges, setLoadingChallenges] = useState(true);
  const [loadingRankings, setLoadingRankings] = useState(true);

  // Load dữ liệu thật từ API
  useEffect(() => {
    if (showRightSidebar) {
      loadRankings(); // Bảng xếp hạng có thể tải bất kể đăng nhập
      // Phụ thuộc vào `user` thay vì `isAuthenticated` để đảm bảo `user` đã được set
      if (user) {
        loadChallenges();
      }
    }
  }, [showRightSidebar, user]); // Thay isAuthenticated bằng user

  const loadChallenges = async () => {
    if (!user) return; // Kiểm tra trực tiếp user
    try {
      const data = await challengeService.getTodayChallenges();
      setChallenges(data);
    } catch (error) {
      console.error('Error loading challenges (user might not be logged in):', error);
    } finally {
      setLoadingChallenges(false);
    }
  };

  const loadRankings = async () => {
    try {
      const data = await challengeService.getLeaderboard();
      setRankings(data);
    } catch (error) {
      console.error('Error loading rankings:', error);
    } finally {
      setLoadingRankings(false);
    }
  };

  const handleLogout = async () => {
    try {
      await logout();
      navigate("/login");
      toast.success("Đã đăng xuất thành công");
    } catch {
      toast.error("Đăng xuất thất bại");
    }
  };

  // Xây dựng menu động dựa trên vai trò và trạng thái đăng nhập
  const buildMenu = () => {
    const publicItems = [
      { icon: <Home size={20} />, label: "HỌC", path: "/" },
      { icon: <Shield size={20} />, label: "BẢNG XẾP HẠNG", path: "/ranking" },
    ];

    const userItems = [
      { icon: <ClipboardList size={20} />, label: "NHIỆM VỤ", path: "/tasks" },
      { icon: <Star size={20} />, label: "ĐÓNG GÓP", path: "/contribute" },
      { icon: <User size={20} />, label: "HỒ SƠ", path: "/user/dashboard" },
    ];

    const adminItem = { icon: <Settings size={20} />, label: "QUẢN TRỊ", path: "/admin" };

    let menu = [...publicItems];
    if (isAuthenticated()) {
      menu.push(...userItems);
      if (user?.roles?.some(role => role.includes('ADMIN'))) {
        menu.push(adminItem);
      }
    }
    return menu;
  };

  const menu = buildMenu();

  return (
    <div className="min-h-screen bg-gray-50 text-gray-800 flex">
      {/* Sidebar */}
      <aside className="w-64 bg-white flex flex-col p-6 space-y-6 border-r border-gray-200 shadow-sm flex-shrink-0">
        <a href="/"><h1 className="text-2xl font-bold text-green-600">Practizz</h1></a>
        <nav className="flex flex-col gap-2">
          {menu.map((item) => (
            <NavLink
              key={item.label}
              to={item.path}
              end
              className={({ isActive }) =>
                `flex items-center gap-3 px-4 py-3 rounded-lg transition ${isActive
                  ? "bg-green-50 text-green-600 border border-green-200 shadow-sm"
                  : "text-gray-700 hover:bg-green-50 hover:text-green-600"
                }`
              }
            >
              {item.icon}
              {item.label}
            </NavLink>
          ))}
        </nav>

        {/* Logout button (xuống dưới cùng) */}
        {isAuthenticated() && ( // Chỉ hiển thị nút Đăng xuất khi đã đăng nhập
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 p-4 space-y-2 py-2 rounded-lg text-gray-700 hover:bg-red-50 hover:text-red-600 transition"
          >
            <LogOut size={20} />
            Đăng xuất
          </button>
        )}
      </aside>

      {/* Main content area */}
      <div className="flex-1 flex">
        <main className={`flex-1 overflow-y-auto min-h-screen ${showRightSidebar ? 'p-6' : 'p-4'}`}>
          <Outlet />
        </main>

        {/* Right sidebar với dữ liệu thật */}
        {showRightSidebar && (
          <aside className="w-80 bg-white border-l border-gray-200 shadow-sm min-h-screen flex-shrink-0">
            <div className="w-80 p-6 space-y-6">
              {/* Login/Register buttons hoặc User info */}
              {!isAuthenticated() ? (
                <div className="flex gap-3">
                  {/* Đăng ký */}
                  <button
                    onClick={() => navigate("/register")}
                    className="flex-1 flex justify-center items-center gap-2 px-5 py-2
               bg-gray-100 text-gray-800 rounded-lg
               hover:bg-gray-200 transition shadow-sm whitespace-nowrap"
                  >
                    <UserPlus size={16} />
                    Đăng ký
                  </button>

                  {/* Đăng nhập */}
                  <button
                    onClick={() => navigate("/login")}
                    disabled={false} // test trước để chắc chắn không bị disable
                    className="flex-1 flex justify-center items-center gap-2 px-5 py-2
               !bg-green-600 !text-white rounded-lg font-medium
               hover:!bg-green-700 active:!bg-green-800
               transition shadow-sm whitespace-nowrap"
                  >
                    <LogIn size={16} />
                    Đăng nhập
                  </button>
                </div>

              ) : (
                <div className="bg-green-50 p-4 rounded-lg border border-green-200">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-green-600 rounded-full flex items-center justify-center text-white font-bold">
                      {user?.name?.charAt(0).toUpperCase() || user?.email?.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <p className="font-medium text-gray-800">{user?.name || user?.email}</p>
                      <p className="text-sm text-gray-600">
                        {user?.roles?.includes('ADMIN') ? 'Quản trị viên' : 'Người dùng'}
                      </p>
                    </div>
                  </div>
                </div>
              )}

              {/* Ranking với dữ liệu thật */}
              <div className="bg-gradient-to-br from-yellow-50 to-orange-50 rounded-xl p-5 border border-yellow-200">
                <div className="flex items-center gap-2 mb-4">
                  <TrophyIcon className="text-yellow-600" size={20} />
                  <h3 className="font-bold text-gray-800">Bảng xếp hạng</h3>
                </div>
                <div className="space-y-3">
                  {loadingRankings ? (
                    <div className="text-center text-gray-500">Đang tải...</div>
                  ) : rankings.length > 0 ? (
                    rankings.slice(0, 3).map((user) => (
                      <div key={user.rank} className="flex items-center justify-between p-3 bg-white rounded-lg shadow-sm border border-yellow-100">
                        <div className="flex items-center gap-3">
                          <span className="text-lg">{user.medal || `${user.rank}.`}</span>
                          <div>
                            <p className="font-medium text-gray-800">{user.userName}</p>
                            <p className="text-sm text-gray-600">{user.totalPoints.toLocaleString()} điểm</p>
                          </div>
                        </div>
                        {user.rank <= 3 && (
                          <div className="text-right">
                            <p className="text-xs text-gray-500">Streak: {user.currentStreak}</p>
                          </div>
                        )}
                      </div>
                    ))
                  ) : (
                    <div className="text-center text-gray-500">Chưa có dữ liệu</div>
                  )}
                </div>
              </div>

              {/* Nhiệm vụ với dữ liệu thật */}
              <div className="bg-gradient-to-br from-green-50 to-emerald-50 rounded-xl p-5 border border-green-200">
                <div className="flex justify-between items-center mb-4">
                  <div className="flex items-center gap-2">
                    <TargetIcon className="text-green-600" size={20} />
                    <h3 className="font-bold text-gray-800">Nhiệm vụ hằng ngày</h3>
                  </div>
                  <Link to="/tasks" className="text-blue-600 text-sm font-medium hover:text-blue-700">XEM TẤT CẢ</Link>
                </div>
                <div className="space-y-4">
                  {loadingChallenges ? (
                    <div className="text-center text-gray-500">Đang tải...</div>
                  ) : challenges.length > 0 ? (
                    challenges.map((challenge) => (
                      <div key={challenge.id} className="p-3 bg-white rounded-lg shadow-sm border border-green-100">
                        <div className="flex justify-between items-center mb-2">
                          <p className="text-sm font-medium text-gray-800">{challenge.title}</p>
                          <span className="text-xs text-green-600 font-medium">+{challenge.rewardPoints} điểm</span>
                        </div>
                        <div className="w-full bg-gray-200 rounded-full h-2 mb-2">
                          <div
                            className={`h-2 rounded-full transition-all duration-300 ${challenge.isCompleted ? 'bg-green-500' : 'bg-green-400'
                              }`}
                            style={{ width: `${challenge.progressPercentage}%` }}
                          />
                        </div>
                        <div className="flex justify-between items-center">
                          <p className="text-xs text-gray-600">
                            {challenge.currentProgress}/{challenge.targetValue}
                          </p>
                          {challenge.isCompleted && (
                            <span className="text-xs text-green-600 font-medium">✓ Hoàn thành</span>
                          )}
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="text-center text-gray-500">Chưa có nhiệm vụ</div>
                  )}
                </div>
              </div>
            </div>
          </aside>
        )}
      </div>
    </div>
  );
}

// Component cho trang chủ
function HomePage() {
  const navigate = useNavigate();
  const [quizzes, setQuizzes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [query, setQuery] = useState({
    keyword: '',
    subject: '',
    difficulty: '',
    page: 0,
  });
  const [searchTerm, setSearchTerm] = useState('');
  const [pagination, setPagination] = useState({ size: 12, totalPages: 0 });

  useEffect(() => {
    const handler = setTimeout(() => {
      setQuery(q => ({ ...q, keyword: searchTerm, page: 0 }));
    }, 500);
    return () => clearTimeout(handler);
  }, [searchTerm]);

  // The single effect to fetch data whenever the query changes
  useEffect(() => {
    const fetchQuizzes = async () => {
      setLoading(true);
      try {
        const params = {
          ...query,
          size: pagination.size,
        };
        const data = await quizService.getPublicQuizzes(params);
        setQuizzes(data.content || []);
        setPagination(p => ({ ...p, totalPages: data.totalPages }));

        if (query.page !== data.number) {
          setQuery(q => ({ ...q, page: data.number }));
        }
      } catch (error) {
        toast.error("Không thể tải danh sách đề thi.");
        console.error("Failed to fetch quizzes:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchQuizzes();
  }, [query, pagination.size]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setQuery(q => ({ ...q, [name]: value, page: 0 }));
  };

  const handlePageChange = (newPage) => {
    setQuery(q => ({ ...q, page: newPage }));
  };

  return (
    <>
      <div className="flex flex-col md:flex-row justify-between items-center gap-4 mb-6">
        <div className="flex-1 w-full">
          <input
            type="text"
            placeholder="Tìm kiếm theo tiêu đề, mô tả..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full p-3 rounded-lg bg-white border border-gray-200 text-gray-800 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent shadow-sm"
          />
        </div>
        <div className="flex gap-4 w-full md:w-auto">
          <select
            name="subject"
            value={query.subject}
            onChange={handleFilterChange}
            className="flex-1 md:w-48 p-3 border border-gray-200 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-green-500 shadow-sm"
          >
            <option value="">Tất cả môn học</option>
            {Object.entries(subjectDisplayMap).map(([value, label]) => (
              <option key={value} value={value}>{label}</option>
            ))}
          </select>
          <select
            name="difficulty"
            value={query.difficulty}
            onChange={handleFilterChange}
            className="flex-1 md:w-48 p-3 border border-gray-200 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-green-500 shadow-sm"
          >
            <option value="">Tất cả độ khó</option>
            {Object.entries(difficultyDisplayMap).map(([value, label]) => (
              <option key={value} value={value}>{label}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Đề thi nổi bật */}
      <section className="mb-6">
        <h2 className="text-xl font-semibold mb-4 text-gray-800">Đề thi nổi bật</h2>
        <div className="flex gap-4 overflow-x-auto pb-4">
          {["Ôn luyện đạo hàm", "Ôn luyện điện tử", "Hóa hữu cơ"].map(
            (title, i) => (
              <div
                key={i}
                className="min-w-[220px] border border-gray-200 rounded-lg p-4 bg-white hover:shadow-md transition-shadow cursor-pointer"
              >
                <h3 className="font-bold text-gray-800">{title}</h3>
                <p className="text-gray-600 text-sm">Thời gian: 50p</p>
                <p className="text-gray-600 text-sm">50 câu hỏi</p>
              </div>
            )
          )}
        </div>
      </section>

      {/* Danh sách đề thi */}
      <section>
        <h2 className="text-xl font-semibold mb-4 text-gray-800">Danh sách đề thi</h2>
        {loading ? (
          <div className="text-center py-10">Đang tải...</div>
        ) : quizzes.length > 0 ? (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
              {quizzes.filter(Boolean).map((quiz) => (
                <Link to={`/quiz/${quiz.id}`} key={quiz.id}>
                  <div
                    className="border border-gray-200 rounded-lg p-4 bg-white hover:shadow-lg hover:-translate-y-1 transition-all duration-200 cursor-pointer h-full flex flex-col"
                  >
                    <h3 className="font-bold text-gray-800 mb-2">{quiz.title}</h3>
                    <p className="text-gray-600 text-sm mb-3 flex-grow">{quiz.description}</p>
                    <div className="text-sm text-gray-500 border-t border-gray-100 pt-2 mt-auto space-y-1">
                      <p>Môn: {subjectDisplayMap[quiz.subject] || quiz.subject}</p>
                      {quiz.difficultyLevel && (
                        <div className="flex justify-between items-center">
                          <span>Độ khó:</span>
                          <span className={`px-2 py-1 rounded text-xs font-medium ${getDifficultyColor(quiz.difficultyLevel)}`}>
                            {difficultyDisplayMap[quiz.difficultyLevel]}
                          </span>
                        </div>
                      )}
                      <p>Thời gian: {quiz.durationMinutes} phút</p>
                      <p>{quiz.questions?.length || 0} câu hỏi</p>
                    </div>
                  </div>
                </Link>
              ))}
            </div>
            {/* Pagination */}
            {pagination.totalPages > 1 && (
              <div className="flex justify-center items-center gap-2 mt-6">
                {Array.from({ length: pagination.totalPages }, (_, i) => (
                  <button
                    key={i}
                    onClick={() => handlePageChange(i)}
                    className={`px-3 py-1 rounded ${i === query.page
                        ? 'bg-green-600 text-white'
                        : 'bg-white border border-gray-200 text-gray-600 hover:bg-gray-50'
                      }`}
                  >
                    {i + 1}
                  </button>
                ))}
              </div>
            )}
          </>
        ) : (
          <div className="text-center py-10 text-gray-500">Không tìm thấy đề thi nào.</div>
        )}
      </section>
    </>
  );
}

function AppRoutes() {
  const navigate = useNavigate();

  return (
    <Routes>
      {/* === PUBLIC ROUTES: Không yêu cầu đăng nhập và không có layout chung === */}
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/confirm" element={<ConfirmEmail />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="/oauth2/success" element={<OAuth2Success />} />
      <Route path="/oauth2/error" element={<OAuth2Error />} />
      <Route path="/logout" element={<Logout />} />

      {/* === ADMIN ROUTES: Yêu cầu quyền ADMIN, có layout riêng === */}
      <Route
        path="/admin"
        element={
          <ProtectedRoute requiredRole="ADMIN">
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="dashboard" replace />} />
        <Route path="dashboard" element={<AdminDashboard />} />
        <Route path="moderation" element={<ModerationPanel />} />
        <Route path="management" element={<AllSubmissionsTable />} />
        <Route path="management/edit/:submissionId" element={<QuizSubmissionForm onSuccess={() => navigate("/admin/management")} />} />
        <Route path="user-view" element={<UserView />} />
        <Route path="grading" element={<GradingListPage />} />
        <Route path="grading/:submissionId" element={<GradingDetailPage />} />
      </Route>

      {/* === GENERAL APP ROUTES: Dùng layout chung (AppLayout) === */}
      <Route path="/" element={<AppLayout />}>
        {/* Routes công khai, ai cũng xem được */}
        <Route index element={<HomePage />} />
        <Route path="ranking" element={<RankingPage />} />

        {/* Routes cần đăng nhập */}
        <Route path="contribute" element={<ProtectedRoute><ContributorDashboard /></ProtectedRoute>} />
        <Route path="tasks" element={<ProtectedRoute><TasksPage /></ProtectedRoute>} />
        <Route path="user/dashboard" element={<ProtectedRoute><UserDashboard /></ProtectedRoute>} />
        <Route path="change-password" element={<ProtectedRoute><ChangePassword /></ProtectedRoute>} />
        {/* <Route path="profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} /> */}
      </Route>

      {/* === SPECIAL LAYOUT ROUTES: Route không có layout chung === */}
      <Route path="quiz/:quizId" element={<div className="min-h-screen bg-gray-50 p-6"><QuizTakingPage /></div>} />

      {/* === FALLBACK ROUTE === */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

function App() {
  return (
    <QuizProvider>
      <ToastContainer position="bottom-right" autoClose={3000} hideProgressBar={false} newestOnTop closeOnClick rtl={false} pauseOnFocusLoss draggable pauseOnHover />
      <AppRoutes />
    </QuizProvider>
  );
}

export default App;