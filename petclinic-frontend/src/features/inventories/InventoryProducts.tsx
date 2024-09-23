import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { ProductModel } from './models/ProductModels/ProductModel';
import './InventoriesListTable.css';
import './InventoryProducts.css';

const InventoryProducts: React.FC = () => {
  const { inventoryId } = useParams<{ inventoryId: string }>();

  // Declare state
  const [products, setProducts] = useState<ProductModel[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Declare filter state
  const [productName, setProductName] = useState<string>('');
  const [productDescription, setProductDescription] = useState<string>('');
  const [status, setStatus] = useState<string>('');

  // Track whether a search has been made
  const [isSearchActive, setIsSearchActive] = useState<boolean>(false);

  // Fetch products from the backend
  // Fetch products with or without filters
  const fetchProducts = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const url = isSearchActive
          ? `http://localhost:8080/api/gateway/inventory/${inventoryId}/products/search`
          : `http://localhost:8080/api/gateway/inventory/${inventoryId}/products`;

      const params = isSearchActive
          ? {
            productName: productName || undefined, // Only send if not empty
            productDescription: productDescription || undefined,
            status: status || undefined,
          }
          : undefined;

      const response = await axios.get<ProductModel[]>(url, { params });
      setProducts(response.data);
    } catch (err) {
      setError('Failed to fetch products.');
    } finally {
      setLoading(false);
    }
  };

  // useEffect with dependency
  useEffect(() => {
    if (inventoryId) {
      fetchProducts().catch(err => console.error(err));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [inventoryId]);

  // Handle form submission for search
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault(); // Prevent form reload
    setIsSearchActive(true); // Mark that search is active
    fetchProducts(); // Fetch products with search filters
  };

  // Render loading, error, and product table
  if (loading) return <p>Loading supplies...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className="inventory-supplies">
      <h2 className="inventory-title">
        Supplies in Inventory: <span>{inventoryId}</span>
      </h2>

      <form onSubmit={handleSearch}>
        <div>
          <label htmlFor="productName">Product Name: </label>
          <input
              id="productName"
              type="text"
              value={productName}
              onChange={(e) => setProductName(e.target.value)}
          />
        </div>

        <div>
          <label htmlFor="productDescription">Product Description: </label>
          <input
              id="productDescription"
              type="text"
              value={productDescription}
              onChange={(e) => setProductDescription(e.target.value)}
          />
        </div>

        <div>
          <label htmlFor="status">Status: </label>
          <select
              id="status"
              value={status}
              onChange={(e) => setStatus(e.target.value)}
          >
            <option value="">All</option>
            <option value="AVAILABLE">Available</option>
            <option value="RE_ORDER">Re-order</option>
            <option value="OUT_OF_STOCK">Out of Stock</option>
          </select>
        </div>

        <button type="submit">Search</button>
      </form>

      {/* Product Table */}
      {products.length > 0 ? (
        <table className="table table-striped">
          <thead>
            <tr>
              <th>SupplyName</th>
              <th>Description</th>
              <th>Quantity</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {products.map((product: ProductModel) => (
              <tr key={product.productId}>
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
        <p>No supplies found for this inventory.</p>
      )}
    </div>
  );
};

export default InventoryProducts;
