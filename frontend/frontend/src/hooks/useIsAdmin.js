import { useAuth } from './useAuth';

/**
 * Hook riêng để kiểm tra vai trò Admin thực sự của người dùng,
 * không bị ảnh hưởng bởi chế độ xem (isViewingAsUser).
 * Dùng cho các logic nền như hiển thị banner, xử lý điều hướng...
 */
export const useIsAdmin = () => {
    const { user } = useAuth();
    // Kiểm tra trực tiếp từ đối tượng user gốc
    return user?.roles?.some(role => role === 'ADMIN' || role === 'ROLE_ADMIN') || false;
};