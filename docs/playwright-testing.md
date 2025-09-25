# Frontend Testing with Playwright

Back to [Main page](../README.md)

## Overview

This document outlines the testing standards and best practices for end-to-end testing using Playwright in the Champlain Pet Clinic frontend application. The project uses ESLint with the Playwright plugin to enforce testing best practices and maintain code quality.

## ESLint Configuration for Tests

The project implements strict ESLint rules specifically for Playwright tests to ensure maintainability and reliability. The linter applies the `eslint-plugin-playwright` which suggests Playwright best practices.

### Key ESLint Rules Applied

The following Playwright-specific ESLint rules are enforced in test files:

- **`playwright/expect-expect`**: Ensures tests contain at least one expect statement
- **`playwright/max-nested-describe`**: Limits nesting of describe blocks to improve readability
- **`playwright/missing-playwright-await`**: Requires await for async Playwright operations
- **`playwright/no-conditional-in-test`**: Prevents conditional logic in tests
- **`playwright/no-force-option`**: Discourages use of force options
- **`playwright/prefer-web-first-assertions`**: Enforces web-first assertions for better reliability
- **`playwright/require-top-level-describe`**: Requires top-level describe blocks for organization

### Reviewing ESLint Recommendations

Before writing or modifying tests, review the ESLint recommendations:

```bash
# Run linting on test files
npm run lint

# Automatically fix ESLint issues where possible
npm run lint:fix
```

Always address ESLint warnings and errors in test files. The linter helps identify:

- Missing await statements
- Inefficient selectors
- Poor test structure
- Non-web-first assertions

## Test Structure Standards

### File Organization

Tests are organized in the `tests/` directory with feature-based folders:

```text
tests/
├── homepage/
├── customerspage/
├── inventoriespage/
├── productspage/
├── vetpage/
└── visitstestpage/
```

### Naming Conventions

- Test files must end with `.spec.ts`
- Use descriptive names that clearly indicate the feature being tested
- Follow camelCase for function and variable names

### Basic Test Structure

```typescript
import { test, expect } from '@playwright/test';

describe('Feature Name', () => {
  test('descriptive test name', async ({ page }) => {
    // Arrange - Use baseURL from config or environment variable
    await page.goto('/target-page'); // Relative URL if baseURL is set in playwright config
    // OR
    // await page.goto(process.env.BASE_URL + '/target-page'); # To do this the environment variables need to be set in the playwright config
    
    // Act
    await page.getByRole('button', { name: 'Action' }).click();
    
    // Assert
    await expect(page.locator('.result')).toBeVisible();
  });
});
```

## Best Practices

### 1. Use Environment Variables

Always use environment variables if possible or Playwright's baseURL configuration instead of hardcoded URLs:

> Note for this to be done the configurations regarding the environment configuration need to be in place in the `playwright.config.ts` in the root of the `petclinic-frontend` directory or else the use of process.env will not work. As mentioned [below](#environment-variables).

```typescript
describe('Homepage Tests', () => {
  test('should display homepage content', async ({ page }) => {
    // Option 1: Use relative URL with baseURL from config
    await page.goto('/home');
    
    // Option 2: Use environment variable with fallback
    const baseURL = process.env.BASE_URL || 'http://localhost:3000';
    await page.goto(`${baseURL}/home`);
    
    await expect(page.locator('h1')).toBeVisible();
  });
});
```

### 2. Use Web-First Assertions

```typescript
// GOOD: Web-first assertions wait automatically
await expect(page.locator('.element')).toBeVisible();
await expect(page.locator('.count')).toHaveCount(3);

// AVOID: Manual waits
await page.waitForSelector('.element');
expect(await page.locator('.element').isVisible()).toBe(true);
```

### 3. Prefer Role-Based Selectors

```typescript
// GOOD: Semantic selectors
await page.getByRole('button', { name: 'Submit' }).click();
await page.getByPlaceholder('Enter email').fill('test@example.com');

// AVOID: CSS selectors when role-based alternatives exist
await page.locator('#submit-btn').click();
```

### 4. Authentication Patterns

For tests requiring authentication, use a consistent pattern with proper URL handling:

```typescript
describe('Authenticated User Features', () => {
  test('authenticated user action', async ({ page }) => {
    // Option 1: Use relative URLs if baseURL is configured
    await page.goto('/users/login');
    
    // Option 2: Use environment variable
    const baseURL = process.env.BASE_URL || 'http://localhost:3000';
    await page.goto(`${baseURL}/users/login`);
    
    // Authenticate as admin
    await page.getByPlaceholder('Enter your email').fill('admin@admin.com');
    await page.getByPlaceholder('Enter your password').fill('pwd');
    await page.getByRole('button', { name: 'Login' }).click();
    
    // Wait for navigation
    await page.waitForURL('**/home');
    await expect(page.getByRole('button', { name: 'Admin' })).toBeVisible();
    
    // Test logic here
  });
});
```

### 5. Proper Test Cleanup

```typescript
describe('Feature Tests', () => {
  test('test name', async ({ page }) => {
    // Test logic
    
    // Cleanup
    await page.close();
  });
});
```

## Configuration

### Important: Vite vs Playwright Environment Variables

**Key Difference**: Playwright tests run in Node.js, while Vite environment variables are only available in the browser context.

- **Vite Environment Variables** (`VITE_*`): Available only during build time and in browser runtime
- **Playwright Tests**: Run in Node.js and access `process.env` directly
- **Solution**: Use Node.js environment variables or configure dotenv in Playwright config

### Environment Variables

**Important**: Playwright tests run in Node.js, not in the browser, so they cannot access Vite environment variables (those prefixed with `VITE_`). Instead, use standard Node.js environment variables or configure dotenv in the Playwright config.

#### Option 1: Use Node.js Environment Variables

```typescript
// Use standard Node.js environment variables
const baseURL = process.env.FRONTEND_URL || 'http://localhost:3000';
const apiURL = process.env.BACKEND_URL || 'http://localhost:8080/api';
```

#### Option 2: Configure dotenv in Playwright Config

Add dotenv configuration to `playwright.config.ts`:

```typescript
import { defineConfig } from '@playwright/test';
import dotenv from 'dotenv';
import path from 'path';

// Load environment variables from .env file
dotenv.config({ path: path.resolve(__dirname, '.env.test') });

export default defineConfig({
  // ... rest of config
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:3000',
  },
});
```

#### Option 3: Set baseURL in Playwright Config

```typescript
// In playwright.config.ts
export default defineConfig({
  use: {
    baseURL: 'http://localhost:3000',
  },
});

// In tests - use relative URLs
await page.goto('/home');
```

Common environment variables for tests:

- `BASE_URL` or `FRONTEND_URL`: Frontend application URL
- `API_URL` or `BACKEND_URL`: Backend API URL

### Test Configuration File

The `playwright.config.ts` file configures:

- Test directory: `./tests`
- Parallel execution settings
- Browser configurations (Chromium, Firefox, WebKit)

### Running Tests

```bash
# Run all tests
npx playwright test

# Run tests in specific browser
npx playwright test --project=chromium

# Run tests in headed mode
npx playwright test --headed

# Run specific test file
npx playwright test tests/homepage/homepagetests.spec.ts
```

## Code Quality Requirements

### ESLint Compliance

All test files must pass ESLint validation:

- No skipped tests without justification
- Proper async/await usage
- Clear test descriptions
- No hardcoded waits using `page.waitForTimeout()`

### Test Requirements

- Each test must contain at least one assertion
- All tests must be wrapped in describe blocks for organization
- Use descriptive test names that explain the expected behavior
- Use relative URLs with baseURL configuration or Node.js environment variables (not Vite environment variables)
- Limit nested describe blocks to maximum of 1 level

## Debugging Tests

### Development Tools

```bash
# Run tests with debug mode
npx playwright test --debug

# Generate test report
npx playwright show-report
```

### Common Issues

1. **Missing await statements**: ESLint will catch these
2. **Flaky selectors**: Use role-based selectors when possible
3. **Race conditions**: Rely on web-first assertions instead of manual waits
