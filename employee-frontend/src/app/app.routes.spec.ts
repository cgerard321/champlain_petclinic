import { routes } from './app.routes';

describe('Application routes (VETS-CPC-2089)', () => {
  it('guards the settings route', () => {
    // Arrange
    const settings = routes.find((route) => route.path === 'settings');

    // Assert
    expect(settings).toBeDefined();
    expect(settings?.canActivate).toHaveLength(3);
  });
});
