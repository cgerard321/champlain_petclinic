import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ProductModel } from './models/ProductModels/ProductModel';
import './InventoriesListTable.module.css';
import './InventoryProducts.css';
import useSearchProducts from '@/features/inventories/hooks/useSearchProducts.ts';
import deleteAllProductsFromInventory from './api/deleteAllProductsFromInventory';
import createPdf from './api/createPdf';
import ConfirmationModal from '@/features/inventories/ConfirmationModal.tsx';
import { Status } from '@/features/inventories/models/ProductModels/Status.ts';
import axiosInstance from '@/shared/api/axiosInstance';

// ---- Local types (no global augmentation) ----
type GoogleNS = {
  translate?: {
    TranslateElement?: new (
      opts: {
        pageLanguage: string;
        autoDisplay: boolean;
        includedLanguages: string;
      },
      elementId: string
    ) => void;
  };
};

type WindowWithGoogle = Window & {
  google?: GoogleNS;
  __gtInit?: () => void;
};

let GT_SCRIPT_ADDED = false;
let GT_WIDGET_INIT = false;

const MAX_QTY = 100;

// ---- Error helper (no `any`) ----
type AxiosErrorLike = {
  response?: { status?: number; statusText?: string; data?: unknown };
  message?: string;
};

const getErrorMessage = (err: unknown): string => {
  if (err && typeof err === 'object') {
    const e = err as AxiosErrorLike;
    if (e.response) {
      const { status = '', statusText = '', data } = e.response;
      const body = typeof data === 'string' ? data : JSON.stringify(data);
      return `(${status}) ${statusText} — ${body}`;
    }
    if (typeof e.message === 'string') return e.message;
  }
  return 'Unknown error';
};

const InventoryProducts: React.FC = () => {
  const { inventoryId } = useParams<{ inventoryId: string }>();
  const { productList, setProductList, getProductList } = useSearchProducts();

  // State
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
    if (!inventoryId) return;
    try {
      await createPdf(inventoryId);
    } catch (err: unknown) {
      console.error('Failed to create PDF', getErrorMessage(err));
    }
  };

  // Google Translate: load once, init once (no global typing)
  useEffect((): void => {
    const aw = window as WindowWithGoogle;

    const initWidget = (): void => {
      if (GT_WIDGET_INIT) return;
      const host = document.getElementById('google_translate_element');
      if (!host) return;
      const TranslateElement = aw.google?.translate?.TranslateElement;
      if (TranslateElement) {
        // eslint-disable-next-line no-new
        new TranslateElement(
          {
            pageLanguage: 'en',
            autoDisplay: false,
            includedLanguages: 'en,fr,de,es',
          },
          'google_translate_element'
        );
        GT_WIDGET_INIT = true;
      }
    };

    if ((window as WindowWithGoogle).google?.translate?.TranslateElement) {
      initWidget();
      return;
    }

    if (!GT_SCRIPT_ADDED) {
      aw.__gtInit = initWidget;
      const s = document.createElement('script');
      s.src = '//translate.google.com/translate_a/element.js?cb=__gtInit';
      s.async = true;
      document.body.appendChild(s);
      GT_SCRIPT_ADDED = true;
    } else {
      aw.__gtInit = initWidget;
    }
  }, []);

  // Fetch products
  useEffect(() => {
    const fetchProducts = async (): Promise<void> => {
      setLoading(true);
      setError(null);
      try {
        const response = await axiosInstance.get<ProductModel[]>(
          `/inventory/${inventoryId}/products/search`,
          { useV2: false }
        );
        const data = Array.isArray(response.data) ? response.data : [];
        setProducts(data);
        setProductList(data);
        setFilteredProducts(data);
      } finally {
        setLoading(false);
      }
    };

    if (inventoryId) {
      fetchProducts().catch(err => console.error(err));
    }
  }, [inventoryId, setProductList]);

  // Delete product
  const deleteProduct = async (): Promise<void> => {
    if (!productToDelete) return;
    try {
      await axiosInstance.delete(
        `/inventory/${inventoryId}/products/${productToDelete}`,
        { useV2: false }
      );
      const updatedProducts = products.filter(
        p => p.productId !== productToDelete
      );
      setProducts(updatedProducts);
      setFilteredProducts(updatedProducts);
    } catch {
      setError('Failed to delete product.');
    } finally {
      setShowConfirmation(false);
      setProductToDelete(null);
    }
  };

  const handleDeleteAllProducts = async (): Promise<void> => {
    try {
      await deleteAllProductsFromInventory({ inventoryId: inventoryId! });
      setProducts([]);
      setFilteredProducts([]);
      alert('All products deleted successfully.');
    } catch {
      setError('Failed to delete all products.');
    }
  };

  const handleFilter = async (): Promise<void> => {
    if (productName || productDescription || productStatus) {
      await getProductList(
        inventoryId!,
        productName || undefined,
        productDescription || undefined,
        productStatus || undefined
      );
    }

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

  const addQuantity = async (
    productId: string,
    currentQuantity: number
  ): Promise<void> => {
    if (currentQuantity >= MAX_QTY) {
      setError(`Max quantity (${MAX_QTY}) reached.`);
      return;
    }

    try {
      const delta = 1;

      await axiosInstance.put(
        `/inventory/${inventoryId}/products/${productId}/restockProduct`,
        null,
        { params: { productQuantity: delta }, useV2: false }
      );

      const updatedQuantity = Math.min(MAX_QTY, currentQuantity + delta);

      let updatedStatus: Status = Status.AVAILABLE;
      if (updatedQuantity === 0) updatedStatus = Status.OUT_OF_STOCK;
      else if (updatedQuantity <= 20) updatedStatus = Status.RE_ORDER;

      const updated = filteredProducts.map(p =>
        p.productId === productId
          ? { ...p, productQuantity: updatedQuantity, status: updatedStatus }
          : p
      );

      setProducts(updated);
      setFilteredProducts(updated);
      setError(null);
    } catch (err: unknown) {
      const msg = getErrorMessage(err);
      console.error('Add quantity failed:', msg);
      setError(`Failed to add product quantity: ${msg}`);
    }
  };

  const reduceQuantity = async (
    productId: string,
    currentQuantity: number
  ): Promise<void> => {
    if (currentQuantity <= 0) {
      setError('Quantity cannot go below 0.');
      return;
    }

    try {
      const updatedQuantity = currentQuantity - 1;
      await axiosInstance.patch(
        `/inventory/${inventoryId}/products/${productId}/consume`,
        { productQuantity: updatedQuantity },
        { useV2: false }
      );

      let updatedStatus: Status = Status.AVAILABLE;
      if (updatedQuantity === 0) updatedStatus = Status.OUT_OF_STOCK;
      else if (updatedQuantity <= 20) updatedStatus = Status.RE_ORDER;

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
      setError(null);
    } catch (err: unknown) {
      const msg = getErrorMessage(err);
      console.error('Reduce quantity failed:', msg);
      setError('Failed to reduce product quantity.');
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
      <div id="google_translate_element"></div>
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

      <div className="table-wrap">
        <table className="table table-striped inventory-table">
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
            {filteredProducts.length > 0 ? (
              filteredProducts.map((product: ProductModel) => (
                <tr key={product.productId}>
                  <td>
                    <span className="truncate">{product.productId}</span>
                  </td>
                  <td>
                    <span className="truncate">{product.productName}</span>
                  </td>
                  <td>
                    <span className="truncate">
                      {product.productDescription}
                    </span>
                  </td>
                  <td>${product.productSalePrice}</td>
                  <td>{product.productQuantity}</td>
                  <td
                    className={
                      product.status === Status.RE_ORDER
                        ? 'status-reorder'
                        : product.status === Status.OUT_OF_STOCK
                          ? 'status-out-of-stock'
                          : 'status-available'
                    }
                  >
                    {product.status.replace('_', ' ')}
                  </td>

                  <td className="actions-cell">
                    <div className="actions-group">
                      <button
                        onClick={e => {
                          e.stopPropagation();
                          navigate(`${product.productId}/edit`);
                        }}
                        className="btn btn-warning"
                      >
                        Edit
                      </button>

                      <button
                        className="btn btn-danger"
                        onClick={() => handleDeleteClick(product.productId)}
                      >
                        Delete
                      </button>

                      <button
                        className="btn btn-success"
                        onClick={() =>
                          addQuantity(
                            product.productId,
                            product.productQuantity
                          )
                        }
                        disabled={product.productQuantity >= MAX_QTY}
                        title={
                          product.productQuantity >= MAX_QTY
                            ? 'Max quantity reached'
                            : ''
                        }
                      >
                        Add Quantity
                      </button>

                      <button
                        className="btn btn-info"
                        onClick={() =>
                          reduceQuantity(
                            product.productId,
                            product.productQuantity
                          )
                        }
                      >
                        Reduce Quantity
                      </button>

                      <button
                        onClick={e => {
                          e.stopPropagation();
                          navigate(`${product.productId}/move`);
                        }}
                        className="btn btn-info"
                      >
                        Move
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan={7} style={{ textAlign: 'center' }}>
                  No products available.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

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
