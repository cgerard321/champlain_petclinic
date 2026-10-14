import { defineConfig, devices } from '@playwright/test';
export default defineConfig({
  testDir: 'C:/Users/ionas/champlain_petclinic/employee-frontend/tests',
  fullyParallel: false,
  reporter: 'line',
  use: { baseURL: 'http://localhost:4200' },
  projects: [{ name: 'system-chrome', use: { ...devices['Desktop Chrome'], launchOptions: { executablePath: 'C:/Program Files/Google/Chrome/Application/chrome.exe' } } }],
});
