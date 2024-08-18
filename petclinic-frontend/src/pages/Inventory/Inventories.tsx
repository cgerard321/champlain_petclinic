import { NavBar } from '@/layouts/AppNavBar.tsx';
import InventoriesListTable from '@/features/inventories/InventoriesListTable.tsx';

export default function Inventories(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1>Inventories</h1>
      <InventoriesListTable />
    </div>
  );
}
