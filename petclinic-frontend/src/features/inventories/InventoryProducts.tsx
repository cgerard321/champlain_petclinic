import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { ProductModel } from './models/ProductModels/ProductModel';
import './InventoriesListTable.css';
import './InventoryProducts.css';

const InventoryProducts: React.FC = () => {
  const { inventoryId } = useParams<{ inventoryId: string }>();

  const [products, setProducts] = useState<ProductModel[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchProducts = async (): Promise<void> => {
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
  };

  useEffect(() => {
    if (inventoryId) {
      fetchProducts().catch(err => console.error(err));
    }
  }, [inventoryId]);

  if (loading) return <p>Loading products...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className="inventory-products">
      <h2>Products in Inventory: {inventoryId}</h2>
      {products.length > 0 ? (
        <table className="table table-striped">
          <thead>
            <tr>
              <th>Product ID</th>
              <th>Product Name</th>
              <th>Description</th>
              <th>Price</th>
              <th>Quantity</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product: ProductModel) => {
              let statusClass = '';

              switch (product.status) {
                case 'RE_ORDER':
                  statusClass = 'status-reorder';
                  break;
                case 'OUT_OF_STOCK':
                  statusClass = 'status-out-of-stock';
                  break;
                case 'AVAILABLE':
                  statusClass = 'status-available';
                  break;
                default:
                  statusClass = '';
              }

              return (
                <tr key={product.productId}>
                  <td>{product.productId}</td>
                  <td>{product.productName}</td>
                  <td>{product.productDescription}</td>
                  <td>${product.productSalePrice}</td>
                  <td>{product.productQuantity}</td>
                  <td className={statusClass}>{product.status}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      ) : (
        <p>No products found for this inventory.</p>
      )}
    </div>
  );
};

export default InventoryProducts;
