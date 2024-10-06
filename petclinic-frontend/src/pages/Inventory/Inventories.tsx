import { NavBar } from '@/layouts/AppNavBar.tsx';
import InventoriesListTable from '@/features/inventories/InventoriesListTable.tsx';
import inventoryPageStyles from './Inventories.module.css';

export default function Inventories(): JSX.Element {
  return (
    <div>
      <NavBar />
      <div id={inventoryPageStyles.titleSection}>
        <h1 id={inventoryPageStyles.pageTitle}>Inventories</h1>
      </div>
      <InventoriesListTable />
    </div>
  );
}
