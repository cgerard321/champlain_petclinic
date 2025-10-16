import { useState, useEffect, useCallback, JSX } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import './ProductList.css';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import Product from './components/Product';
import { useUser } from '@/context/UserContext';
import './components/Sidebar.css';
//import StarRating from '@/features/products/components/StarRating.tsx';
import './components/StarRating.css';
//import { ProductType } from '@/features/products/api/ProductTypeEnum.ts';
import { getAllProductBundles } from './api/getAllProductBundles';
import { ProductBundleModel } from './models/ProductModels/ProductBundleModel';
import ProductBundle from './components/ProductBundle';
//import ProductSearch from './components/ProductSearch';

interface ProductsListProps {
  searchQuery: string;
  view: 'catalog' | 'extras';
  filters: {
    minPrice?: number;
    maxPrice?: number;
    ratingSort?: string;
    minStars?: number;
    maxStars?: number;
    deliveryType?: string;
    productType?: string;
  };
  sortCriteria?: string;
}

export default function ProductList({
  searchQuery,
  view,
  filters,
  sortCriteria,
}: ProductsListProps): JSX.Element {
  const [productList, setProductList] = useState<ProductModel[]>([]);
  const [bundleList, setBundleList] = useState<ProductBundleModel[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const { user } = useUser();
  const [recentlyClickedProducts, setRecentlyClickedProducts] = useState<
    ProductModel[]
  >([]);
  const [filteredList, setFilteredList] = useState<ProductModel[]>([]);

  const fetchProducts = useCallback(async (): Promise<void> => {
    setIsLoading(true);
    try {
      const list = await getAllProducts(
        filters.minPrice,
        filters.maxPrice,
        filters.minStars,
        filters.maxStars,
        //filters.ratingSort ?? 'default',
        filters.deliveryType ?? '',
        filters.productType ?? ''
      );
      const visible = list.filter(p => !p.isUnlisted);
      setProductList(visible);
      setFilteredList(visible);
    } catch (err) {
      console.error('Error fetching products:', err);
      setProductList([]);
      setFilteredList([]);
    } finally {
      setIsLoading(false);
    }

    try {
      const bundles = await getAllProductBundles();
      setBundleList(bundles);
    } catch (err) {
      console.error('Error fetching product bundles:', err);
    }
  }, [filters]);

  useEffect(() => {
    fetchProducts();
    const saved = localStorage.getItem(
      `recentlyClickedProducts_${user.userId}`
    );
    if (saved) setRecentlyClickedProducts(JSON.parse(saved));
  }, [fetchProducts, user.userId]);

  useEffect(() => {
    const sorted = [...productList];

    switch (sortCriteria) {
      case 'rating-desc':
        sorted.sort((a, b) => b.averageRating - a.averageRating);
        break;
      case 'rating-asc':
        sorted.sort((a, b) => a.averageRating - b.averageRating);
        break;
      case 'price-desc':
        sorted.sort((a, b) => b.productSalePrice - a.productSalePrice);
        break;
      case 'price-asc':
        sorted.sort((a, b) => a.productSalePrice - b.productSalePrice);
        break;
      default:
        break;
    }

    if (searchQuery === '') {
      setFilteredList(sorted);
    } else {
      setFilteredList(
        sorted.filter(p =>
          p.productName.toLowerCase().includes(searchQuery.toLowerCase())
        )
      );
    }
  }, [sortCriteria, productList, searchQuery]);

  // useEffect(() => {
  //   if (searchQuery === '') {
  //     setFilteredList(productList);
  //   } else {
  //     setFilteredList(
  //       productList.filter(p =>
  //         p.productName.toLowerCase().includes(searchQuery.toLowerCase())
  //       )
  //     );
  //   }
  // }, [searchQuery, productList]);

  const handleProductClick = (product: ProductModel): void => {
    setRecentlyClickedProducts(prev => {
      const updated = prev.filter(p => p.productId !== product.productId);
      updated.unshift(product);
      if (updated.length > 5) updated.pop();
      localStorage.setItem(
        `recentlyClickedProducts_${user.userId}`,
        JSON.stringify(updated)
      );
      return updated;
    });
  };

  const RecentlyViewedProducts = (): JSX.Element => (
    <div className="recently-viewed-container">
      <h2 className="section-header">Recently Seen</h2>
      <div className="recently-viewed-flex">
        {recentlyClickedProducts.length > 0 ? (
          recentlyClickedProducts
            .filter(p => !p.isUnlisted)
            .map(p => <Product key={p.productId} product={p} />)
        ) : (
          <p>No Recently Seen Items.</p>
        )}
      </div>
    </div>
  );

  return (
    <div className="product-list-container">
      <div className="main-content">
        {view === 'catalog' && (
          <div className="list-container">
            {/*<h2 className="section-header">Catalog</h2>*/}
            <div className="grid">
              {isLoading ? (
                <p>Loading items...</p>
              ) : filteredList.length > 0 ? (
                filteredList.map(p => (
                  <div key={p.productId} onClick={() => handleProductClick(p)}>
                    <Product product={p} />
                  </div>
                ))
              ) : (
                <p>No items found.</p>
              )}
            </div>
          </div>
        )}

        {view === 'extras' && (
          <>
            <div className="list-container">
              <h2 className="section-header">Bundles</h2>
              <div className="grid product-bundles-grid">
                {bundleList.length > 0 ? (
                  bundleList.map(b => (
                    <ProductBundle key={b.bundleId} bundle={b} />
                  ))
                ) : (
                  <p>No Bundles Available</p>
                )}
              </div>
            </div>
            <div>
              <hr />
            </div>
            <RecentlyViewedProducts />
          </>
        )}
      </div>
    </div>
  );
}
