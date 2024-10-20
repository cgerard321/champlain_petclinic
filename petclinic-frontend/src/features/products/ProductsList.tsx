import { useState, useEffect, JSX } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import './ProductList.css';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import Product from './components/Product';
import AddProduct from './components/AddProduct';
import { addProduct } from '@/features/products/api/addProduct';
import { useUser } from '@/context/UserContext';
import './components/Sidebar.css';
import { getProductsByType } from '@/features/products/api/getProductsByType.ts';
// import AddImage from './components/AddImage';
import { addImage } from './api/addImage';
import { ImageModel } from './models/ProductModels/ImageModel';
import StarRating from '@/features/products/components/StarRating.tsx';
import './components/StarRating.css';
import { ProductType } from '@/features/products/api/ProductTypeEnum.ts';
import { getAllProductBundles } from './api/getAllProductBundles';
import { ProductBundleModel } from './models/ProductModels/ProductBundleModel';
import ProductBundle from './components/ProductBundle';

export default function ProductList(): JSX.Element {
  const [productList, setProductList] = useState<ProductModel[]>([]);
  const [bundleList, setBundleList] = useState<ProductBundleModel[]>([]);
  const [minPrice, setMinPrice] = useState<number | undefined>(undefined);
  const [maxPrice, setMaxPrice] = useState<number | undefined>(undefined);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const { user } = useUser();
  const [isRightRole, setIsRightRole] = useState<boolean>(false);
  const [isSidebarOpen, setIsSidebarOpen] = useState<boolean>(false);
  const [filterType, setFilterType] = useState<string>('');
  const [recentlyClickedProducts, setRecentlyClickedProducts] = useState<
    ProductModel[]
  >([]);
  const [ratingSort, setRatingSort] = useState<string>('default');
  const [minStars, setMinStars] = useState<number>(0);
  const [maxStars, setMaxStars] = useState<number>(5);
  const [validationMessage, setValidationMessage] = useState<string>('');

  const validationStars = async (
    minStars: number,
    maxStars: number
  ): Promise<void> => {
    if (minStars >= maxStars) {
      setValidationMessage(
        'Minimum stars cannot be greater than or equal to maximum stars.'
      );
    } else {
      setValidationMessage('');
    }
  };

  function FilterByPriceErrorHandling(): void {
    // Validate inputs for filter by price
    if (
      minPrice !== undefined &&
      maxPrice !== undefined &&
      minPrice > maxPrice
    ) {
      alert('Min Price cannot be greater than Max Price');
    }
  }

  const fetchProducts = async (): Promise<void> => {
    FilterByPriceErrorHandling();
    setIsLoading(true);
    try {
      if (filterType.trim() === '') {
        const list = await getAllProducts(
          minPrice,
          maxPrice,
          minStars,
          maxStars,
          ratingSort
        );
        const filteredList = list.filter(product => !product.isUnlisted);
        setProductList(filteredList);
      } else {
        const filteredList = await getProductsByType(filterType);
        setProductList(filteredList);
      }
    } catch (err) {
      console.error('Error fetching products:', err);
      setProductList([]);
    } finally {
      setIsLoading(false);
    }
    // Fetch product bundles
    try {
      const bundles = await getAllProductBundles();
      setBundleList(bundles);
    } catch (err) {
      console.error('Error fetching product bundles:', err);
    }
  };

  useEffect(() => {
    fetchProducts();
    const savedProducts = localStorage.getItem('recentlyClickedProducts');
    if (savedProducts) {
      setRecentlyClickedProducts(JSON.parse(savedProducts));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    const hasRightRole =
      user?.roles !== undefined &&
      Array.from(user.roles).some(
        role => role.name === 'ADMIN' || role.name === 'INVENTORY_MANAGER'
      );
    setIsRightRole(hasRightRole);
  }, [user]);

  const handleAddImage = async (formData: FormData): Promise<ImageModel> => {
    try {
      const createdImage = await addImage(formData);
      await fetchProducts();
      return createdImage;
    } catch (error) {
      console.error('Error adding image:', error);
      throw error;
    }
  };

  const handleAddProduct = async (
    product: ProductModel
  ): Promise<ProductModel> => {
    try {
      const savedProduct = await addProduct(product);
      await fetchProducts();
      return savedProduct;
    } catch (error) {
      throw error;
    }
  };

  const toggleSidebar = (): void => {
    setIsSidebarOpen(!isSidebarOpen);
  };

  // Handler to close sidebar when clicking outside
  const handleOverlayClick = (): void => {
    setIsSidebarOpen(false);
  };

  const clearFilters = async (): Promise<void> => {
    setMinPrice(undefined);
    setMaxPrice(undefined);
    setFilterType('');
    setRatingSort('');
    setMaxStars(5);
    setMinStars(0);
    setValidationMessage('');
    const list = await getAllProducts(minPrice, maxPrice, minStars, maxStars);
    setProductList(list);
  };

  const handleProductClick = (product: ProductModel): void => {
    setRecentlyClickedProducts(listOfProducts => {
      const updatedProducts = listOfProducts.filter(
        currentProduct => currentProduct.productId !== product.productId
      );

      updatedProducts.unshift(product);

      if (updatedProducts.length > 5) {
        updatedProducts.pop();
      }

      localStorage.setItem(
        'recentlyClickedProducts',
        JSON.stringify(updatedProducts)
      );

      return updatedProducts;
    });
  };
  const RecentlyViewedProducts = (): JSX.Element => (
    <div className="recently-viewed-container">
      <h2>Recently Seen Products</h2>
      <div className="grid">
        {recentlyClickedProducts.length > 0 ? (
          recentlyClickedProducts
            .filter(product => !product.isUnlisted)
            .map(product => (
              <Product key={product.productId} product={product} />
            ))
        ) : (
          <p>No recently clicked products.</p>
        )}
      </div>
    </div>
  );

  return (
    <div className="product-list-container">
      {isSidebarOpen && (
        <div className="overlay" onClick={handleOverlayClick}></div>
      )}

      <div
        className={`sidebar ${isSidebarOpen ? 'open' : ''}`}
        id="sidebar"
        aria-hidden={!isSidebarOpen}
      >
        <button
          className="close-button"
          onClick={toggleSidebar}
          aria-label="Close Filters"
        >
          &times;
        </button>
        <div className="filter-container">
          <h2>Filters</h2>
          <label>
            Min Price:
            <input
              type="number"
              value={minPrice ?? typeof 'number'}
              onChange={e =>
                setMinPrice(
                  e.target.value ? parseFloat(e.target.value) : undefined
                )
              }
              min="0"
              placeholder="e.g., 10"
            />
          </label>
          <label>
            Max Price:
            <input
              type="number"
              value={maxPrice ?? typeof 'number'}
              onChange={e =>
                setMaxPrice(
                  e.target.value ? parseFloat(e.target.value) : undefined
                )
              }
              min="0"
              placeholder="e.g., 100"
            />
          </label>
          <label>
            Product Type:
            <select
              value={filterType}
              onChange={e => setFilterType(e.target.value)}
            >
              <option value="">Select Product Type</option>
              {Object.values(ProductType).map(type => (
                <option key={type} value={type}>
                  {type.charAt(0).toUpperCase() + type.slice(1).toLowerCase()}
                </option>
              ))}
            </select>
          </label>

          <div className="star-rating-container">
            <h2>Filter by Star Rating</h2>
            <div className="star-row">
              <label>Min Stars:</label>
              <StarRating
                currentRating={minStars}
                viewOnly={false}
                updateRating={rating => {
                  setMinStars(rating);
                  validationStars(rating, maxStars);
                }}
              />
            </div>
            <div className="star-row">
              <label>Max Stars:</label>
              <StarRating
                currentRating={maxStars}
                viewOnly={false}
                updateRating={rating => {
                  setMaxStars(rating);
                  validationStars(minStars, rating);
                }}
              />
            </div>
            {validationMessage && (
              <div style={{ color: 'red' }}>{validationMessage}</div>
            )}
          </div>
          <select
            name="rating"
            value={ratingSort}
            onChange={e => setRatingSort(e.target.value)}
          >
            <option value="default">Sort by Rating</option>
            <option value="asc">Low to High</option>
            <option value="desc">High to Low</option>
          </select>
          <button
            className="apply-filter-button"
            disabled={validationMessage !== ''}
            onClick={fetchProducts}
          >
            Apply
          </button>
          <button className="clear-filter-button" onClick={clearFilters}>
            Clear
          </button>
        </div>
      </div>

      {isRightRole && (
        <AddProduct addProduct={handleAddProduct} addImage={handleAddImage} />
      )}
      <div className="main-content">
        <div className="product-bundle-container">
          <h2>Product Bundles</h2>
          <div className="grid product-bundles-grid">
            {bundleList.length > 0 ? (
              bundleList.map((bundle: ProductBundleModel) => (
                <ProductBundle key={bundle.bundleId} bundle={bundle} />
              ))
            ) : (
              <p>No product bundles available.</p>
            )}
          </div>
        </div>
        <div>
          <hr />
        </div>
        <div className="list-container">
          <h2>List Products</h2>
          {!isSidebarOpen && (
            <button
              className="toggle-sidebar-button"
              onClick={toggleSidebar}
              aria-expanded={isSidebarOpen}
              aria-controls="sidebar"
            >
              &#9776; Filters
            </button>
          )}
          <div className="grid">
            {isLoading ? (
              <p>Loading products...</p>
            ) : productList.length > 0 ? (
              productList.map((product: ProductModel) => (
                <div
                  key={product.productId}
                  onClick={() => handleProductClick(product)}
                >
                  <Product key={product.productId} product={product} />
                </div>
              ))
            ) : (
              <p>No products found.</p>
            )}
          </div>
        </div>
        <div>
          <hr />
        </div>
        <RecentlyViewedProducts />
      </div>
    </div>
  );
}
