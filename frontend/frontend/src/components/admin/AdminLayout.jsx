import { NavLink, Outlet, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { ShieldCheck, ListChecks, BarChart3, Home } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';

const adminMenu = [
    { to: '/admin/dashboard', label: 'Tổng quan', icon: <BarChart3 size={20} /> },
    { to: '/admin/moderation', label: 'Kiểm duyệt', icon: <ShieldCheck size={20} /> },
    { to: '/admin/management', label: 'Quản lý Đề thi', icon: <ListChecks size={20} /> },
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
    const { user, loading, isAuthenticated } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();

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
                    <button
                        onClick={() => navigate('/user/dashboard')}
                        className="flex items-center gap-3 w-full px-4 py-3 rounded-lg transition-colors text-gray-300 hover:bg-gray-700 hover:text-white"
                    >
                        <Home size={20} />
                        <span>Về trang chủ</span>
                    </button>
                </div>
            </aside>
            <main className="flex-1 p-6 overflow-y-auto">
                <Outlet />
            </main>
        </div>
    );
}