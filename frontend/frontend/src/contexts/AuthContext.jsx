import React, { createContext, useContext, useState, useEffect } from 'react';
import { getRolesFromToken, getUserFromToken } from '../utils/jwtUtils';

const AuthContext = createContext();

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
};

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const token = localStorage.getItem('token');
        if (token) {
            const userData = getUserFromToken(token);
            setUser(userData);
        }
        setIsLoading(false);
    }, []);

    const login = (token) => {
        localStorage.setItem('token', token);
        const userData = getUserFromToken(token);
        setUser(userData);
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setUser(null);
        delete api.defaults.headers.common['Authorization'];
    };

    const hasRole = (role) => {
        if (!user) return false;
        return user.roles.includes(role);
    };

    const value = {
        user,
        isLoading,
        login,
        logout,
        isAuthenticated: !!user,
        hasRole
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};