
import { defineConfig } from 'vitest/config';
import { fileURLToPath, URL } from 'node:url';

const fromRoot = (path: string) =>
  fileURLToPath(new URL(path, import.meta.url));

export default defineConfig({
  resolve: {
    alias: {
      '@shared': fromRoot('./src/app/shared'),
      '@core': fromRoot('./src/app/core'),
      '@features': fromRoot('./src/app/features'),
      '@app': fromRoot('./src/app'),
      '@environments': fromRoot('./src/environments'),
      '@layout': fromRoot('./src/app/layout'),
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./test/setup.ts'],
  },
});
