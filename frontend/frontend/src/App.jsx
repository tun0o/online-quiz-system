// App.jsx
import { Routes, Route, Navigate, useLocation, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
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
import { subjectDisplayMap } from "@/utils/displayMaps";
import Login from "@/components/auth/Login";
import Register from "@/components/auth/Register";
import ConfirmEmail from "@/components/auth/ConfirmEmail";
import Logout from "@/components/auth/Logout";
import NotFoundPage from "@/components/common/NotFoundPage";
import UserDashboard from "@/components/user/UserDashboard";
import AdminDashboard from "@/components/admin/AdminDashboard";

/**
 * Layout chung cho toàn bộ ứng dụng, bao gồm Sidebar và khu vực nội dung chính.
 */
function AppLayout() {
    const location = useLocation();
    const { user, logout, isAuthenticated } = useAuth();
    const navigate = useNavigate();
    const showRightSidebar = !['/contribute'].includes(location.pathname);

    const baseMenu = [
        { icon: <Home size={20} />, label: "HỌC", path: "/" },
        { icon: <Star size={20} />, label: "ĐÓNG GÓP", path: "/contribute" },
        { icon: <Shield size={20} />, label: "BẢNG XẾP HẠNG", path: "/ranking" },
        { icon: <ClipboardList size={20} />, label: "NHIỆM VỤ", path: "/tasks" },
        { icon: <User size={20} />, label: "HỒ SƠ", path: "/profile" },
    ];

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

    const handleLogout = async () => {
        try {
            await logout();
            navigate("/login");
            toast.success("Đã đăng xuất thành công");
        } catch {
            toast.error("Đăng xuất thất bại");
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 text-gray-800 flex">
            {/* Sidebar */}
            <aside className="w-64 bg-white flex flex-col justify-between p-6 border-r border-gray-200 shadow-sm flex-shrink-0">
                {/* Logo + Menu */}
                <div>
                    <h1 className="text-2xl font-bold text-green-600">Practizz</h1>
                    <nav className="flex flex-col gap-2 mt-6">
                        {menu.map((item) => (
                            <button
                                key={item.label}
                                onClick={() => navigate(item.path)}
                                className={`flex items-center gap-3 px-4 py-3 rounded-lg transition ${
                                    location.pathname === item.path
                                        ? "bg-green-50 text-green-600 border border-green-200 shadow-sm"
                                        : "text-gray-700 hover:bg-green-50 hover:text-green-600"
                                }`}
                            >
                                {item.icon}
                                {item.label}
                            </button>
                        ))}
                    </nav>
                </div>

                {/* Logout button (xuống dưới cùng) */}
                {isAuthenticated() && (
                    <button
                        onClick={handleLogout}
                        className="flex items-center gap-3 px-4 py-3 rounded-lg text-gray-700 hover:bg-red-50 hover:text-red-600 transition"
                    >
                        <LogOut size={20} />
                        Đăng xuất
                    </button>
                )}
            </aside>


            {/* Main content area */}
            <div className="flex-1 flex">
                <main className={`flex-1 overflow-y-auto min-h-screen ${showRightSidebar ? 'p-6' : 'p-4'}`}>
                    <Routes>
                        <Route index element={<HomePage />} />
                        <Route path="contribute" element={<ContributorDashboard />} />
                        <Route path="quiz/:quizId" element={<QuizTakingPage />} />
                        <Route path="ranking" element={<div>Trang Bảng xếp hạng</div>} />
                        <Route path="tasks" element={<div>Trang Nhiệm vụ</div>} />
                        <Route path="profile" element={
                            <ProtectedRoute>
                                <div>Trang Hồ sơ</div>
                            </ProtectedRoute>
                        } />
                    </Routes>
                </main>

                {/* Right sidebar (có thể được điều khiển hiển thị theo route) */}
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
                                            {user?.email?.charAt(0).toUpperCase()}
                                        </div>
                                        <div>
                                            <p className="font-medium text-gray-800">{user?.email}</p>
                                            <p className="text-sm text-gray-600">
                                                {user?.roles?.includes('ADMIN') ? 'Quản trị viên' : 'Người dùng'}
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            )}

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
                                    <button className="text-blue-600 text-sm font-medium hover:text-blue-700">XEM TẤT CẢ</button>
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

// Component cho trang làm bài quiz (placeholder)
function QuizTakingPage() {
    return (
        <div className="text-center p-10 bg-white rounded-lg shadow-sm">
            <h1 className="text-2xl font-bold">Trang Làm Bài Quiz</h1>
            <p className="mt-4">Bạn đang chuẩn bị làm đề thi</p>
            <p className="mt-2 text-gray-600">Giao diện làm bài chi tiết sẽ được phát triển ở đây.</p>
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
        setQuery(q => ({ ...q, subject: e.target.value, page: 0 }));
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
                <div className="w-full md:w-auto">
                    <select
                        name="subject"
                        value={query.subject}
                        onChange={handleFilterChange}
                        className="w-full md:w-48 p-3 border border-gray-200 rounded-lg bg-white focus:outline-none focus:ring-2 focus:ring-green-500 shadow-sm"
                    >
                        <option value="">Tất cả môn học</option>
                        {Object.entries(subjectDisplayMap).map(([value, label]) => (
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
                                <button
                                    key={quiz.id}
                                    onClick={() => navigate(`/quiz/${quiz.id}`)}
                                    className="border border-gray-200 rounded-lg p-4 bg-white hover:shadow-lg hover:-translate-y-1 transition-all duration-200 cursor-pointer h-full flex flex-col text-left"
                                >
                                    <h3 className="font-bold text-gray-800 mb-2">{quiz.title}</h3>
                                    <p className="text-gray-600 text-sm mb-3 flex-grow">{quiz.description}</p>
                                    <div className="text-sm text-gray-500 border-t border-gray-100 pt-2 mt-auto">
                                        <p>Môn: {subjectDisplayMap[quiz.subject] || quiz.subject}</p>
                                        <p>Thời gian: {quiz.durationMinutes} phút</p>
                                        <p>{quiz.questions?.length || 0} câu hỏi</p>
                                    </div>
                                </button>
                            ))}
                        </div>
                        {/* Pagination */}
                        {pagination.totalPages > 1 && (
                            <div className="flex justify-center items-center gap-2 mt-6">
                                {Array.from({ length: pagination.totalPages }, (_, i) => (
                                    <button
                                        key={i}
                                        onClick={() => handlePageChange(i)}
                                        className={`px-3 py-1 rounded ${
                                            i === query.page
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
    const { user, isAuthenticated } = useAuth();

    // Điều hướng mặc định theo role
    const getDefaultRedirect = () => {
        if (!isAuthenticated) return <HomePage />;

        // Kiểm tra role với cả hai định dạng
        const isAdmin = user?.roles?.some(role =>
            role.includes('ADMIN') || role.includes('ROLE_ADMIN')
        );

        return isAdmin
            ? <Navigate to="/admin/dashboard" replace />
            : <Navigate to="/user/dashboard" replace />;
    };

    return (
        <Routes>
            {/* Default route */}
            <Route path="/" element={getDefaultRedirect()} />

            {/* Public auth routes (không dùng layout chung) */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/confirm" element={<ConfirmEmail />} />
            <Route path="/logout" element={<Logout />} />

            {/* App routes (dùng layout chung: sidebar, header, etc.) */}
            <Route path="/*" element={<AppLayout />} />

            {/* Admin routes */}
            <Route
                path="/admin/*"
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
                <Route path="management/edit/:submissionId" element={
                    <QuizSubmissionForm onSuccess={() => navigate("/admin/management")} />
                } />
            </Route>

            {/* User dashboard route */}
            <Route
                path="/user/dashboard"
                element={
                    <ProtectedRoute>
                        <AppLayout>
                            <UserDashboard />
                        </AppLayout>
                    </ProtectedRoute>
                }
            />

            {/* 404 fallback */}
            <Route path="*" element={<NotFoundPage />} />
        </Routes>
    );
}

function App() {
    return (
        <QuizProvider>
            <ToastContainer position="bottom-right" autoClose={3000} hideProgressBar={false} newestOnTop closeOnClick rtl={false} pauseOnFocusLoss draggable pauseOnHover/>
            <AppRoutes />
        </QuizProvider>
    );
}

export default App;