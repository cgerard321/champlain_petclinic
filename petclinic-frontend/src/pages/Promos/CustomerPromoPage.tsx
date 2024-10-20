import { NavBar } from '@/layouts/AppNavBar.tsx';
import CardPromos from '@/features/promos/components/CardPromos.tsx';
export default function CustomerPromoPage(): JSX.Element {
  return (
    <div>
      <NavBar />
      <CardPromos />
    </div>
  );
}
