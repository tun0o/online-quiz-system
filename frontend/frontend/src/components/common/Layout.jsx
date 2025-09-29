import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { hasRole } from '../../utils/roleUtils';

const Layout = ({ children }) => {
    const { user, isAuthenticated, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <div className="app-layout">
            <header className="app-header">
                <nav className="main-nav">
                    <Link to="/" className="nav-brand">Online Quiz System</Link>

                    <div className="nav-links">
                        {isAuthenticated ? (
                            <>
                                {hasRole('ROLE_ADMIN') && (
                                    <Link to="/admin/dashboard" className="nav-link">Quản trị</Link>
                                )}
                                {hasRole('ROLE_USER') && (
                                    <Link to="/user/dashboard" className="nav-link">Người dùng</Link>
                                )}
                                <span className="user-greeting">Xin chào, {user?.email}</span>
                                <button onClick={handleLogout} className="logout-btn">Đăng xuất</button>
                            </>
                        ) : (
                            <>
                                <Link to="/login" className="nav-link">Đăng nhập</Link>
                                <Link to="/register" className="nav-link">Đăng ký</Link>
                            </>
                        )}
                    </div>
                </nav>
            </header>

            <main className="app-main">
                {children}
            </main>

            <footer className="app-footer">
                <p>&copy; 2023 Online Quiz System. All rights reserved.</p>
            </footer>
        </div>
    );
};

export default Layout;