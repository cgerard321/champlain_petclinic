import { useTranslation } from 'react-i18next';
import { useState, useEffect } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import Product from './components/Product';
import { useUser } from '@/context/UserContext';
import { addProductToRecentlyViewed } from './utils/recentlyViewed';

export default function TrendingList(): JSX.Element {
  const { t } = useTranslation('products');
  const [trendingList, setTrendingList] = useState<ProductModel[]>([]);
  const { user } = useUser();

  const fetchProducts = async (): Promise<void> => {
    const list = await getAllProducts();
    const filteredList = list.filter(product => !product.isUnlisted);
    setTrendingList(filteredList);
  };

  useEffect(() => {
    fetchProducts();
  }, []);
  const handleProductClick = (product: ProductModel): void => {
    addProductToRecentlyViewed(product, user?.userId);
  };

  const topFourTrending = [...trendingList]
    .sort((a, b) => b.requestCount - a.requestCount)
    .slice(0, 4);

  return (
    <div className="products-section-container shop-trending-section">
      <div className="shop-section-heading">
        <div>
          <h2 className="section-title category-title">{t('trending')}</h2>
          <p>{t('trendingHint')}</p>
        </div>
      </div>
      <div className="grid">
        {topFourTrending.map((product: ProductModel) => (
          <div
            key={product.productId}
            onClick={() => handleProductClick(product)}
            style={{ cursor: 'pointer' }}
          >
            <Product product={product} />
          </div>
        ))}
      </div>
    </div>
  );
}
