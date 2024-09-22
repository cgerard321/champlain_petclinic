import { useState, useEffect, JSX } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import './ProductList.css';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import Product from './components/Product';
import AddProduct from './components/AddProduct';
import { addProduct } from '@/features/products/api/addProduct';
import { useUser } from '@/context/UserContext';

export default function ProductList(): JSX.Element {
  const [productList, setProductList] = useState<ProductModel[]>([]);
  const [minPrice, setMinPrice] = useState<number | undefined>(undefined);
  const [maxPrice, setMaxPrice] = useState<number | undefined>(undefined);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const { user } = useUser();
  const [isRightRole, setIsRightRole] = useState<boolean>(false);
  const [recentlyClickedProducts, setRecentlyClickedProducts] = useState<ProductModel[]>([]);

  const fetchProducts = async (): Promise<void> => {
    // Validate inputs
    if (
      minPrice !== undefined &&
      maxPrice !== undefined &&
      minPrice > maxPrice
    ) {
      alert('Min Price cannot be greater than Max Price');
      return;
    }
    if (
      (minPrice !== undefined && minPrice < 0) ||
      (maxPrice !== undefined && maxPrice < 0)
    ) {
      alert('Price values cannot be negative');
      return;
    }

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

  const handleProductClick = (product: ProductModel) => {
    setRecentlyClickedProducts(listOfProducts => {
      const updatedProducts = [];

      for (let p of listOfProducts) {
        updatedProducts.push(p);
      }

        updatedProducts.push(product);


      if (updatedProducts.length > 5) {
        updatedProducts.shift();
      }


      return updatedProducts;
    });
  };

  return (
    <div>
      <div className="filter-container">
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
          />
        </label>
        <button onClick={fetchProducts}>Apply Filter</button>
      </div>

      {isRightRole && <AddProduct addProduct={handleAddProduct} />}
      <div className="grid">
        {isLoading ? (
          <p>Loading products...</p>
        ) : productList.length > 0 ? (
          productList.map((product: ProductModel) => (
              <div onClick={() => handleProductClick(product)}>
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
  );
}
