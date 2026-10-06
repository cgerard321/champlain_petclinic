import { Route } from '@angular/router';

import { routes } from './app.routes';

describe('Application routes', () => {
  function shellRoute(): Route | undefined {
    // The shell is the only top-level route with an empty path; it wraps every page that has
    // to show the header, the user menu and the language switcher.
    return routes.find((route) => route.path === '');
  }

  function settingsRoute(): Route | undefined {
    return shellRoute()?.children?.find((route) => route.path === 'settings');
  }

  // 1. POSITIVE - Settings lives inside the shell.
  // VETS-CPC-2090 criterion 16: the page must be displayed inside the employee portal shell,
  // so it has to be a child of the shell route and not a root route of its own.
  it('nests the settings route under the shell', () => {
    // Assert
    expect(settingsRoute()).toBeDefined();
    expect(routes.some((route) => route.path === 'settings')).toBe(false);
  });

  // 2. POSITIVE - The guards still protect it, through the parent.
  it('guards the settings route through the shell route', () => {
    // Assert
    expect(shellRoute()?.canActivate).toHaveLength(3);
  });

  // 3. NEGATIVE - No duplicated guards.
  // The parent already runs the three guards; repeating them on the child would run them
  // twice on every navigation.
  it('does not repeat the guards on the settings route itself', () => {
    // Assert
    expect(settingsRoute()?.canActivate).toBeUndefined();
  });
});
