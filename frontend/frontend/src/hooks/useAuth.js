// Hook giả lập để lấy thông tin người dùng.
// Trong thực tế, bạn sẽ thay thế nó bằng logic gọi API hoặc lấy từ context/redux.

const useAuth = () => {
  // Giả sử người dùng đã đăng nhập với vai trò ADMIN.
  // Bạn có thể thay đổi 'ADMIN' thành 'USER' để kiểm tra.
  const user = {
    name: 'Current Admin',
    role: 'ADMIN', // 'ADMIN' or 'USER'
  };

  return { user };
};

export default useAuth;