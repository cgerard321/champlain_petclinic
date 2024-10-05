import { useState, useEffect } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import ImageContainer from './components/ImageContainer';

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
        {topFourTrending.map((product: ProductModel) => (
          <div className="card" key={product.productId}>
            <ImageContainer imageId={product.imageId} />
            <h2>{product.productName}</h2>
            <p>{product.productDescription}</p>
            <p>Rating: {product.averageRating.toFixed(1)}</p>
            <p>Price: ${product.productSalePrice.toFixed(2)}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
