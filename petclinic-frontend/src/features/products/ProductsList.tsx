import { useState, useEffect, JSX } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import './ProductList.css';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import Product from './components/Product';
import AddProduct from './components/AddProduct';
import { addProduct } from '@/features/products/api/addProduct';
import { useUser } from '@/context/UserContext';
import './components/Sidebar.css';

export default function ProductList(): JSX.Element {
  const [productList, setProductList] = useState<ProductModel[]>([]);
  const [minPrice, setMinPrice] = useState<number | undefined>(undefined);
  const [maxPrice, setMaxPrice] = useState<number | undefined>(undefined);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const { user } = useUser();
  const [isRightRole, setIsRightRole] = useState<boolean>(false);
  const [isSidebarOpen, setIsSidebarOpen] = useState<boolean>(false);
  const [recentlyClickedProducts, setRecentlyClickedProducts] = useState<
    ProductModel[]
  >([]);

  function FilterByPriceErrorHandling(): void {
    // Validate inputs for filter by price
    if (
      minPrice !== undefined &&
      maxPrice !== undefined &&
      minPrice > maxPrice
    ) {
      alert('Min Price cannot be greater than Max Price');
      return;
    }
  }

  const fetchProducts = async (): Promise<void> => {
    FilterByPriceErrorHandling();
    setIsLoading(true);
    try {
      const list = await getAllProducts(minPrice, maxPrice);
      setProductList(list);
    } catch (err) {
      console.error('Error fetching products:', err);
      setProductList([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
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

  const handleAddProduct = async (
    product: Omit<ProductModel, 'productId'>
  ): Promise<void> => {
    try {
      await addProduct(product);
      await fetchProducts();
    } catch (error) {
      console.error('Error adding product:', error);
    }
  };

  const toggleSidebar = (): void => {
    setIsSidebarOpen(!isSidebarOpen);
  };

  // Handler to close sidebar when clicking outside
  const handleOverlayClick = (): void => {
    setIsSidebarOpen(false);
  };

  const clearFilters = (): void => {
    setMinPrice(undefined);
    setMaxPrice(undefined);
  };

  const handleProductClick = (product: ProductModel): void => {
    setRecentlyClickedProducts(listOfProducts => {
      const updatedProducts = [];

      for (const p of listOfProducts) {
        if (p.productId !== product.productId) {
          updatedProducts.push(p);
        }
      }

      updatedProducts.push(product);

      if (updatedProducts.length > 5) {
        updatedProducts.shift();
      }

      return updatedProducts;
    });
  };

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
          <button className="apply-filter-button" onClick={fetchProducts}>
            Apply Filter
          </button>
          <button className="clear-filter-button" onClick={clearFilters}>
            Clear Filters
          </button>
        </div>
      </div>

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

      {isRightRole && <AddProduct addProduct={handleAddProduct} />}
      <div className="main-content">
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
        <div>
          <h2>Recently Clicked Products</h2>
          <div className="grid">
            {recentlyClickedProducts.map(product => (
              <div className="card" key={product.productId}>
                <h2>{product.productName}</h2>
                <p>{product.productDescription}</p>
                <p>Price: ${product.productSalePrice.toFixed(2)}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
