import { createContext, useContext, useState, useMemo, useCallback, useEffect } from 'react';

const AdminViewContext = createContext();

export const useAdminView = () => {
  const context = useContext(AdminViewContext);
  if (!context) {
    throw new Error('useAdminView must be used within an AdminViewProvider');
  }
  return context;
};

export const AdminViewProvider = ({ children }) => {
  // Khởi tạo state từ localStorage, mặc định là false nếu không có
  const [isViewingAsUser, setIsViewingAsUser] = useState(() => {
    const savedState = localStorage.getItem('adminIsViewingAsUser');
    return savedState === 'true'; // Chuyển đổi chuỗi 'true'/'false' thành boolean
  });

  // Các hàm này chỉ thay đổi state, không navigate
  const switchToUserView = useCallback(() => {
    setIsViewingAsUser(true);
    localStorage.setItem('adminIsViewingAsUser', 'true');
  }, []);
  
  const switchToAdminView = useCallback(() => {
    setIsViewingAsUser(false);
    localStorage.setItem('adminIsViewingAsUser', 'false');
  }, []);

  const value = useMemo(() => ({ isViewingAsUser, switchToUserView, switchToAdminView }), [isViewingAsUser, switchToUserView, switchToAdminView]);

  return <AdminViewContext.Provider value={value}>{children}</AdminViewContext.Provider>;
};
