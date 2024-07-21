/// <reference types="vite/client" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import federation from '@originjs/vite-plugin-federation';

export default defineConfig(() => {
  return {
  plugins: [
    react(),
    federation({
      name: 'petclinic-frontend',
      shared: ['react', 'react-dom'],
    }),
  ],
  resolve:{
    alias: {
      '@/': new URL('./src/', import.meta.url).pathname,
    }
  },
  envDir: 'src/environments',
  builds:{
    modulePReload: false,
    target: 'esnext',
    assestsDir: 'src/assets'
  }
  };
});