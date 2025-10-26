# Angular 20 Employee Frontend

Modern Angular 20 frontend for Champlain Pet Clinic employees.

## Production Access

**Live Application**: [https://petclinic-backend.benmusicgeek.synology.me/employee/](https://petclinic-backend.benmusicgeek.synology.me/employee/)

## Quick Start

```bash
npm install

npm run dev

npm run build
```

## Tech Stack

- **Angular 20.3.7** - Latest Angular framework
- **Vite 7.1.12** - Build tool and dev server
- **TypeScript 5.9.3** - Type-safe JavaScript
- **Angular Material 20.2.10** - UI components
- **Node.js 22.12.0** - Runtime (Docker)
- **Nginx Alpine** - Production server

## Project Structure

```
src/app/
├── core/           # Services, guards, interceptors
│   ├── guards/     # Route guards
│   ├── interceptors/ # HTTP interceptors
│   └── services/   # Core services
├── features/       # Feature components
├── shared/         # Shared components
│   ├── api/        # API services
│   └── components/ # Reusable components
└── app.component.* # Main component
```

## Key Features

- **Material Design** - Replaced Bootstrap with Angular Material
- **Standalone Components** - Modern Angular architecture

## Angular Material

### What's Already Set Up

- **Material Theme** - Indigo-pink theme imported
- **Animations** - Enabled in main.ts
- **Material Icons** - Google Fonts loaded

### Using Material Components

```typescript
// Import the module you need
import { MatSlideToggleModule } from '@angular/material/slide-toggle';

@Component({
  imports: [MatSlideToggleModule]
})
```

```html
<mat-slide-toggle>Toggle me!</mat-slide-toggle>
```

### Adding New Material Components

```typescript
// 1. Import the module
import { MatButtonModule } from '@angular/material/button';

// 2. Add to component imports
@Component({
  imports: [MatButtonModule]
})
```

### App-Wide Styling

```scss
// src/style.css - Global styles
@import '@angular/material/prebuilt-themes/indigo-pink.css';

body {
  font-family: 'Roboto', sans-serif;
  margin: 0;
  padding: 0;
}

html,
body {
  height: 100%;
}
```

```scss
// Component-specific styles
.feature-card {
  background: #f8f9fa;
  border-radius: 12px;
  transition: transform 0.3s ease;
}
```

### More Information

- [Angular Material Documentation](https://material.angular.dev/)

## Development

- **Dev Server**: `npm run dev` (port 4200)
- **Build**: `npm run build`
- **Tests**: `playwright test` (E2E tests)
- **Test Debug**: `playwright test --debug` (Debug mode)
- **Lint**: `npm run lint`
- **Lint Fix**: `npm run lint:fix` (Auto-fix linting issues)

## Docker Support

- **Multi-stage build** with Nginx
- **Port 4200** exposed

## Testing

- **Playwright** E2E testing
- **Chrome, Firefox, Safari** support
- **CI/CD ready** configuration

## Migration Plan

This is the setup for the new Angular 20 employee frontend. Each team should be responsible for migrating their own components from the old Angular application to respect all the configuration of the new employee frontend:

### Team Responsibilities

- **Each team** - Responsible for their own component migration
- **Follow standards** - Use this Angular 20 setup as the standard
- **Material Design** - Convert Bootstrap components to Angular Material
- **Standalone components** - Convert to new Angular 20 architecture
- **Testing** - Update tests to work with new setup
- **Documentation** - Update component documentation

### Migration Checklist for Each Team

1. **Audit components** - Identify components to migrate
2. **Update imports** - Convert to standalone components
3. **Material conversion** - Replace Bootstrap with Material components
4. **Styling updates** - Apply Material Design theme
5. **Testing updates** - Update test files
6. **Documentation** - Update component docs
