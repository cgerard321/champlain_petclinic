import { useState, useEffect, JSX } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import './ProductList.css';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import Product from './components/Product';

export default function ProductList(): JSX.Element {
  const [productList, setProductList] = useState<ProductModel[]>([]);
  const [minPrice, setMinPrice] = useState<number | undefined>(undefined);
  const [maxPrice, setMaxPrice] = useState<number | undefined>(undefined);
  const [isLoading, setIsLoading] = useState<boolean>(false);

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

      <div className="grid">
        {isLoading ? (
          <p>Loading products...</p>
        ) : productList.length > 0 ? (
          productList.map((product: ProductModel) => (
            <Product key={product.productId} product={product} />
          ))
        ) : (
          <p>No products found.</p>
        )}
      </div>
    </div>
  );
}
