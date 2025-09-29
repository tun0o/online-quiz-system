import React, { createContext, useContext, useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';

const AdminViewContext = createContext();

export const useAdminView = () => {
    const context = useContext(AdminViewContext);
    if (!context) {
        throw new Error('useAdminView must be used within an AdminViewProvider');
    }
    return context;
};

export const AdminViewProvider = ({ children }) => {
    const { user, hasRole } = useAuth();
    const [isAdminView, setIsAdminView] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        // Chỉ cho phép ADMIN chuyển đổi view
        if (user && hasRole('ROLE_ADMIN')) {
            // Kiểm tra localStorage để lưu trạng thái view
            const savedView = localStorage.getItem('adminViewMode');
            setIsAdminView(savedView === 'true');
        } else {
            setIsAdminView(false);
        }
        setIsLoading(false);
    }, [user, hasRole]);

    const toggleView = () => {
        if (user && hasRole('ROLE_ADMIN')) {
            const newView = !isAdminView;
            setIsAdminView(newView);
            localStorage.setItem('adminViewMode', newView.toString());
        }
    };

    const switchToAdminView = () => {
        if (user && hasRole('ROLE_ADMIN')) {
            setIsAdminView(true);
            localStorage.setItem('adminViewMode', 'true');
        }
    };

    const switchToUserView = () => {
        if (user && hasRole('ROLE_ADMIN')) {
            setIsAdminView(false);
            localStorage.setItem('adminViewMode', 'false');
        }
    };

    const value = {
        isAdminView,
        toggleView,
        switchToAdminView,
        switchToUserView,
        isLoading,
        canToggle: user && hasRole('ROLE_ADMIN')
    };

    return (
        <AdminViewContext.Provider value={value}>
            {children}
        </AdminViewContext.Provider>
    );
};

