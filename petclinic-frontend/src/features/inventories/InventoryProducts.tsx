import { useState, useEffect, useCallback } from 'react';
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
  }, [inventoryId]);

  useEffect(() => {
    if (inventoryId) {
      fetchProducts().catch(err => console.error(err));
    }
  }, [inventoryId, fetchProducts]);

  if (loading) return <p>Loading products...</p>;
  if (error) return <p>{error}</p>;

   return (
    <div className="inventory-products">
      <h2>
        {products.length > 0 ? (
          <>Products in Inventory: {inventoryId}</>
        ) : (
          <span className="inventory-title">
            Supplies in Inventory: <span>{inventoryId}</span>
          </span>
        )}
      </h2>
       
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
            {products.map((product: ProductModel) => (
              <tr key={product.productId}>
                <td>{product.productId}</td>
                <td>{product.productName}</td>
                <td>{product.productDescription}</td>
                <td>${product.productSalePrice}</td>
                <td>{product.productQuantity}</td>
                <td
                  style={{
                    color:
                      product.status === 'RE_ORDER'
                        ? '#f4a460' // Tan for RE_ORDER
                        : product.status === 'OUT_OF_STOCK'
                          ? 'red' // Red for OUT_OF_STOCK
                          : product.status === 'AVAILABLE'
                            ? 'green' // Green for AVAILABLE
                            : 'inherit', // Default color
                  }}
                >
                  {product.status.replace('_', ' ')}
                </td>
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
