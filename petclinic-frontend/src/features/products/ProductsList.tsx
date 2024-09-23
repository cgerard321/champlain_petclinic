import { useState, useEffect, JSX } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import { getProductsByType } from "@/features/products/api/getProductsByType.ts";
import './ProductList.css';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel'; // Ensure this is consistent
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
  const [filterType, setFilterType] = useState<string>(''); // Add state for filter

  const fetchProducts = async (): Promise<void> => {
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
      if (filterType.trim() === '') {
        const list = await getAllProducts(minPrice, maxPrice);
        setProductList(list);
      } else {
        const filteredList = await getProductsByType(filterType);
        // @ts-ignore
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filterType]);

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

  return (
      <div>
        <div className="filter-container">
          <label>
            Min Price:
            <input
                type="number"
                value={minPrice ?? ''}
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
                value={maxPrice ?? ''}
                onChange={e =>
                    setMaxPrice(
                        e.target.value ? parseFloat(e.target.value) : undefined
                    )
                }
            />
          </label>
          <label>
            Product Type: {/* Added input for product type filtering */}
            <input
                type="text"
                placeholder="Enter product type"
                value={filterType}
                onChange={e => setFilterType(e.target.value)}
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
                  <Product key={product.productId} product={product} />
              ))
          ) : (
              <p>No products found.</p>
          )}
        </div>
      </div>
  );
}
