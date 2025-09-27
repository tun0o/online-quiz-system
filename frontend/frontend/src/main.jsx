// index.jsx
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import './index.css';
import App from './App.jsx';
import { AuthProvider } from '@/contexts/AuthContext'; // thêm import
import { ensureDeviceIdentity } from './utils/device.js';

ensureDeviceIdentity().finally(() => {
    createRoot(document.getElementById('root')).render(
        <StrictMode>
            <AuthProvider>
                <BrowserRouter>
                    <App />
                </BrowserRouter>
            </AuthProvider>
        </StrictMode>
    );
});
