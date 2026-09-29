import { InventoryList } from './pages/inventory-list/inventory-list';
import routes from './routes';

describe('Inventory routes', () => {
  it('should have the inventory list route', () => {
    // Assert
    expect(routes[0].component).toBe(InventoryList);
  });
});
