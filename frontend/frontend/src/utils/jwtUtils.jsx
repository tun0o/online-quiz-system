export const decodeToken = (token) => {
    try {
        const payload = token.split('.')[1];
        // Chuẩn hóa chuỗi Base64URL về Base64 chuẩn
        let base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
        // Thêm padding nếu cần thiết
        const padding = base64.length % 4;
        if (padding) {
            base64 += '='.repeat(4 - padding);
        }
        // Giải mã chuỗi đã được chuẩn hóa
        const decodedPayload = atob(base64);
        return JSON.parse(decodeURIComponent(escape(decodedPayload)));
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