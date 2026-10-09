import * as React from 'react';
import { useState, useEffect, useCallback, useMemo, JSX } from 'react';
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
import Reveal from '@/shared/components/animations/Reveal';

const CATALOG_PAGE_SIZE = 8;

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

const ProductList = function Productlist({
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
  const [currentPage, setCurrentPage] = useState(0);

  const fetchProducts = useCallback(async (): Promise<void> => {
    setIsLoading(true);
    try {
      const list = await getAllProducts(
        filters.minPrice,
        filters.maxPrice,
        filters.minStars,
        filters.maxStars,
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
      const query = searchQuery.toLowerCase();
      setFilteredList(
        sorted.filter(
          p =>
            p.productName.toLowerCase().includes(query) ||
            p.productType?.toLowerCase().includes(query)
        )
      );
    }
  }, [sortCriteria, productList, searchQuery]);

  const totalPages = Math.ceil(filteredList.length / CATALOG_PAGE_SIZE);
  const firstVisibleProduct = currentPage * CATALOG_PAGE_SIZE + 1;
  const lastVisibleProduct = Math.min(
    (currentPage + 1) * CATALOG_PAGE_SIZE,
    filteredList.length
  );

  const paginatedProducts = useMemo(() => {
    const startIndex = currentPage * CATALOG_PAGE_SIZE;
    return filteredList.slice(startIndex, startIndex + CATALOG_PAGE_SIZE);
  }, [currentPage, filteredList]);

  useEffect(() => {
    setCurrentPage(0);
  }, [filters, searchQuery, sortCriteria]);

  useEffect(() => {
    setCurrentPage(page => Math.min(page, Math.max(totalPages - 1, 0)));
  }, [totalPages]);

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
    <Reveal delay={80}>
      <div className="products-section-container ">
        <h2 className="section-title category-title">Recently Seen</h2>
        <div className="recently-viewed-flex">
          {recentlyClickedProducts.length > 0 ? (
            recentlyClickedProducts
              .filter(p => !p.isUnlisted)
              .map((p, i) => (
                <Reveal key={p.productId} delay={i * 60 + 120}>
                  <Product product={p} />
                </Reveal>
              ))
          ) : (
            <p>No Recently Seen Items.</p>
          )}
        </div>
      </div>
    </Reveal>
  );

  return (
    <div className="product-list-container">
      <div className="main-content">
        {view === 'catalog' && (
          <Reveal delay={80}>
            <div className="products-section-container ">
              <h2 className="section-title category-title">Catalog</h2>
              {isLoading ? (
                <p>Loading items...</p>
              ) : filteredList.length > 0 ? (
                <div className="grid">
                  {paginatedProducts.map((p, i) => (
                    <Reveal key={p.productId} delay={i * 60 + 120}>
                      <div onClick={() => handleProductClick(p)}>
                        <Product product={p} />
                      </div>
                    </Reveal>
                  ))}
                </div>
              ) : (
                <p style={{ textAlign: 'center', margin: '40px 0' }}>
                  No items found.
                </p>
              )}
              {!isLoading && filteredList.length > 0 && (
                <div className="catalog-pagination-container">
                  <nav
                    className="catalog-pagination"
                    aria-label="Product pages"
                  >
                    <button
                      type="button"
                      className="catalog-pagination-button"
                      onClick={() => setCurrentPage(page => page - 1)}
                      disabled={currentPage === 0}
                      aria-label="Previous product page"
                      title="Previous page"
                    >
                      <span aria-hidden="true">&lsaquo;</span>
                    </button>

                    <span
                      className="catalog-page-number active"
                      aria-current="page"
                      aria-label={`Current product page ${currentPage + 1}`}
                    >
                      {currentPage + 1}
                    </span>

                    <button
                      type="button"
                      className="catalog-pagination-button"
                      onClick={() => setCurrentPage(page => page + 1)}
                      disabled={currentPage + 1 >= totalPages}
                      aria-label="Next product page"
                      title="Next page"
                    >
                      <span aria-hidden="true">&rsaquo;</span>
                    </button>
                  </nav>

                  <span
                    className="catalog-pagination-status"
                    aria-live="polite"
                  >
                    {firstVisibleProduct}&ndash;{lastVisibleProduct} /{' '}
                    {filteredList.length}
                  </span>
                </div>
              )}
            </div>
          </Reveal>
        )}

        {view === 'extras' && (
          <>
            <Reveal delay={80}>
              <div className="products-section-container ">
                <h2 className="section-title">Bundles</h2>
                <div className="grid product-bundles-grid">
                  {bundleList.length > 0 ? (
                    bundleList.map((b, i) => (
                      <Reveal key={b.bundleId} delay={i * 60 + 120}>
                        <ProductBundle bundle={b} />
                      </Reveal>
                    ))
                  ) : (
                    <p>No Bundles Available</p>
                  )}
                </div>
              </div>
            </Reveal>
            <div>
              <hr />
            </div>
            <RecentlyViewedProducts />
          </>
        )}
      </div>
    </div>
  );
};
export default React.memo(ProductList);
