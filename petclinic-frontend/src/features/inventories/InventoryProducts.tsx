import { useCallback, useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { ProductModel } from './models/ProductModels/ProductModel';
import './InventoriesListTable.css';

const InventoryProducts: React.FC = () => {
  const { inventoryId } = useParams<{ inventoryId: string }>();

  // Declare state
  const [products, setProducts] = useState<ProductModel[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch products from the backend with useCallback
  const fetchProducts = useCallback(async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get<ProductModel[]>(
        `http://localhost:8080/api/gateway/inventory/${inventoryId}/products`
      );
      setProducts(response.data);
    } catch (err) {
      setError('Failed to fetch products.');
    } finally {
      setLoading(false);
    }
  }, [inventoryId]); // Add inventoryId as a dependency here

  // useEffect with dependency
  useEffect(() => {
    if (inventoryId) {
      fetchProducts().catch(err => console.error(err));
    }
  }, [inventoryId, fetchProducts]); // Add fetchProducts here as a dependency

  // Render loading, error, and product table
  if (loading) return <p>Loading products...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className="inventory-products">
      <h2>Products in Inventory: {inventoryId}</h2>

      {/* Product Table */}
      {products.length > 0 ? (
        <table className="table table-striped">
          <thead>
            <tr>
              <th>Product ID</th>
              <th>Product Name</th>
              <th>Description</th>
              <th>Price</th>
              <th>Quantity</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product: ProductModel) => (
              <tr key={product.productId}>
                <td>{product.productId}</td>
                <td>{product.productName}</td>
                <td>{product.productDescription}</td>
                <td>${product.productSalePrice}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <p>No products found for this inventory.</p>
      )}
    </div>
  );
};

export default InventoryProducts;
