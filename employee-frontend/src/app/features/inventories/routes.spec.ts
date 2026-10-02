import { InventoryList } from './pages/inventory-list/inventory-list';
import routes from './routes';

describe('Inventory routes', () => {
  it('should have the inventory list route', () => {
    // CHANGE MADE BY VETS-CPC-1927 SO THAT `npm test` CAN COMPILE AT ALL
    // `routes[0]` was accessed without a guard, which TypeScript rejects with
    // "TS2532: Object is possibly 'undefined'" since PR #1489 turned on
    // `noUncheckedIndexedAccess` in tsconfig.json (2026-09-24, four days before this
    // file was added). tsconfig.spec.json puts every *.spec.ts in a single TypeScript
    // program, so this one error stopped EVERY test in the project from compiling -
    // including the new i18n tests. CI never caught it because the Node.js workflow
    // runs lint, lint:style and build, but never `npm test`.
    // Adding `?.` is the minimal fix and changes nothing when routes[0] exists, which
    // it does here. Flagged to the INVT team so they can confirm or replace it.
    // Assert
    expect(routes[0]?.component).toBe(InventoryList);
  });
});
