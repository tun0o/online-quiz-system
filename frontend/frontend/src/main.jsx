// index.jsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import './index.css';
import App from './App.jsx';
import { AuthProvider } from '@/contexts/AuthContext';
import { AdminViewProvider } from '@/contexts/AdminViewContext';

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <AuthProvider>
            <AdminViewProvider>
                <BrowserRouter>
                    <App />
                </BrowserRouter>
            </AdminViewProvider>
        </AuthProvider>
    </StrictMode>
);
