import { getRolesFromToken } from './jwtUtils';

export const hasRole = (requiredRole) => {
    const token = localStorage.getItem('token');
    if (!token) return false;

    const roles = getRolesFromToken(token);
    return roles.includes(requiredRole);
};

export const hasAnyRole = (requiredRoles) => {
    const token = localStorage.getItem('token');
    if (!token) return false;

    const roles = getRolesFromToken(token);
    return requiredRoles.some(role => roles.includes(role));
};

export const getCurrentUserRoles = () => {
    const token = localStorage.getItem('token');
    if (!token) return [];

    return getRolesFromToken(token);
};