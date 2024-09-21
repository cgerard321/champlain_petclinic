import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { ProductModel } from './models/ProductModels/ProductModel';
import './InventoriesListTable.css';
import './InventoryProducts.css';

const InventoryProducts: React.FC = () => {
  const { inventoryId } = useParams<{ inventoryId: string }>();

  const [productStatus, setProductStatus] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // State for products list and filtered products list
  const [productList, setProductList] = useState<ProductModel[]>([]);
  const [filteredProducts, setFilteredProducts] = useState<ProductModel[]>([]);

  // Fetch all products for the inventory
  const fetchProducts = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get<ProductModel[]>(
          `http://localhost:8080/api/gateway/inventory/${inventoryId}/products`
      );
      setProductList(response.data);  // Store all products
      setFilteredProducts(response.data);  // Initially, no filtering applied
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

  // Function to filter the products based on the search criteria
  const filterProducts = (): void => {
    let filtered = productList;

    if (productStatus) {
      filtered = filtered.filter(product => product.status === productStatus);
    }

    setFilteredProducts(filtered);  // Update the filtered products state
  };

  if (loading) return <p>Loading supplies...</p>;
  if (error) return <p>{error}</p>;

  return (
      <div className="inventory-supplies">
        <h2>Supplies in Inventory: {inventoryId}</h2>

        <table className="table table-striped">
          <thead>
          <tr>
            <th>SupplyId</th>
            <th>SupplyName</th>
            <th>Description</th>
            <th>Quantity</th>
            <th>Status</th>
          </tr>
          <tr>
            <td></td> {/* Empty cell for SupplyId */}
            <td>
              <input
                  type="text"
                  className="form-control"
                  placeholder="Search by supply name"
                  // Will have name filtering logic here
                  onKeyUp={e => e.key === 'Enter' && filterProducts()} // Filter when Enter is pressed
              />
            </td>
            <td>
              <input
                  type="text"
                  className="form-control"
                  placeholder="Search by description"
                  // Will have description filtering logic here
                  onKeyUp={e => e.key === 'Enter' && filterProducts()} // Filter when Enter is pressed
              />
            </td>
            <td></td> {/* Empty cell for Quantity */}
            <td>
              <select
                  className="form-control"
                  value={productStatus}
                  onChange={e => setProductStatus(e.target.value)}
                  onSelect={filterProducts} // Filter when selected
              >
                <option value="">All</option>
                <option value="RE_ORDER">Re-Order</option>
                <option value="OUT_OF_STOCK">Out of Stock</option>
                <option value="AVAILABLE">Available</option>
              </select>
            </td>
            <td>
              <button
                  className="btn btn-success"
                  onClick={filterProducts} // Filter on button click
              >
                Search
              </button>
            </td>
          </tr>
          </thead>
          <tbody>
          {filteredProducts.length > 0 ? (
              filteredProducts.map((product: ProductModel) => {
                return (
                    <tr key={product.productId}>
                      <td>{product.productId}</td>
                      <td>{product.productName}</td>
                      <td>{product.productDescription}</td>
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
                );
              })
          ) : (
              <p>No supplies found.</p>
          )}
          </tbody>
        </table>
      </div>
  );
};

export default InventoryProducts;
