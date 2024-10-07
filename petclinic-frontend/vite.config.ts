import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import federation from '@originjs/vite-plugin-federation';
import * as path from "path";

export default defineConfig({
  esbuild: {
    loader: 'tsx',  // Add this line to force esbuild to handle `.tsx` correctly
    // You can also disable minification in development mode
  },
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
    assetsDir: 'src/assets',
  },
});