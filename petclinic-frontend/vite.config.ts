/// <reference types="vite/client" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import federation from '@originjs/vite-plugin-federation';
import path from 'path';

export default defineConfig(() => {
  return {
    plugins: [
      react(),
      federation({
        name: 'petclinic-frontend',
        shared: ['react', 'react-dom'],
      }),
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    envDir: 'src/environments/',
    build: {
      modulePreload: false,
      target: 'esnext',
      assestsDir: 'src/assets',
    },
  };
});
