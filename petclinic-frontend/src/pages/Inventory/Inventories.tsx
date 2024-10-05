import { NavBar } from '@/layouts/AppNavBar.tsx';
import InventoriesListTable from '@/features/inventories/InventoriesListTable.tsx';
import './Inventories.css';

export default function Inventories(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1 id="page-title">Inventories</h1>
      <InventoriesListTable />
    </div>
  );
}
