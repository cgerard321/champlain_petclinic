import { useTranslation } from 'react-i18next';
import { matchesFilters } from '@/features/products/utils/productFilters';
import * as React from 'react';
import { useState, useEffect, useCallback, useMemo, useRef, JSX } from 'react';
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
import './components/Image.css';
//import ProductSearch from './components/ProductSearch';
import Reveal from '@/shared/components/animations/Reveal';
import {
  addProductToRecentlyViewed,
  loadRecentlyViewed,
} from './utils/recentlyViewed';

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
    availability?: string;
  };
  sortCriteria?: string;
  onClearFilters?: () => void;
}

const ProductList = function Productlist({
  searchQuery,
  view,
  filters,
  sortCriteria,
  onClearFilters,
}: ProductsListProps): JSX.Element {
  const { t, i18n } = useTranslation('products');
  const language = i18n.resolvedLanguage || i18n.language || 'en';
  const [productList, setProductList] = useState<ProductModel[]>([]);
  const [bundleList, setBundleList] = useState<ProductBundleModel[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const { user } = useUser();
  const [recentlyClickedProducts, setRecentlyClickedProducts] = useState<
    ProductModel[]
  >([]);
  const [filteredList, setFilteredList] = useState<ProductModel[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const catalogRef = useRef<HTMLDivElement>(null);

  const handlePageChange = (direction: number): void => {
    setCurrentPage(page => page + direction);
    catalogRef.current?.scrollIntoView({
      behavior: window.matchMedia('(prefers-reduced-motion: reduce)').matches
        ? 'instant'
        : 'smooth',
      block: 'start',
    });
  };

  const fetchProducts = useCallback(async (): Promise<void> => {
    setIsLoading(true);
    try {
      const list = await getAllProducts(
        filters.minPrice,
        filters.maxPrice,
        filters.minStars,
        filters.maxStars,
        filters.deliveryType ?? '',
        filters.productType?.includes(',') ? '' : filters.productType ?? ''
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
  }, [fetchProducts]);

  useEffect(() => {
    let cancelled = false;
    setRecentlyClickedProducts([]);
    void loadRecentlyViewed(user.userId).then(products => {
      if (!cancelled) setRecentlyClickedProducts(products);
    });
    return () => {
      cancelled = true;
    };
  }, [user.userId]);

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

    setFilteredList(
      sorted.filter(product =>
        matchesFilters(product, filters, { query: searchQuery, language, t })
      )
    );
  }, [sortCriteria, productList, searchQuery, language, t, filters]);

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

  const visiblePageNumbers = useMemo(() => {
    const firstPage = Math.max(
      0,
      Math.min(currentPage - 2, Math.max(totalPages - 5, 0))
    );
    return Array.from(
      { length: Math.min(totalPages, 5) },
      (_, index) => firstPage + index
    );
  }, [currentPage, totalPages]);

  useEffect(() => {
    setCurrentPage(0);
  }, [filters, searchQuery, sortCriteria]);

  useEffect(() => {
    setCurrentPage(page => Math.min(page, Math.max(totalPages - 1, 0)));
  }, [totalPages]);

  const handleProductClick = (product: ProductModel): void => {
    addProductToRecentlyViewed(product, user.userId);

    setRecentlyClickedProducts(prev => {
      const updated = prev.filter(p => p.productId !== product.productId);
      updated.unshift(product);
      return updated.slice(0, 5);
    });
  };

  return (
    <div className="product-list-container">
      <div className="main-content">
        {view === 'catalog' && (
          <Reveal delay={80}>
            <div ref={catalogRef} className="products-section-container ">
              <div className="shop-section-heading">
                <div>
                  <h2 className="section-title category-title">
                    {t('catalog')}
                  </h2>
                  <p>{t('catalogHint')}</p>
                </div>
                {!isLoading && (
                  <span>
                    {t('productCount', { count: filteredList.length })}
                  </span>
                )}
              </div>
              {isLoading ? (
                <div
                  className="grid catalog-grid products-loading-grid"
                  role="status"
                  aria-label={t('loading')}
                >
                  {Array.from({ length: CATALOG_PAGE_SIZE }, (_, index) => (
                    <div className="product-card-skeleton" key={index}>
                      <span className="skeleton-image" />
                      <span className="skeleton-line skeleton-line-title" />
                      <span className="skeleton-line skeleton-line-short" />
                      <span className="skeleton-line skeleton-line-button" />
                    </div>
                  ))}
                </div>
              ) : filteredList.length > 0 ? (
                <div className="grid catalog-grid">
                  {paginatedProducts.map((p, i) => (
                    <Reveal key={p.productId} delay={i * 60 + 120}>
                      <div onClick={() => handleProductClick(p)}>
                        <Product
                          product={
                            productList.find(
                              current => current.productId === p.productId
                            ) ?? p
                          }
                          recentlyViewed={recentlyClickedProducts.some(
                            recentlyViewedProduct =>
                              recentlyViewedProduct.productId === p.productId
                          )}
                        />
                      </div>
                    </Reveal>
                  ))}
                </div>
              ) : (
                <div className="no-products-results">
                  <p>
                    {filters.availability ||
                    filters.minPrice !== undefined ||
                    filters.maxPrice !== undefined ||
                    filters.deliveryType ||
                    filters.productType ||
                    (filters.minStars ?? 0) > 0 ||
                    (filters.maxStars ?? 5) < 5
                      ? t('noFilteredItems')
                      : t('noItems')}
                  </p>
                  {onClearFilters && (
                    <button type="button" onClick={onClearFilters}>
                      {t('clearFilters')}
                    </button>
                  )}
                </div>
              )}
              {!isLoading && filteredList.length > 0 && (
                <div className="catalog-pagination-container">
                  <nav
                    className="catalog-pagination"
                    aria-label={t('productPages')}
                  >
                    <button
                      type="button"
                      className="catalog-pagination-button"
                      onClick={() => handlePageChange(-1)}
                      disabled={currentPage === 0}
                      aria-label={t('previousProductPage')}
                      title={t('previousPage')}
                    >
                      <span aria-hidden="true">&lsaquo;</span>
                    </button>

                    {visiblePageNumbers.map(pageNumber => (
                      <button
                        type="button"
                        key={pageNumber}
                        className={`catalog-page-number${pageNumber === currentPage ? ' active' : ''}`}
                        aria-current={
                          pageNumber === currentPage ? 'page' : undefined
                        }
                        aria-label={t('currentProductPage', {
                          count: pageNumber + 1,
                        })}
                        onClick={() => {
                          setCurrentPage(pageNumber);
                          catalogRef.current?.scrollIntoView({
                            behavior: window.matchMedia(
                              '(prefers-reduced-motion: reduce)'
                            ).matches
                              ? 'instant'
                              : 'smooth',
                            block: 'start',
                          });
                        }}
                      >
                        {pageNumber + 1}
                      </button>
                    ))}

                    <button
                      type="button"
                      className="catalog-pagination-button"
                      onClick={() => handlePageChange(1)}
                      disabled={currentPage + 1 >= totalPages}
                      aria-label={t('nextProductPage')}
                      title={t('nextPage')}
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
              <div className="products-section-container shop-bundles-section">
                <div className="shop-section-heading">
                  <div>
                    <h2 className="section-title">{t('bundles')}</h2>
                    <p>{t('bundlesHint')}</p>
                  </div>
                </div>
                <div className="grid product-bundles-grid">
                  {bundleList.length > 0 ? (
                    bundleList.map((b, i) => (
                      <Reveal key={b.bundleId} delay={i * 60 + 120}>
                        <ProductBundle bundle={b} />
                      </Reveal>
                    ))
                  ) : (
                    <p>{t('noBundles')}</p>
                  )}
                </div>
              </div>
            </Reveal>
          </>
        )}
      </div>
    </div>
  );
};
export default React.memo(ProductList);
