import { useState, useEffect } from 'react';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import Product from './Product';
import '@/features/products/ProductList.css';
import { useNavigate } from 'react-router-dom';

export default function RecentlyViewedProducts(): JSX.Element {
  const [recentlyClickedProducts, setRecentlyClickedProducts] = useState<
    ProductModel[]
  >([]);
  const navigate = useNavigate();

  useEffect(() => {
    const savedProducts = localStorage.getItem('recentlyClickedProducts');
    if (savedProducts) {
      setRecentlyClickedProducts(JSON.parse(savedProducts));
    }
  }, []);
  const handleProductClick = (productId: string): void => {
    navigate(`/products/${productId}`);
  };

  return (
    <div className="recently-viewed-container">
      <h2>Recently Seen Products</h2>
      <div className="grid">
        {recentlyClickedProducts.length > 0 ? (
          recentlyClickedProducts.map(product => (
            <div
              key={product.productId}
              onClick={() => handleProductClick(product.productId)}
              style={{ cursor: 'pointer' }}
            >
              <Product key={product.productId} product={product} />
            </div>
          ))
        ) : (
          <p>No recently clicked products.</p>
        )}
      </div>
    </div>
  );
}
