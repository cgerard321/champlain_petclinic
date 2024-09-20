import { NavBar } from '@/layouts/AppNavBar.tsx';
import ProductsList from '@/features/products/ProductsList.tsx';
import TrendingList from '@/features/products/TrendlingList';

export default function Products(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1>Products</h1>
      <h3>Here are the trending Products</h3>
      <TrendingList/>



      <hr />
    
      <ProductsList />
                 
    </div>
  );
}
