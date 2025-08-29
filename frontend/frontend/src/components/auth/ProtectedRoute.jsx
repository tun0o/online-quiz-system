import { Navigate, useLocation } from 'react-router-dom';
import useAuth from '../../hooks/useAuth'; // Hook giả lập để lấy thông tin user

/**
 * Component để bảo vệ một route, chỉ cho phép truy cập nếu người dùng có vai trò phù hợp.
 * @param {object} props
 * @param {React.ReactNode} props.children - Component con sẽ được render nếu được phép.
 * @param {string} props.allowedRole - Vai trò được phép truy cập (e.g., 'ADMIN').
 */
export default function ProtectedRoute({ children, allowedRole }) {
  const { user } = useAuth();
  const location = useLocation();

  if (!user || user.role !== allowedRole) {
    // Nếu user không tồn tại hoặc không có quyền, chuyển hướng đến trang đăng nhập
    // hoặc trang "không có quyền truy cập", lưu lại vị trí hiện tại để có thể quay lại sau khi đăng nhập.
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
}