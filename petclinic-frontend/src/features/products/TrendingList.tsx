import { useState, useEffect } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import Product from './components/Product';

export default function TrendingList(): JSX.Element {
  const [trendingList, setTrendingList] = useState<ProductModel[]>([]);

  const fetchProducts = async (): Promise<void> => {
    const list = await getAllProducts();
    setTrendingList(list);
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  const topFourTrending = [...trendingList]
    .sort((a, b) => b.requestCount - a.requestCount)
    .slice(0, 4);

  return (
    <div>
      <div className="grid">
        {topFourTrending.map((product: ProductModel) => {
          return <Product key={product.productId} product={product} />;
        })}
      </div>
    </div>
  );
}
