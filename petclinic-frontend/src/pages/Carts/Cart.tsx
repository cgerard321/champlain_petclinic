import { NavBar } from '@/layouts/AppNavBar';
import CartTable from '@/features/carts/components/CartTable.tsx';

export default function CartPage(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1>Carts</h1>
      <CartTable />
    </div>
  );
}
