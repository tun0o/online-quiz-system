import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const HomePage = () => {
    const { isAuthenticated, hasRole } = useAuth();

    return (
        <div className="home-page">
            <div className="hero-section">
                <h1>Chào mừng đến với Hệ thống Quiz Online</h1>
                <p>Nền tảng học tập và đánh giá kiến thức hàng đầu</p>

                {!isAuthenticated ? (
                    <div className="auth-buttons">
                        <Link to="/login" className="btn btn-primary">Đăng nhập</Link>
                        <Link to="/register" className="btn btn-secondary">Đăng ký</Link>
                    </div>
                ) : (
                    <div className="dashboard-links">
                        {hasRole('ROLE_ADMIN') && (
                            <Link to="/admin/dashboard" className="btn btn-primary">Dashboard Quản trị</Link>
                        )}
                        {hasRole('ROLE_USER') && (
                            <Link to="/user/dashboard" className="btn btn-primary">Dashboard Người dùng</Link>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default HomePage;