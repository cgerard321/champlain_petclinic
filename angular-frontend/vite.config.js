import { defineConfig } from 'vite';
import legacy from '@vitejs/plugin-legacy';

export default defineConfig(() => {
  return {
    plugins: [
      legacy({
        targets: ['defaults', 'not IE 11']
      })
    ],
    resolve: {
      alias: {
        '@': new URL('./src/', import.meta.url).pathname,
      },
    },
    build: {
      modulePreload: false,
      target: 'esnext',
      assetsDir: 'src/assets',
      rollupOptions: {
        input: {
          main: './index.html'
        }
      }
    },
    publicDir: 'public',
    server: {
      port: 8082,
      host: true,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false
        }
      }
    }
  };
});
