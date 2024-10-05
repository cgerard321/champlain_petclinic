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
          <div
            className={`card ${product.productQuantity < 10 ? 'low-quantity' : ''}`}
            key={product.productId}
          >
            <h2>{product.productName}</h2>
            <ImageContainer imageId={product.imageId} />
            <p>{product.productDescription}</p>
            <p>Rating: {product.averageRating.toFixed(1)}</p>
            <p>Price: ${product.productSalePrice.toFixed(2)}</p>
            {/* Conditionally display the "Out of Stock" or "Low Stock" message */}
            {product.productQuantity === 0 ? (
              <p className="out-of-stock">Out of Stock</p>
            ) : product.productQuantity < 10 ? (
              <p className="low-stock">Low Stock</p>
            ) : null}
          </div>
        ))}
      </div>
    </div>
  );
}
