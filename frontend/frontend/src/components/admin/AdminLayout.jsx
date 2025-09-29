<<<<<<< HEAD
import { NavLink, Outlet } from 'react-router-dom';
import { ShieldCheck, ListChecks, LogOut, Edit3 } from 'lucide-react';

const adminMenu = [
  { to: '/admin/moderation', label: 'Kiểm duyệt', icon: <ShieldCheck size={20} /> },
  { to: '/admin/management', label: 'Quản lý Đề thi', icon: <ListChecks size={20} /> },
  { to: '/admin/grading', label: 'Chấm bài', icon: <Edit3 size={20} /> },
=======
import { NavLink, Outlet, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ShieldCheck, ListChecks, LogOut, BarChart3, Home, User, Eye } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import { useAdminView } from '@/contexts/AdminViewContext';

const adminMenu = [
    { to: '/admin/dashboard', label: 'Tổng quan', icon: <BarChart3 size={20} /> },
    { to: '/admin/moderation', label: 'Kiểm duyệt', icon: <ShieldCheck size={20} /> },
    { to: '/admin/management', label: 'Quản lý Đề thi', icon: <ListChecks size={20} /> },
>>>>>>> feature-quiz
];

const navLinkClasses = ({ isActive }) =>
    `flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
        isActive ? 'bg-gray-700 text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white'
    }`;

const DefaultLoader = () => (
    <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600" />
    </div>
);

export default function AdminLayout() {
    const { user, loading, isAuthenticated, logout } = useAuth();
    const { isAdminView, switchToUserView, canToggle } = useAdminView();
    const location = useLocation();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    if (loading) return <DefaultLoader />;

    if (!isAuthenticated) return <Navigate to="/login" state={{ from: location }} replace />;

    const isAdmin = user?.roles?.some(role =>
        role === 'ADMIN' || role === 'ROLE_ADMIN'
    );

    if (!isAdmin) return <Navigate to="/unauthorized" replace />;

    return (
        <div className="flex min-h-screen bg-gray-100 font-sans">
            <aside className="w-64 bg-gray-800 text-white flex flex-col flex-shrink-0">
                <div className="h-16 flex items-center justify-center text-2xl font-bold border-b border-gray-700">
                    <a href="/admin">Practizz Admin Panel</a>
                </div>
                <nav className="flex-1 p-4 space-y-2">
                    {adminMenu.map(item => (
                        <NavLink key={item.to} to={item.to} className={navLinkClasses} end>
                            {item.icon}
                            <span>{item.label}</span>
                        </NavLink>
                    ))}
                </nav>
                <div className="p-4 border-t border-gray-700 space-y-2">
                    {canToggle && (
                        <button
                            onClick={() => navigate('/admin/user-view')}
                            className="flex items-center gap-3 w-full px-4 py-3 rounded-lg text-gray-300 hover:bg-gray-700 hover:text-white transition-colors"
                        >
                            <User size={20} />
                            <span>Xem giao diện User</span>
                        </button>
                    )}
                    <a
                        href="/"
                        className="flex items-center gap-3 px-4 py-3 rounded-lg text-gray-300 hover:bg-gray-700 hover:text-white transition-colors"
                    >
                        <Home size={20} />
                        <span>Về trang chủ</span>
                    </a>
                    <button
                        onClick={handleLogout}
                        type="button"
                        className="group flex items-center gap-3 w-full px-4 py-3 rounded-lg
               text-gray-300 hover:bg-gray-700 transition-colors
               focus:outline-none focus:ring-0 focus:border-0 border-0"
                    >
                        <LogOut size={20} className="text-gray-300 group-hover:text-red-600 transition-colors" />
                        <span className="text-gray-300 group-hover:text-red-600 transition-colors">Đăng xuất</span>
                    </button>
                </div>
            </aside>
            <main className="flex-1 p-6 overflow-y-auto">
                <Outlet />
            </main>
        </div>
    );
}