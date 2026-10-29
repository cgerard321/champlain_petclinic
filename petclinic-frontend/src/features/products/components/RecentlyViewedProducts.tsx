import { useTranslation } from 'react-i18next';
import { useState, useEffect } from 'react';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import Product from './Product';
import { loadRecentlyViewed } from '../utils/recentlyViewed';
import '@/features/products/ProductList.css';
import { useNavigate } from 'react-router-dom';
import { useUser } from '@/context/UserContext';

interface RecentlyViewedProductsProps {
  compact?: boolean;
}

export default function RecentlyViewedProducts({
  compact = false,
}: RecentlyViewedProductsProps): JSX.Element | null {
  const { t } = useTranslation('products');
  const { user } = useUser();
  const [recentlyClickedProducts, setRecentlyClickedProducts] = useState<
    ProductModel[]
  >([]);
  const navigate = useNavigate();

  useEffect(() => {
    let cancelled = false;
    setRecentlyClickedProducts([]);
    void loadRecentlyViewed(user?.userId).then(products => {
      if (!cancelled) setRecentlyClickedProducts(products);
    });
    return () => {
      cancelled = true;
    };
  }, [user?.userId]);
  const handleProductClick = (productId: string): void => {
    navigate(`/products/${productId}`);
  };

  if (recentlyClickedProducts.length === 0) {
    return null;
  }

  return (
    <section
      className={`products-section-container${compact ? ' shop-recently-viewed' : ''}`}
    >
      <div className="shop-section-heading">
        <h2 className="section-title">{t('recent')}</h2>
        {compact && <span>{t('recentHint')}</span>}
      </div>
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
                <Product
                  key={product.productId}
                  product={product}
                  recentlyViewed
                  compact={compact}
                />
              </div>
            ))
        ) : (
          <p>{t('noRecent')}</p>
        )}
      </div>
    </section>
  );

  /**recently-viewed-list used to be grid changed it since I wanted it to be horizontally scrollable */
}
