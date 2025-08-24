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
  },
})
