import { useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAdminView } from '@/contexts/AdminViewContext';
import { useIsAdmin } from '@/hooks/useIsAdmin';

/**
 * Component này xử lý logic điều hướng khi Admin chuyển đổi giữa các chế độ xem.
 * Nó phải được đặt bên trong Router.
 */
export default function AdminViewHandler() {
    const { isViewingAsUser } = useAdminView();
    const isAdmin = useIsAdmin();
    const navigate = useNavigate();
    const location = useLocation();
    const previousIsViewingAsUser = useRef(isViewingAsUser);

    useEffect(() => {
        // Chỉ thực hiện logic nếu người dùng là Admin
        if (!isAdmin) {
            return;
        }

        // Kiểm tra xem state có thay đổi không
        if (previousIsViewingAsUser.current !== isViewingAsUser) {
            if (isViewingAsUser && !location.pathname.startsWith('/admin')) {
                // Đang ở chế độ xem user, và đã ở trang user -> không làm gì
            } else if (isViewingAsUser) {
                navigate('/');
            } else if (!isViewingAsUser && location.pathname.startsWith('/admin')) {
                // Đang ở chế độ admin, và đã ở trang admin -> không làm gì
            } else {
                navigate('/admin');
            }
        }

        // Cập nhật giá trị trước đó cho lần render tiếp theo
        previousIsViewingAsUser.current = isViewingAsUser;
    }, [isViewingAsUser, isAdmin, navigate, location.pathname]);

    return null; // Component này không render gì cả
}