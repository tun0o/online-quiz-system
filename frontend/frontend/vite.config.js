import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'), // dùng @ thay cho ./src
    },
  },
  server: {
    port: 3000,          // đổi port dev
    open: true,          // tự mở trình duyệt khi chạy
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // BE Spring Boot chẳng hạn
        changeOrigin: true,
        secure: false,
      },
    },
  },
  build: {
    outDir: 'dist', // thư mục build
    rollupOptions: {
      output: {
        manualChunks: {
          // Vendor chunks
          'react-vendor': ['react', 'react-dom'],
          'router-vendor': ['react-router-dom'],
          'ui-vendor': ['lucide-react'],
          'toast-vendor': ['react-toastify'],
          'axios-vendor': ['axios'],
          // App chunks
          'auth': [
            './src/components/auth/Login.jsx',
            './src/components/auth/Register.jsx',
            './src/components/auth/OAuth2Success.jsx',
            './src/components/auth/OAuth2Error.jsx'
          ],
          'admin': [
            './src/components/admin/AdminDashboard.jsx',
            './src/components/admin/ModerationPanel.jsx',
            './src/components/admin/AllSubmissionsTable.jsx'
          ],
          'services': [
            './src/services/api.js',
            './src/services/authService.js',
            './src/services/quizService.js'
          ]
        }
      }
    }
  },
})
