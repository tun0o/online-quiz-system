export const decodeToken = (token) => {
    try {
        const payload = token.split('.')[1];
        const decodedPayload = atob(payload);
        return JSON.parse(decodedPayload);
    } catch (e) {
        console.error('Error decoding token:', e);
        return null;
    }
};

export const getRolesFromToken = (token) => {
    const decoded = decodeToken(token);
    return decoded?.roles || [];
};

export const getUserFromToken = (token) => {
    const decoded = decodeToken(token);
    if (!decoded) return null;

    return {
        id: decoded.userId || decoded.sub,
        email: decoded.sub,
        roles: decoded.roles || [],
        isVerified: decoded.verified || false
    };
};

export const isTokenExpired = (token) => {
    const decoded = decodeToken(token);
    if (!decoded || !decoded.exp) return true;

    return decoded.exp * 1000 < Date.now();
};