import AddPromo from '@/features/promos/components/AddPromo.tsx';
import { NavBar } from '@/layouts/AppNavBar.tsx';

export default function AddPromoPage(): JSX.Element {
  return (
    <div>
      <NavBar />
      <div className="add-promo-page">
        <AddPromo />
      </div>
    </div>
  );
}
