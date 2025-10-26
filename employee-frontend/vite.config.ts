/// <reference types="vite/client" />
import { defineConfig } from 'vite';
import angular from '@analogjs/vite-plugin-angular';

export default defineConfig(() => {
  return {
    plugins: [angular()],
    resolve: {
      alias: {
        '@/': new URL('./src/', import.meta.url).pathname,
      },
    },
    envDir: 'src/environments/',
    server: {
      port: 4200,
      host: true,
    },
    build: {
      target: 'esnext',
      outDir: 'dist',
    },
  };
});
