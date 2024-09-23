import { NavBar } from '@/layouts/AppNavBar.tsx';
import ProductsList from '@/features/products/ProductsList.tsx';
import ProductSearch from '@/features/products/components/ProductSearch';
import "./Products.css"
export default function Products(): JSX.Element {
  return (
    <div>
      <NavBar />
        <ProductSearch />
        <h1>Products</h1>
      <ProductsList />
    </div>
  );
}
