import { useState, useEffect } from 'react';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import Product from './Product';
import '@/features/products/ProductList.css';
import { useNavigate } from 'react-router-dom';
import { useUser } from '@/context/UserContext';

export default function RecentlyViewedProducts(): JSX.Element {
  const { user } = useUser();
  const [recentlyClickedProducts, setRecentlyClickedProducts] = useState<
    ProductModel[]
  >([]);
  const navigate = useNavigate();

  useEffect(() => {
    if (!user?.userId) return;
    const savedProducts = localStorage.getItem(
      `recentlyClickedProducts_${user.userId}`
    );
    if (savedProducts) {
      setRecentlyClickedProducts(JSON.parse(savedProducts));
    }
  }, [user]);
  const handleProductClick = (productId: string): void => {
    navigate(`/products/${productId}`);
  };

  return (
    <div className="products-section-container">
      <h2 className="section-title">Recently Seen</h2>
      <div className="recently-viewed-flex">
        {recentlyClickedProducts.length > 0 ? (
          recentlyClickedProducts
            .filter(product => !product.isUnlisted)
            .map(product => (
              <div
                key={product.productId}
                onClick={() => handleProductClick(product.productId)}
                style={{ cursor: 'pointer' }}
              >
                <Product key={product.productId} product={product} />
              </div>
            ))
        ) : (
          <p>No Recently Seen Items.</p>
        )}
      </div>
    </div>
  );

  /**recently-viewed-list used to be grid changed it since I wanted it to be horizontally scrollable */
}
