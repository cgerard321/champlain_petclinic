import { defineConfig, loadEnv } from 'vite';
import legacy from '@vitejs/plugin-legacy';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  return {
    plugins: [
      legacy({
        targets: ['defaults', 'not IE 11']
      })
    ],
    resolve: {
      alias: {
        '@': new URL('./public/', import.meta.url).pathname,
      },
    },
    build: {
      modulePreload: false,
      target: 'esnext',
      assetsDir: 'public/assets',
      rollupOptions: {
        input: {
          main: './index.html'
        }
      }
    },
    publicDir: 'public',
    server: {
      port: 4200,
      host: true,
      proxy: {
        '/api': {
          target: env.VITE_API_TARGET || 'http://localhost:8080',
          changeOrigin: true,
          secure: false
        }
      }
    }
  };
});
