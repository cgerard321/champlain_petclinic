import { useState, useEffect, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { ProductModel } from './models/ProductModels/ProductModel';
import './InventoriesListTable.css';
import './InventoryProducts.css';
import { Status } from './models/ProductModels/Status';

const InventoryProducts: React.FC = () => {
  const { inventoryId } = useParams<{ inventoryId: string }>();

  // Declare state for products, loading, error, and search inputs
  const [products, setProducts] = useState<ProductModel[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [searchName, setSearchName] = useState<string>('');
  const [searchDescription, setSearchDescription] = useState<string>('');
  const [searchStatus, setSearchStatus] = useState<Status | ''>('');

  // Fetch products from the backend using search filters
  const fetchProducts = useCallback(async (): Promise<void> => {
    setLoading(true);
    setError(null);

    try {
      const response = await axios.get<ProductModel[]>(
          `http://localhost:8080/api/gateway/${inventoryId}/products`,
          {
            params: {
              productName: searchName || undefined,
              productDescription: searchDescription || undefined,
              status: searchStatus || undefined,
            },
          }
      );
      setProducts(response.data);
    } catch (err) {
      setError('Failed to fetch products.');
    } finally {
      setLoading(false);
    }
  }, [inventoryId, searchName, searchDescription, searchStatus]);

  // useEffect to fetch products on mount or when search criteria change
  useEffect(() => {
    if (inventoryId) {
      fetchProducts().catch(err => console.error(err));
    }
  }, [inventoryId, fetchProducts]);

  // Handle search by clicking the button or pressing enter
  const handleSearch = () => {
    fetchProducts().then(r => console.log(r));
  };

  const handleKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      handleSearch();
    }
  };

  // Render loading, error, and product table
  if (loading) return <p>Loading supplies...</p>;
  if (error) return <p>{error}</p>;

  return (
      <div className="inventory-supplies">
        <h2 className="inventory-title">
          Supplies in Inventory: <span>{inventoryId}</span>
        </h2>

        {/* Search Inputs */}
        <div className="search-container">
          <input
              type="text"
              placeholder="Search by Product Name"
              value={searchName}
              onChange={(e) => setSearchName(e.target.value)}
              onKeyDown={handleKeyDown}
          />
          <input
              type="text"
              placeholder="Search by Description"
              value={searchDescription}
              onChange={(e) => setSearchDescription(e.target.value)}
              onKeyDown={handleKeyDown}
          />
          <select
              value={searchStatus}
              onChange={(e) => setSearchStatus(e.target.value as Status)}
          >
            <option value="">All Status</option>
            <option value="RE_ORDER">Re-order</option>
            <option value="OUT_OF_STOCK">Out of Stock</option>
            <option value="AVAILABLE">Available</option>
          </select>
          <button onClick={handleSearch}>Search</button>
        </div>

        {/* Product Table */}
        {products.length > 0 ? (
            <table className="table table-striped">
              <thead>
              <tr>
                <th>SupplyId</th>
                <th>SupplyName</th>
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
                                  ? '#f4a460'
                                  : product.status === 'OUT_OF_STOCK'
                                      ? 'red'
                                      : product.status === 'AVAILABLE'
                                          ? 'green'
                                          : 'inherit',
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
