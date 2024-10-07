import { NavBar } from '@/layouts/AppNavBar';
import PromoListTable from '@/features/promos/components/PromoTable.tsx';

export default function PromoPage(): JSX.Element {
  return (
    <div>
      <NavBar />
      <PromoListTable />
    </div>
  );
}
