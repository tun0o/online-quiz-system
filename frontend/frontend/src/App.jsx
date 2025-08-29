import { Routes, Route, NavLink, Outlet, useLocation, Navigate, useNavigate } from "react-router-dom";
import { Home, Star, Shield, ClipboardList, User, Settings, UserPlus, LogIn, TrophyIcon, TargetIcon, ServerCrash } from "lucide-react";
import { QuizProvider } from "./contexts/QuizContext";
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import ContributorDashboard from "./components/contributor/ContributorDashboard";
import ProtectedRoute from "./components/auth/ProtectedRoute";
import useAuth from "./hooks/useAuth";
import AdminLayout from "./components/admin/AdminLayout";
import ModerationPanel from "./components/admin/ModerationPanel";
import AllSubmissionsTable from "./components/admin/AllSubmissionsTable";
import QuizSubmissionForm from "./components/contributor/QuizSubmissionForm";

/**
 * Layout chung cho toàn bộ ứng dụng, bao gồm Sidebar và khu vực nội dung chính.
 * <Outlet /> là một placeholder từ react-router-dom, nơi các component của route con sẽ được render.
 */
function AppLayout() {
  const location = useLocation();
  const { user } = useAuth();
  const showRightSidebar = !['/contribute'].includes(location.pathname);

  const baseMenu = [
    { icon: <Home size={20} />, label: "HỌC", path: "/" },
    { icon: <Star size={20} />, label: "ĐÓNG GÓP", path: "/contribute" },
    { icon: <Shield size={20} />, label: "BẢNG XẾP HẠNG", path: "/ranking" },
    { icon: <ClipboardList size={20} />, label: "NHIỆM VỤ", path: "/tasks" },
    { icon: <User size={20} />, label: "HỒ SƠ", path: "/profile" },
  ];

  // Menu cho người dùng thông thường. Admin sẽ truy cập trang của họ qua đường dẫn trực tiếp.
  const menu = baseMenu;
  const tasks = [
    { title: "Làm đúng 10 câu trong 1 đề", progress: 7, total: 10 },
    { title: "Học 10 phút", progress: 3, total: 10 },
    { title: "Hoàn thành 10 đề", progress: 2, total: 10 },
  ];

  const rankings = [
    { rank: 1, name: "Nguyễn Văn A", score: 128247, medal: "🥇" },
    { rank: 2, name: "Hehe", score: 982988, medal: "🥈" },
    { rank: 3, name: "Nguyễn Thị B", score: 2323, medal: "🥉" },
  ];

  return (
    <div className="min-h-screen bg-gray-50 text-gray-800 flex">
      {/* Sidebar */}
      <aside className="w-64 bg-white flex flex-col p-6 space-y-6 border-r border-gray-200 shadow-sm flex-shrink-0">
        <h1 className="text-2xl font-bold text-green-600">Practizz</h1>
        <nav className="flex flex-col gap-2">
          {menu.map((item) => (
            <NavLink
              key={item.label}
              to={item.path}
              end // `end` prop đảm bảo NavLink chỉ active khi path khớp chính xác
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
      </aside>

      {/* Main content area */}
      <div className="flex-1 flex">
        <main className={`flex-1 overflow-y-auto min-h-screen ${showRightSidebar ? 'p-6' : 'p-4'}`}>
          <Outlet /> {/* Nội dung của các trang sẽ được render ở đây */}
        </main>

        {/* Right sidebar (có thể được điều khiển hiển thị theo route) */}
        {showRightSidebar && (
          <aside className="w-80 bg-white border-l border-gray-200 shadow-sm min-h-screen flex-shrink-0">
            <div className="w-80 p-6 space-y-6">
              {/* Login/Register buttons */}
              <div className="flex gap-3">
                <button className="flex-1 flex justify-center items-center gap-2 px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition shadow-sm">
                  <UserPlus size={16} />
                  Đăng ký
                </button>
                <button className="flex-1 flex justify-center items-center gap-2 px-4 py-2 bg-white text-green-600 border border-green-600 rounded-lg hover:bg-green-50 transition shadow-sm">
                  <LogIn size={16} />
                  Đăng nhập
                </button>
              </div>

              {/* Ranking */}
              <div className="bg-gradient-to-br from-yellow-50 to-orange-50 rounded-xl p-5 border border-yellow-200">
                <div className="flex items-center gap-2 mb-4">
                  <TrophyIcon className="text-yellow-600" size={20} />
                  <h3 className="font-bold text-gray-800">Bảng xếp hạng</h3>
                </div>
                <div className="space-y-3">
                  {rankings.map((user) => (
                    <div key={user.rank} className="flex items-center justify-between p-3 bg-white rounded-lg shadow-sm border border-yellow-100">
                      <div className="flex items-center gap-3">
                        <span className="text-lg">{user.medal || `${user.rank}.`}</span>
                        <div>
                          <p className="font-medium text-gray-800">{user.name}</p>
                          <p className="text-sm text-gray-600">{user.score.toLocaleString()} điểm</p>
                        </div>
                      </div>
                      {user.rank <= 3 && (
                        <div className="w-2 h-8 bg-gradient-to-b from-yellow-400 to-yellow-600 rounded-full"></div>
                      )}
                    </div>
                  ))}
                </div>
              </div>

              {/* Nhiệm vụ */}
              <div className="bg-gradient-to-br from-green-50 to-emerald-50 rounded-xl p-5 border border-green-200">
                <div className="flex justify-between items-center mb-4">
                  <div className="flex items-center gap-2">
                    <TargetIcon className="text-green-600" size={20} />
                    <h3 className="font-bold text-gray-800">Nhiệm vụ hằng ngày</h3>
                  </div>
                  <a href="#" className="text-blue-600 text-sm font-medium hover:text-blue-700">XEM TẤT CẢ</a>
                </div>
                <div className="space-y-4">
                  {tasks.map((task, i) => (
                    <div key={i} className="p-3 bg-white rounded-lg shadow-sm border border-green-100">
                      <div className="flex justify-between items-center mb-2">
                        <p className="text-sm font-medium text-gray-800">{task.title}</p>
                        <span className="text-xs text-gray-500">{task.progress}/{task.total}</span>
                      </div>
                      <div className="w-full bg-gray-200 h-2 rounded-full overflow-hidden">
                        <div className="bg-gradient-to-r from-green-400 to-green-500 h-2 rounded-full transition-all duration-300" style={{ width: `${(task.progress / task.total) * 100}%` }} />
                      </div>
                      <p className="text-xs text-green-600 mt-1 font-medium">{Math.round((task.progress / task.total) * 100)}% hoàn thành</p>
                    </div>
                  ))}
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
  return (
    <>
      <div className="flex justify-between items-center mb-6">
        <div className="flex-1">
          <input
            type="text"
            placeholder="Tìm kiếm..."
            className="w-full p-3 rounded-lg bg-white border border-gray-200 text-gray-800 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent shadow-sm"
          />
        </div>
      </div>

      {/* Đề xuất */}
      <section className="mb-6">
        <h2 className="text-xl font-semibold mb-4 text-gray-800">Đề xuất</h2>
        <div className="flex gap-4 overflow-x-auto pb-4">
          {["Ôn luyện đạo hàm", "Ôn luyện điện tử", "Hóa hữu cơ"].map(
            (title, i) => (
              <div
                key={i}
                className="min-w-[220px] border border-gray-200 rounded-lg p-4 bg-white hover:shadow-md transition-shadow"
              >
                <h3 className="font-bold text-gray-800">{title}</h3>
                <p className="text-gray-600">Thời gian: 50p</p>
                <p className="text-gray-600">Số người tham gia: 273829</p>
                <p className="text-gray-600">Bình luận: 283</p>
                <p className="text-gray-600">50 câu hỏi</p>
              </div>
            )
          )}
        </div>
      </section>

      {/* Danh sách đề */}
      <section>
        <h2 className="text-xl font-semibold mb-4 text-gray-800">Danh sách đề</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {Array.from({ length: 12 }).map((_, i) => (
            <div
              key={i}
              className="border border-gray-200 rounded-lg p-4 bg-white hover:shadow-md transition-shadow cursor-pointer"
            >
              <h3 className="font-bold text-gray-800">Ôn luyện đạo hàm</h3>
              <p className="text-gray-600">Thời gian: 50p</p>
              <p className="text-gray-600">Số người tham gia: 273829</p>
              <p className="text-gray-600">Bình luận: 283</p>
              <p className="text-gray-600">50 câu hỏi</p>
            </div>
          ))}
        </div>
      </section>
    </>
  );
}

// Component cho trang Login (giả)
function LoginPage() {
  return (
    <div className="text-center p-10">
      <h1 className="text-2xl font-bold">Trang Đăng Nhập</h1>
      <p className="mt-4">Đây là nơi form đăng nhập sẽ xuất hiện.</p>
      <p>Hiện tại, bạn đang được giả lập là có vai trò `ADMIN` trong `useAuth.js`.</p>
      <p>Nếu bạn đổi vai trò thành `USER`, bạn sẽ bị chuyển hướng về đây khi cố truy cập `/admin`.</p>
    </div>
  );
}

// Component cho trang 404
function NotFoundPage() {
  return (
    <div className="text-center p-10 flex flex-col items-center">
      <ServerCrash size={64} className="text-red-500 mb-4" />
      <h1 className="text-4xl font-bold">404 - Không tìm thấy trang</h1>
      <p className="mt-4">Rất tiếc, trang bạn đang tìm kiếm không tồn tại.</p>
    </div>
  );
}


function AppRoutes() {
  const navigate = useNavigate();

  return (
    <Routes>
      {/* Các trang có layout chung (sidebar, etc.) */}
      <Route path="/" element={<AppLayout />}>
        <Route index element={<HomePage />} />
        <Route path="contribute" element={<ContributorDashboard />} />
        {/* Các route khác cho ranking, tasks, profile... */}
        <Route path="ranking" element={<div>Trang Bảng xếp hạng</div>} />
        <Route path="tasks" element={<div>Trang Nhiệm vụ</div>} />
        <Route path="profile" element={<div>Trang Hồ sơ</div>} />
      </Route>

      {/* Admin Routes - Hoàn toàn riêng biệt */}
      <Route path="/admin" element={<ProtectedRoute allowedRole="ADMIN"><AdminLayout /></ProtectedRoute>}>
        <Route index element={<Navigate to="moderation" replace />} />
        <Route path="moderation" element={<ModerationPanel />} />
        <Route path="management" element={<AllSubmissionsTable />} />
        <Route path="management/edit/:submissionId" element={<QuizSubmissionForm onSuccess={() => navigate('/admin/management')} />} />
      </Route>

      {/* Các trang không có layout chung */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

function App() {
  return (
    <QuizProvider>
      <ToastContainer autoClose={3000} hideProgressBar={false} />
      <AppRoutes />
    </QuizProvider>
  );
}

export default App;
