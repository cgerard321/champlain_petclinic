import { useState, useEffect, JSX } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import './ProductList.css';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import Product from './components/Product';
import AddProduct from './components/AddProduct';
import { addProduct } from '@/features/products/api/addProduct';
import { deleteProduct } from '@/features/products/api/deleteProduct'; // Import deleteProduct
import { useUser } from '@/context/UserContext';
import './components/Sidebar.css';
import { getProductsByType } from '@/features/products/api/getProductsByType.ts';
import { addImage } from './api/addImage';
import { ImageModel } from './models/ProductModels/ImageModel';
import StarRating from '@/features/products/components/StarRating.tsx';
import './components/StarRating.css';

export default function ProductList(): JSX.Element {
  const [productList, setProductList] = useState<ProductModel[]>([]);
  const [minPrice, setMinPrice] = useState<number | undefined>(undefined);
  const [maxPrice, setMaxPrice] = useState<number | undefined>(undefined);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const { user } = useUser();
  const [isRightRole, setIsRightRole] = useState<boolean>(false);
  const [isSidebarOpen, setIsSidebarOpen] = useState<boolean>(false);
  const [filterType, setFilterType] = useState<string>('');
  const [recentlyClickedProducts, setRecentlyClickedProducts] = useState<ProductModel[]>([]);
  const [ratingSort, setRatingSort] = useState<string>('default');
  const [minStars, setMinStars] = useState<number>(0);
  const [maxStars, setMaxStars] = useState<number>(5);

  function FilterByPriceErrorHandling(): void {
    if (minPrice !== undefined && maxPrice !== undefined && minPrice > maxPrice) {
      alert('Min Price cannot be greater than Max Price');
      return;
    }
  }

  const fetchProducts = async (): Promise<void> => {
    FilterByPriceErrorHandling();
    setIsLoading(true);
    try {
      if (filterType.trim() === '') {
        const list = await getAllProducts(minPrice, maxPrice, minStars, maxStars, ratingSort);
        setProductList(list);
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
  };

  useEffect(() => {
    fetchProducts();
    const savedProducts = localStorage.getItem('recentlyClickedProducts');
    if (savedProducts) {
      return setRecentlyClickedProducts(JSON.parse(savedProducts));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    const hasRightRole =
      user?.roles !== undefined &&
      Array.from(user.roles).some(
        role => role.name === 'ADMIN' 
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

  const handleAddProduct = async (product: ProductModel): Promise<ProductModel> => {
    try {
      const savedProduct = await addProduct(product);
      await fetchProducts();
      return savedProduct;
    } catch (error) {
      throw error;
    }
  };

  const handleDeleteProduct = async (productId: string): Promise<void> => {
    try {
      await deleteProduct(productId); // Call deleteProduct
      setProductList(prevProducts => prevProducts.filter(product => product.productId !== productId));
    } catch (error) {
      console.error('Failed to delete product:', error);
    }
  };

  const toggleSidebar = (): void => {
    setIsSidebarOpen(!isSidebarOpen);
  };

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

      localStorage.setItem('recentlyClickedProducts', JSON.stringify(updatedProducts));

      return updatedProducts;
    });
  };

  return (
    <div className="product-list-container">
      {isSidebarOpen && <div className="overlay" onClick={handleOverlayClick}></div>}

      <div className={`sidebar ${isSidebarOpen ? 'open' : ''}`} id="sidebar" aria-hidden={!isSidebarOpen}>
        <button className="close-button" onClick={toggleSidebar} aria-label="Close Filters">
          &times;
        </button>
        <div className="filter-container">
          <h2>Filters</h2>
          <label>
            Min Price:
            <input
              type="number"
              value={minPrice ?? typeof 'number'}
              onChange={e => setMinPrice(e.target.value ? parseFloat(e.target.value) : undefined)}
              min="0"
              placeholder="e.g., 10"
            />
          </label>
          <label>
            Max Price:
            <input
              type="number"
              value={maxPrice ?? typeof 'number'}
              onChange={e => setMaxPrice(e.target.value ? parseFloat(e.target.value) : undefined)}
              min="0"
              placeholder="e.g., 100"
            />
          </label>
          <label>
            Product Type:
            <input
              type="text"
              placeholder="Enter product type"
              value={filterType}
              onChange={e => setFilterType(e.target.value)}
            />
          </label>
          <div className="star-rating-container">
            <h2>Filter by Star Rating</h2>
            <div className="star-row">
              <label>Min Stars:</label>
              <StarRating currentRating={minStars} viewOnly={false} updateRating={setMinStars} />
            </div>
            <div className="star-row">
              <label>Max Stars:</label>
              <StarRating currentRating={maxStars} viewOnly={false} updateRating={setMaxStars} />
            </div>
          </div>
          <select name="rating" value={ratingSort} onChange={e => setRatingSort(e.target.value)}>
            <option value="default">Sort by Rating</option>
            <option value="asc">Low to High</option>
            <option value="desc">High to Low</option>
          </select>
          <button className="apply-filter-button" onClick={fetchProducts}>
            Apply
          </button>
          <button className="clear-filter-button" onClick={clearFilters}>
            Clear
          </button>
        </div>
      </div>

      {!isSidebarOpen && (
        <button className="toggle-sidebar-button" onClick={toggleSidebar} aria-expanded={isSidebarOpen} aria-controls="sidebar">
          &#9776; Filters
        </button>
      )}

      {isRightRole && (
        <AddProduct addProduct={handleAddProduct} addImage={handleAddImage} />
      )}

      <div className="main-content">
        <div className="grid">
          {isLoading ? (
            <p>Loading products...</p>
          ) : productList.length > 0 ? (
            productList.map((product: ProductModel) => (
              <div key={product.productId} onClick={() => handleProductClick(product)}>
                <Product key={product.productId} product={product} />
                {isRightRole && (
                  <button onClick={() => handleDeleteProduct(product.productId)} style={{ color: 'red' }}>
                    Delete
                  </button>
                )}
              </div>
            ))
          ) : (
            <p>No products found.</p>
          )}
        </div>
        <div>
          <hr />
        </div>
        <div>
          <h2>Recently Clicked Products</h2>
          <div className="grid">
            {recentlyClickedProducts.map(product => (
              <Product key={product.productId} product={product} />
            ))}
          </div>
        </div>
      </div>
</div>
  );
}
