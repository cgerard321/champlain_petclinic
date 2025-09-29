import { NavBar } from '@/layouts/AppNavBar.tsx';
import InventoriesListTable from '@/features/inventories/InventoriesListTable.tsx';
import inventoryPageStyles from './Inventories.module.css';

export default function Inventories(): JSX.Element {
  return (
    <div>
      <NavBar />
      <div id={inventoryPageStyles.titleSection}>
        <h1
          id={inventoryPageStyles.pageTitle}
          className="text-primary fw-bold display-3"
        >
          Inventories
        </h1>
      </div>
      <InventoriesListTable />
    </div>
  );
}
