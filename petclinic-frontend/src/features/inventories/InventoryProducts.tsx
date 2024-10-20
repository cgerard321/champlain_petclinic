import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import { ProductModel } from './models/ProductModels/ProductModel';
import './InventoriesListTable.module.css';
import './InventoryProducts.css';
import useSearchProducts from '@/features/inventories/hooks/useSearchProducts.ts';
import deleteAllProductsFromInventory from './api/deleteAllProductsFromInventory';
import createPdf from './api/createPdf';
import ConfirmationModal from '@/features/inventories/ConfirmationModal.tsx';
import { Status } from '@/features/inventories/models/ProductModels/Status.ts';

const InventoryProducts: React.FC = () => {
  const { inventoryId } = useParams<{ inventoryId: string }>();
  const { productList, setProductList, getProductList } = useSearchProducts();

  // Declare state
  const [productName, setProductName] = useState<string>('');
  const [productDescription, setProductDescription] = useState<string>('');
  const [productStatus, setProductStatus] = useState<Status>(Status.AVAILABLE);
  const [products, setProducts] = useState<ProductModel[]>([]);
  const [filteredProducts, setFilteredProducts] = useState<ProductModel[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [showConfirmation, setShowConfirmation] = useState<boolean>(false);
  const [productToDelete, setProductToDelete] = useState<string | null>(null);
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

  const googleTranslateElementInit = (): void => {
    new window.google.translate.TranslateElement(
      {
        pageLanguage: 'en',
        autoDisplay: false,
        includedLanguages: 'en,fr,de,es', // Specify the languages you want to include
      },
      'google_translate_element'
    );
  };

  useEffect(() => {
    const addScript = document.createElement('script');
    addScript.setAttribute(
      'src',
      '//translate.google.com/translate_a/element.js?cb=googleTranslateElementInit'
    );
    document.body.appendChild(addScript);
    window.googleTranslateElementInit = googleTranslateElementInit;
  }, []);

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
      } finally {
        setLoading(false);
      }
    };

    if (inventoryId) {
      fetchProducts().catch(err => console.error(err));
    }
  }, [inventoryId, setProductList]);

  // Delete product by productId
  const deleteProduct = async (): Promise<void> => {
    if (productToDelete) {
      try {
        await axios.delete(
          `http://localhost:8080/api/v2/gateway/inventories/${inventoryId}/products/${productToDelete}`
        );
        const updatedProducts = products.filter(
          product => product.productId !== productToDelete
        );
        setProducts(updatedProducts);
        setFilteredProducts(updatedProducts);
      } catch (err) {
        setError('Failed to delete product.');
      } finally {
        setShowConfirmation(false);
        setProductToDelete(null);
      }
    }
  };

  const handleDeleteAllProducts = async (): Promise<void> => {
    try {
      await deleteAllProductsFromInventory({ inventoryId: inventoryId! });
      setProducts([]);
      setFilteredProducts([]);
      alert('All products deleted successfully.');
    } catch (err) {
      setError('Failed to delete all products.');
    }
  };

  const handleFilter = async (): Promise<void> => {
    // Ensure that `productList` is populated with products matching the criteria
    if (productName || productDescription || productStatus) {
      await getProductList(
        inventoryId!,
        productName || undefined,
        productDescription || undefined,
        productStatus || undefined
      );
    }

    // Apply additional client-side filtering if necessary
    const filtered = products.filter(product => {
      const matchesName = productName
        ? product.productName.toLowerCase().includes(productName.toLowerCase())
        : true;
      const matchesDescription = productDescription
        ? product.productDescription
            .toLowerCase()
            .includes(productDescription.toLowerCase())
        : true;
      const matchesStatus = productStatus
        ? product.status === productStatus
        : true;

      return matchesName && matchesDescription && matchesStatus;
    });

    setFilteredProducts(filtered);
  };

  const handleDeleteClick = (productId: string): void => {
    setProductToDelete(productId);
    setShowConfirmation(true);
  };

  const cancelDelete = (): void => {
    setShowConfirmation(false);
    setProductToDelete(null);
  };

  const reduceQuantity = async (
    productId: string,
    currentQuantity: number
  ): Promise<void> => {
    if (currentQuantity > 0) {
      try {
        const updatedQuantity = currentQuantity - 1;
        await axios.patch(
          `http://localhost:8080/api/gateway/inventory/${inventoryId}/products/${productId}/consume`,
          {
            productQuantity: updatedQuantity,
          }
        );

        let updatedStatus: Status = Status.AVAILABLE;
        if (updatedQuantity === 0) {
          updatedStatus = Status.OUT_OF_STOCK;
        } else if (updatedQuantity <= 20) {
          updatedStatus = Status.RE_ORDER;
        }

        const updatedProducts = filteredProducts.map(product =>
          product.productId === productId
            ? {
                ...product,
                productQuantity: updatedQuantity,
                status: updatedStatus,
              }
            : product
        );

        setProducts(updatedProducts);
        setFilteredProducts(updatedProducts);
      } catch (err) {
        setError('Failed to reduce product quantity.');
      }
    }
  };

  useEffect(() => {
    if (productList) {
      setFilteredProducts(
        productList.filter(
          product => !productStatus || product.status === productStatus
        )
      );
    }
  }, [productList, productStatus]);

  if (loading) return <p>Loading supplies...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className="inventory-supplies">
      <h2 className="inventory-title">
        Supplies in Inventory: <span>{inventoryId}</span>
      </h2>
      <button
        className="btn btn-secondary"
        onClick={() =>
          navigate('/inventories', {
            state: { lastConsultedInventoryId: inventoryId },
          })
        }
      >
        Go Back
      </button>
      <div id="google_translate_element"></div> {/* Translate element */}
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
            onChange={e => setProductStatus(e.target.value as Status)}
          >
            <option value="">All</option>
            {Object.values(Status).map(status => (
              <option key={status} value={status}>
                {status
                  .replace('_', ' ')
                  .toLowerCase()
                  .replace(/\b\w/g, c => c.toUpperCase())}
              </option>
            ))}
          </select>
        </div>
      </div>
      {/* Always render the table structure */}
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
          {filteredProducts.length > 0 ? (
            filteredProducts.map((product: ProductModel) => (
              <tr key={product.productId}>
                <td>{product.productId}</td>
                <td>{product.productName}</td>
                <td>{product.productDescription}</td>
                <td>${product.productSalePrice}</td>
                <td>{product.productQuantity}</td>
                <td
                  style={{
                    color:
                      product.status === Status.RE_ORDER
                        ? '#f4a460'
                        : product.status === Status.OUT_OF_STOCK
                          ? 'red'
                          : product.status === Status.AVAILABLE
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
                    onClick={() => handleDeleteClick(product.productId)}
                  >
                    Delete
                  </button>
                </td>
                <td>
                  <button
                    className="btn btn-info"
                    onClick={() =>
                      reduceQuantity(product.productId, product.productQuantity)
                    }
                  >
                    Reduce Quantity
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
            ))
          ) : (
            <tr>
              <td colSpan={10} style={{ textAlign: 'center' }}>
                No products available.
              </td>
            </tr>
          )}
        </tbody>
      </table>
      <button
        className="btn btn-add"
        onClick={() => navigate(`/inventory/${inventoryId}/products/add`)}
      >
        Add
      </button>
      <button className="btn btn-danger" onClick={handleDeleteAllProducts}>
        Delete All Products
      </button>
      <ConfirmationModal
        show={showConfirmation}
        message="Are you sure you want to delete this product?"
        onConfirm={deleteProduct}
        onCancel={cancelDelete}
      />
    </div>
  );
};

export default InventoryProducts;
