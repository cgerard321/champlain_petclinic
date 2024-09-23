import { NavBar } from '@/layouts/AppNavBar.tsx';
import InventoriesListTableV2 from '@/features/inventories/models/InventoriesListTableV2.tsx';

export default function Inventories(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1>Inventories</h1>
      <InventoriesListTableV2 />
    </div>
  );
}
