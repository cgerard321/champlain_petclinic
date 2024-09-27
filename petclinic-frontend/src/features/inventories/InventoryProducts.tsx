import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import { ProductModel } from './models/ProductModels/ProductModel';
import './InventoriesListTable.css';
import './InventoryProducts.css';
import useSearchProducts from "@/features/inventories/hooks/useSearchProducts.ts";

const InventoryProducts: React.FC = () => {
  const { inventoryId } = useParams<{ inventoryId: string }>();

  const { productList, setProductList, getProductList } = useSearchProducts();

  // Declare state
  const [productName, setProductName] = useState<string>('');
  const [productDescription, setProductDescription] = useState<string>('');
  const [productStatus, setProductStatus] = useState<string>('');
  const [products, setProducts] = useState<ProductModel[]>([]);
  const [filteredProducts, setFilteredProducts] = useState<ProductModel[]>([]); // State for filtered products
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch products from the backend
  const fetchProducts = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get<ProductModel[]>(
          `http://localhost:8080/api/gateway/inventory/${inventoryId}/products`
      );
      setProducts(response.data);
      setFilteredProducts(response.data); // Initialize filtered products with all products
    } catch (err) {
      setError('Failed to fetch products.');
    } finally {
      setLoading(false);
    }
  };

  // Delete product by productId
  const deleteProduct = async (productId: string): Promise<void> => {
    try {
      await axios.delete(
          `http://localhost:8080/api/gateway/inventory/${inventoryId}/products/${productId}`
      );
      // Filter out the deleted product from both lists
      const updatedProducts = products.filter(product => product.productId !== productId);
      setProducts(updatedProducts);
      setFilteredProducts(updatedProducts); // Update filteredProducts as well
    } catch (err) {
      setError('Failed to delete product.');
    }
  };

  // useEffect with dependency
  useEffect(() => {
    if (inventoryId) {
      fetchProducts().catch(err => console.error(err));
    }
  }, [inventoryId]);

  // Handle filtering
  const handleFilter = () => {
    let filtered = products;

    if (productStatus) {
      filtered = filtered.filter(product => product.status === productStatus);
    }

    setProductList(filtered); // Update productList with filtered products by status

    if (!productName && !productDescription) {
      setFilteredProducts(filtered);
    } else if (productName && !productDescription) {
      getProductList(inventoryId!, productName);
    } else if (!productName && productDescription) {
      getProductList(inventoryId!, undefined, productDescription);
    } else {
      getProductList(inventoryId!, productName, productDescription);
    }
    filtered = productList; // Combine both filtered products from frontend and backend

    setFilteredProducts(filtered);
  };

  // Render loading, error, and product table
  if (loading) return <p>Loading supplies...</p>;
  if (error) return <p>{error}</p>;

  return (
      <div className="inventory-supplies">
        <h2 className="inventory-title">
          Supplies in Inventory: <span>{inventoryId}</span>
        </h2>

        <div className="products-filtering">
          <div className="filter-by-name">
            <label htmlFor="product-name">Filter by Name:</label>
            <input
                type="text"
                id="product-name"
                placeholder="Enter product name"
                onChange={e => setProductName(e.target.value)}
                onKeyUp={e =>
                    e.key === 'Enter' && handleFilter()
                }
            />
          </div>

          <div className="filter-by-description">
            <label htmlFor="product-description">Filter by Description:</label>
            <input
                type="text"
                id="product-description"
                placeholder="Enter product description"
                onChange={e => setProductDescription(e.target.value)}
                onKeyUp={e =>
                    e.key === 'Enter' && handleFilter()
                }
            />
          </div>

          <div className="filter-by-status">
            <label htmlFor="product-status">Filter by Status:</label>
            <select
                id="product-status"
                onChange={e => setProductStatus(e.target.value)}
                onSelect={() => handleFilter()}
            >
              <option value="">All</option>
              <option value="AVAILABLE">Available</option>
              <option value="OUT_OF_STOCK">Out of Stock</option>
              <option value="RE_ORDER">Re-Order</option>
            </select>
          </div>
        </div>

        {/* Product Table */}
        {filteredProducts.length > 0 ? (
            <table className="table table-striped">
              <thead>
              <tr>
                <th>SupplyId</th>
                <th>SupplyName</th>
                <th>Description</th>
                <th>Price</th>
                <th>Quantity</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
              </thead>
              <tbody>
              {filteredProducts.map((product: ProductModel) => (
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
                    <td>
                      <button
                          className="btn btn-danger"
                          onClick={() => deleteProduct(product.productId)}
                      >
                        Delete
                      </button>
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
