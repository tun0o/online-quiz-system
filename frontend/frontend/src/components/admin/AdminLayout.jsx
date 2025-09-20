import { NavLink, Outlet } from 'react-router-dom';
import { ShieldCheck, ListChecks, LogOut, Edit3 } from 'lucide-react';

const adminMenu = [
  { to: '/admin/moderation', label: 'Kiểm duyệt', icon: <ShieldCheck size={20} /> },
  { to: '/admin/management', label: 'Quản lý Đề thi', icon: <ListChecks size={20} /> },
  { to: '/admin/grading', label: 'Chấm bài', icon: <Edit3 size={20} /> },
];

const navLinkClasses = ({ isActive }) =>
  `flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
    isActive
      ? 'bg-gray-700 text-white'
      : 'text-gray-300 hover:bg-gray-700 hover:text-white'
  }`;

export default function AdminLayout() {
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
        <div className="p-4 border-t border-gray-700">
          <a href="/" className="flex items-center gap-3 px-4 py-3 rounded-lg text-gray-300 hover:bg-gray-700 hover:text-white">
            <LogOut size={20} className="transform rotate-180" />
            <span>Về trang chính</span>
          </a>
        </div>
      </aside>
      <main className="flex-1 p-6 overflow-y-auto">
        <Outlet />
      </main>
    </div>
  );
}