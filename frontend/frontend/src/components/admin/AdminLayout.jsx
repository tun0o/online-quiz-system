
import { NavLink, Outlet, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ShieldCheck, ListChecks, LogOut, BarChart3, Home, User, Inbox, Users } from 'lucide-react';
import { useAuth, useAuthStore } from '@/hooks/useAuth';
import { useAdminView } from '@/contexts/AdminViewContext';

// Menu đầy đủ cho Admin Panel
const fullAdminMenu = [
    // Admin-only
    { to: '/admin/dashboard', label: 'Tổng quan', icon: <BarChart3 size={20} />, roles: ['ADMIN'] },
    { to: '/admin/users', label: 'Người dùng', icon: <Users size={20} />, roles: ['ADMIN'] },
    // Moderator & Admin
    { to: '/admin/mod-dashboard', label: 'Tổng quan', icon: <BarChart3 size={20} />, roles: ['MODERATOR'] },
    { to: '/admin/moderation', label: 'Kiểm duyệt', icon: <ShieldCheck size={20} />, roles: ['ADMIN', 'MODERATOR'] },
    { to: '/admin/management', label: 'Quản lý Đề thi', icon: <ListChecks size={20} />, roles: ['ADMIN', 'MODERATOR'] },
    { to: '/admin/grading', label: 'Chấm bài', icon: <Inbox size={20} />, roles: ['ADMIN', 'MODERATOR'] },
];

const navLinkClasses = ({ isActive }) =>
    `flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${isActive ? 'bg-gray-700 text-white' : 'text-gray-300 hover:bg-gray-700 hover:text-white'
    }`;

const DefaultLoader = () => (
    <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-green-600" />
    </div>
);

export default function AdminLayout() {
    const { user, isAuthenticated, logout, hasRole } = useAuthStore();
    const { switchToUserView } = useAdminView();
    const location = useLocation();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    if (!isAuthenticated()) return <Navigate to="/login" state={{ from: location }} replace />;

    const isAdmin = hasRole('ADMIN');
    const isModerator = hasRole('MODERATOR');

    // Nếu không phải Admin hoặc Moderator, không cho phép truy cập
    if (!isAdmin && !isModerator) return <Navigate to="/unauthorized" replace />;

    // Xây dựng menu dựa trên vai trò
    const menuItems = fullAdminMenu.filter(item => {
        if (isAdmin) {
            // Admin thấy tất cả các mục, trừ dashboard của Mod
            return item.roles.includes('ADMIN');
        }
        if (isModerator) {
            return item.roles.includes('MODERATOR');
        }
    });

    return (
        <div className="flex min-h-screen bg-gray-100 font-sans">
            <aside className="w-64 bg-gray-800 text-white flex flex-col flex-shrink-0">
                <div className="h-16 flex items-center justify-center text-2xl font-bold border-b border-gray-700">
                    <a href="/admin">Practizz Panel</a>
                </div>
                <nav className="flex-1 p-4 space-y-2">
                    {menuItems.map(item => (
                        <NavLink key={item.to} to={item.to} className={navLinkClasses} end>
                            {item.icon}
                            <span>{item.label}</span>
                        </NavLink>
                    ))}
                </nav>
                <div className="p-4 border-t border-gray-700 space-y-2">
                    {/* Chỉ Admin mới có nút chuyển đổi giao diện */}
                    {isAdmin && (
                        <button
                            onClick={switchToUserView}
                            className="flex items-center gap-3 w-full px-4 py-3 rounded-lg bg-transparent text-gray-300 hover:bg-gray-700 hover:text-white transition-colors border-0"
                        >
                            <User size={20} />
                            <span>Xem giao diện User</span>
                        </button>
                    )}
                    <button
                        onClick={handleLogout}
                        type="button"
                        className="group flex items-center gap-3 w-full px-4 py-3 rounded-lg bg-transparent
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