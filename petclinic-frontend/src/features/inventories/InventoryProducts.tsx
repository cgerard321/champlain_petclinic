import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import { ProductModel } from './models/ProductModels/ProductModel';
import './InventoriesListTable.module.css';
import './InventoryProducts.css';
import useSearchProducts from '@/features/inventories/hooks/useSearchProducts.ts';
import deleteAllProductsFromInventory from './api/deleteAllProductsFromInventory';
import createPdf from './api/createPdf';

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
  const navigate = useNavigate();

  const handleCreatePdf = async (): Promise<void> => {
    if (inventoryId) {
      try {
        await createPdf(inventoryId);
      } catch (error) {
        console.error('Failed to create PDF', error);
      }
    }
  };

  // Fetch products from the backend
  useEffect(() => {
    const fetchProducts = async (): Promise<void> => {
      setLoading(true);
      setError(null);
      try {
        const response = await axios.get<ProductModel[]>(
          `http://localhost:8080/api/v2/gateway/inventories/${inventoryId}/products/search`
        );
        setProducts(response.data);
        setProductList(response.data); // Set productList as well
        setFilteredProducts(response.data); // Initialize filtered products with all products
      } catch (err) {
        setError('Failed to fetch products.');
      } finally {
        setLoading(false);
      }
    };

    if (inventoryId) {
      fetchProducts().catch(err => console.error(err));
    }
  }, [inventoryId, setProductList]); // Add 'setProductList' to the dependency array

  // Delete product by productId
  const deleteProduct = async (productId: string): Promise<void> => {
    try {
      await axios.delete(
        `http://localhost:8080/api/v2/gateway/inventories/${inventoryId}/products/${productId}`
      );
      // Filter out the deleted product from both lists
      const updatedProducts = products.filter(
        product => product.productId !== productId
      );
      setProducts(updatedProducts);
      setFilteredProducts(updatedProducts); // Update filteredProducts as well
    } catch (err) {
      setError('Failed to delete product.');
    }
  };

  const handleDeleteAllProducts = async (): Promise<void> => {
    try {
      // Call the deleteAllProductsFromInventory function
      await deleteAllProductsFromInventory({ inventoryId: inventoryId! });
      // Clear the product lists after successful deletion
      setProducts([]);
      setFilteredProducts([]);
      alert('All products deleted successfully.');
    } catch (err) {
      setError('Failed to delete all products.');
    }
  };

  const handleFilter = async (): Promise<void> => {
    // Apply status filtering on the frontend first
    let filtered = products;

    if (productStatus) {
      filtered = filtered.filter(product => product.status === productStatus);
    }

    // Call the backend only if name or description is provided
    if (productName || productDescription) {
      await getProductList(
        inventoryId!,
        productName || undefined,
        productDescription || undefined
      );
    }

    // Set filteredProducts when productList changes (after the backend call)
    // Trigger a state change here in case productList is updated asynchronously
    setFilteredProducts(filtered); // Apply status filter immediately
  };

  const reduceQuantity = async (
    productId: string,
    currentQuantity: number
  ): Promise<void> => {
    if (currentQuantity > 0) {
      try {
        // Calculate the updated quantity
        const updatedQuantity = currentQuantity - 1;

        // Send the PATCH request to update the quantity in the backend
        await axios.patch(
          `http://localhost:8080/api/gateway/inventory/${inventoryId}/products/${productId}/consume`,
          {
            productQuantity: updatedQuantity,
          }
        );

        // Determine the new status based on the updated quantity
        let updatedStatus: 'RE_ORDER' | 'OUT_OF_STOCK' | 'AVAILABLE' =
          'AVAILABLE';
        if (updatedQuantity === 0) {
          updatedStatus = 'OUT_OF_STOCK';
        } else if (updatedQuantity <= 20) {
          updatedStatus = 'RE_ORDER';
        }

        // Update the product list in the frontend
        const updatedProducts = filteredProducts.map(product =>
          product.productId === productId
            ? {
                ...product,
                productQuantity: updatedQuantity,
                status: updatedStatus, // Update status
              }
            : product
        );

        setProducts(updatedProducts);
        setFilteredProducts(updatedProducts); // Update the filtered list if needed
      } catch (err) {
        setError('Failed to reduce product quantity.');
      }
    }
  };

  // UseEffect to monitor changes in productList and apply filtering
  useEffect(() => {
    // Apply frontend status filtering on the updated productList from the backend
    if (productList) {
      setFilteredProducts(
        productList.filter(
          product => !productStatus || product.status === productStatus
        )
      );
    }
  }, [productList, productStatus]); // Trigger this effect when productList or productStatus changes

  // Render loading, error, and product table
  if (loading) return <p>Loading supplies...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className="inventory-supplies">
      <h2 className="inventory-title">
        Supplies in Inventory: <span>{inventoryId}</span>
      </h2>

      <button
        className="btn btn-secondary"
        onClick={() => navigate('/inventories')}
      >
        Back
      </button>
      <button className="btn btn-primary" onClick={handleCreatePdf}>
        Download PDF
      </button>

      <div className="products-filtering">
        <div className="filter-by-name">
          <label htmlFor="product-name">Filter by Name:</label>
          <input
            type="text"
            id="product-name"
            placeholder="Enter product name"
            onChange={e => setProductName(e.target.value)}
            onKeyUp={e => e.key === 'Enter' && handleFilter()}
          />
        </div>

        <div className="filter-by-description">
          <label htmlFor="product-description">Filter by Description:</label>
          <input
            type="text"
            id="product-description"
            placeholder="Enter product description"
            onChange={e => setProductDescription(e.target.value)}
            onKeyUp={e => e.key === 'Enter' && handleFilter()}
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
              <th colSpan={4}>Actions</th>
            </tr>
          </thead>
          <tbody>
            {filteredProducts.map((product: ProductModel) => (
              <tr key={product.productName}>
                <td>{product.productName}</td>
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
                    onClick={e => {
                      e.stopPropagation();
                      navigate(`${product.productId}/edit`);
                    }}
                    className="btn btn-warning"
                  >
                    Edit
                  </button>
                </td>
                <td>
                  <button
                    className="btn btn-danger"
                    onClick={() => deleteProduct(product.productId)}
                  >
                    Delete
                  </button>
                </td>
                <td>
                  <button
                    className="btn btn-primary"
                    onClick={() =>
                      reduceQuantity(product.productId, product.productQuantity)
                    }
                    disabled={product.productQuantity <= 0} // Disable if no more products
                  >
                    Consume
                  </button>
                </td>
                <td>
                  <button
                    onClick={e => {
                      e.stopPropagation();
                      navigate(`${product.productId}/move`);
                    }}
                    className="btn btn-info"
                  >
                    Move
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <p>No supplies found for this inventory.</p>
      )}
      <button
        className="btn btn-add"
        onClick={() => navigate(`/inventory/${inventoryId}/products/add`)}
      >
        Add
      </button>
      <button className="btn btn-danger" onClick={handleDeleteAllProducts}>
        Delete All Products
      </button>
    </div>
  );
};

export default InventoryProducts;
