import { NavBar } from '@/layouts/AppNavBar.tsx';
import ProductsList from '@/features/products/ProductsList.tsx';
import ProductSearch from '@/features/products/components/ProductSearch';
import './Products.css';
import TrendingList from '@/features/products/TrendingList';

export default function Products(): JSX.Element {
  return (
    <div>
      <NavBar />
      <ProductSearch />
      <h1>Products</h1>
      <h3>Here are the trending Products</h3>
      <TrendingList />
      <hr />
      <ProductsList />
    </div>
  );
}
