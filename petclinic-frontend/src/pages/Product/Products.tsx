import { NavBar } from '@/layouts/AppNavBar.tsx';
import ProductsList from '@/features/products/ProductsList.tsx';

export default function Products(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1>Products</h1>
      <ProductsList />

      <h3>Here are the trending Products</h3>
    </div>
  );
}
