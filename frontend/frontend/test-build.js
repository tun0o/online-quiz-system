// Test script to verify build works correctly
import { build } from 'vite';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'path';

const config = defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(process.cwd(), './src'),
    },
  },
  build: {
    outDir: 'dist',
    rollupOptions: {
      output: {
        manualChunks: {
          'react-vendor': ['react', 'react-dom'],
          'router-vendor': ['react-router-dom'],
          'ui-vendor': ['lucide-react'],
          'toast-vendor': ['react-toastify'],
          'axios-vendor': ['axios'],
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
});

async function testBuild() {
  try {
    console.log('🔨 Testing build with code splitting...');
    await build(config);
    console.log('✅ Build successful! Code splitting should work correctly.');
  } catch (error) {
    console.error('❌ Build failed:', error.message);
    process.exit(1);
  }
}

testBuild();
