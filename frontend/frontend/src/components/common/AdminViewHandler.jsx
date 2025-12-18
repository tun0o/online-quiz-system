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
    const hasNavigatedRef = useRef(false);

    useEffect(() => {
        // Chỉ thực hiện logic nếu người dùng là Admin
        if (!isAdmin) {
            hasNavigatedRef.current = false; // Reset khi không còn là admin
            return;
        }

        if (isViewingAsUser) {
            // Nếu đang ở chế độ xem user nhưng vẫn ở trang admin
            if (location.pathname.startsWith('/admin')) {
                navigate('/');
            }
        } else {
            // Nếu đang ở chế độ admin nhưng không ở trang admin (và không phải trang chủ)
            if (!location.pathname.startsWith('/admin')) {
                navigate('/admin');
            }
        }
    }, [isViewingAsUser, isAdmin, navigate, location.pathname]);

    return null; // Component này không render gì cả
}