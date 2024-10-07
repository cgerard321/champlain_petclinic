import { NavBar } from '@/layouts/AppNavBar.tsx';
import UpdatePromo from '@/features/promos/components/UpdatePromo.tsx';

export default function UpdatePromoPage(): JSX.Element {
  return (
    <div>
      <NavBar />
      <div className="update-promo-page">
        <UpdatePromo />
      </div>
    </div>
  );
}
