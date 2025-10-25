import { useState, useEffect } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import Product from './components/Product';
import { useUser } from '@/context/UserContext';

export default function TrendingList(): JSX.Element {
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
  const addToRecentlyViewed = (product: ProductModel): void => {
    if (!user?.userId) return;

    const savedProducts = localStorage.getItem(
      `recentlyClickedProducts_${user.userId}`
    );
    const currentProducts = savedProducts ? JSON.parse(savedProducts) : [];
    const updatedProducts = currentProducts.filter(
      (p: ProductModel) => p.productId !== product.productId
    );

    updatedProducts.unshift(product);

    if (updatedProducts.length > 5) {
      updatedProducts.pop();
    }
    localStorage.setItem(
      `recentlyClickedProducts_${user.userId}`,
      JSON.stringify(updatedProducts)
    );
  };

  const handleProductClick = (product: ProductModel): void => {
    addToRecentlyViewed(product);
  };

  const topFourTrending = [...trendingList]
    .sort((a, b) => b.requestCount - a.requestCount)
    .slice(0, 4);

  return (
    <div className="products-section-container ">
      <h2 className="section-title category-title">Trending</h2>
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
