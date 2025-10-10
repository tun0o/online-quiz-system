// index.jsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import './index.css';
import App from './App.jsx';
import { AdminViewProvider } from '@/contexts/AdminViewContext';
import { useAuthStore } from './hooks/useAuth';
import api from './services/api';

// Khởi tạo header Authorization khi ứng dụng tải lần đầu
// Đoạn code này được đặt ở đây để phá vỡ vòng lặp phụ thuộc giữa useAuth.js và api.js
const initialAccessToken = useAuthStore.getState().accessToken;
if (initialAccessToken) {
    api.defaults.headers.common['Authorization'] = `Bearer ${initialAccessToken}`;
}

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <AdminViewProvider>
            <BrowserRouter>
                <App />
            </BrowserRouter>
        </AdminViewProvider>
    </StrictMode>
);
