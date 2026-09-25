import { InventoryList } from './pages/inventory-list/inventory-list';
import { SupplyPage } from './pages/supply-page/supply-page';
import routes from './routes';

describe('Inventory routes', () => {
  it('should have the inventory list route', () => {
    // Assert
    expect(routes[0].component).toBe(InventoryList);
  });

  it('should have the supply route', () => {
    // Arrange
    const supplyRoute = routes.find((route) => route.path === ':inventoryId/supplies');

    // Assert
    expect(supplyRoute).toBeDefined();
    expect(supplyRoute?.component).toBe(SupplyPage);
  });
});
