/// <reference types="vite/client" />
import { defineConfig } from 'vite';
import angular from '@analogjs/vite-plugin-angular';

export default defineConfig(() => {
  return {
    plugins: [
      // angular({
      //   tsconfig: './tsconfig.json',
      //   jit: true,
      //   experimental: {
      //     supportNonStandardFileExtensions: true
      //   }
      // })
    ],
    resolve: {
      alias: {
        '@/': new URL('./src/', import.meta.url).pathname,
      },
    },
    envDir: 'src/environments/',
    server: {
      port: 4200,
      host: true,
      fs: {
        allow: ['..']
      },
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
        }
      }
    },
    build: {
      target: 'esnext',
      outDir: 'dist',
      sourcemap: true,
      assetsDir: 'assets',
      rollupOptions: {
        output: {
          entryFileNames: 'assets/[name]-[hash].js',
          chunkFileNames: 'assets/[name]-[hash].js',
          assetFileNames: 'assets/[name]-[hash].[ext]'
        }
      }
    },
    optimizeDeps: {
      include: ['@angular/core', '@angular/common', '@angular/forms', '@angular/router'],
      force: true
    },
  };
});
