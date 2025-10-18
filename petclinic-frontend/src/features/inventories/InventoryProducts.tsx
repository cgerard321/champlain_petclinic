import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
// import axios from 'axios'; wrong axios
import { ProductModel } from './models/ProductModels/ProductModel';
import './InventoriesListTable.module.css';
import './InventoryProducts.css';
import useSearchProducts from '@/features/inventories/hooks/useSearchProducts.ts';
import createPdf from './api/createPdf';
import ConfirmationModal from '@/features/inventories/ConfirmationModal.tsx';
import { Status } from '@/features/inventories/models/ProductModels/Status.ts';
import axiosInstance from '@/shared/api/axiosInstance';
import AddSupplyToInventory from '@/features/inventories/AddSupplyToInventory';
import EditInventoryProducts from '@/features/inventories/EditInventoryProducts';

const MAX_QTY = 100;

/** ---------- error helper (unchanged) ---------- */
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
  googleTranslateElementInit?: () => void;
};
// module-level flags so we only add & init once across navigations
let GT_SCRIPT_ADDED = false;
let GT_WIDGET_INIT = false;

const InventoryProducts: React.FC = () => {
  const { inventoryId } = useParams<{ inventoryId: string }>();
  const { productList, setProductList, getProductList } = useSearchProducts();

  // Declare state
  const [productName, setProductName] = useState<string>('');
  const [inventoryName, setInventoryName] = useState<string>('');
  const [productDescription, setProductDescription] = useState<string>('');
  const [productStatus, setProductStatus] = useState<Status | ''>('');
  const [products, setProducts] = useState<ProductModel[]>([]);
  const [filteredProducts, setFilteredProducts] = useState<ProductModel[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [showConfirmation, setShowConfirmation] = useState<boolean>(false);
  const [productToDelete, setProductToDelete] = useState<string | null>(null);
  const [pdfLoading, setPdfLoading] = useState(false);
  const [addOpen, setAddOpen] = useState(false);
  const [editOpen, setEditOpen] = useState(false);
  const [editProductId, setEditProductId] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleCreatePdf = async (): Promise<void> => {
    if (!inventoryId || pdfLoading) return;
    setPdfLoading(true);
    setError(null);

    const { errorMessage } = await createPdf(inventoryId);

    if (errorMessage) {
      setError(errorMessage);
    }
    setPdfLoading(false);
  };
  useEffect(() => {
    if (!inventoryId) return;
    axiosInstance
      .get(`/inventories/${inventoryId}`, { useV2: false })
      .then(res => {
        const name = (res.data?.inventoryName ?? res.data?.name ?? '')
          .toString()
          .trim();
        if (name) setInventoryName(name);
      })
      .catch(err => {
        console.warn('Failed to fetch inventory details', err);
      });
  }, [inventoryId]);
  useEffect(() => {
    const w = window as WindowWithGoogle;

    const initWidget = (): void => {
      if (GT_WIDGET_INIT) return;
      const host = document.getElementById('google_translate_element');
      if (!host) return;

      const TranslateElement = w.google?.translate?.TranslateElement;
      if (TranslateElement) {
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
      w.googleTranslateElementInit = initWidget;
      const s = document.createElement('script');
      s.src =
        '//translate.google.com/translate_a/element.js?cb=googleTranslateElementInit';
      s.async = true;
      document.body.appendChild(s);
      GT_SCRIPT_ADDED = true;
    } else {
      w.googleTranslateElementInit = initWidget;
    }
  }, []);

  const loadProducts = useCallback(async (): Promise<void> => {
    if (!inventoryId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await axiosInstance.get<ProductModel[]>(
        `/inventories/${inventoryId}/products/search`,
        { useV2: false }
      );
      const data = Array.isArray(res.data) ? res.data : [];
      data.forEach(p => {
        p.productMargin = parseFloat(
          (p.productSalePrice - p.productPrice).toFixed(2)
        );
      });
      setProducts(data);
      setProductList(data);
      setFilteredProducts(data);
    } finally {
      setLoading(false);
    }
  }, [inventoryId, setProductList]);

  useEffect(() => {
    void loadProducts();
  }, [loadProducts]);

  const deleteProduct = async (): Promise<void> => {
    if (productToDelete) {
      try {
        await axiosInstance.delete(
          `/inventories/${inventoryId}/products/${productToDelete}`,
          { useV2: false }
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
        `/inventories/${inventoryId}/products/${productId}/restockProduct`,
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
    if (currentQuantity <= 0) return;

    try {
      const updatedQuantity = currentQuantity - 1;
      await axiosInstance.patch(
        `/inventories/${inventoryId}/products/${productId}/consume`,
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
    } catch {
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
        Supplies in Inventory: <span>{inventoryName}</span>
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
      <button
        className="btn btn-primary"
        onClick={handleCreatePdf}
        disabled={pdfLoading}
      >
        {pdfLoading ? 'Downloading…' : 'Download PDF'}
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
            onChange={e => setProductStatus(e.target.value as Status | '')}
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
      <table className="table table-striped">
        <thead>
          <tr>
            <th>Supply Id</th>
            <th>Supply Name</th>
            <th>Description</th>
            <th>
              <span className="th-2line">
                Sale
                <br />
                Price
              </span>
            </th>
            <th>
              <span className="th-2line">
                Cost
                <br />
                Price
              </span>
            </th>
            <th>
              <span className="th-2line">
                Profit
                <br />
                Margin
              </span>
            </th>
            <th>Quantity</th>
            <th>Status</th>
            <th>Actions</th>
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
                <td>${product.productPrice}</td>
                <td
                  style={{
                    color: product.productMargin >= 0 ? 'green' : 'red',
                    fontWeight: 'bold',
                  }}
                >
                  ${product.productMargin}
                </td>
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

                <td className="actions-cell">
                  <div className="actions-group">
                    <button
                      onClick={e => {
                        e.stopPropagation();
                        setEditProductId(product.productId);
                        setEditOpen(true);
                      }}
                      className="btn btn-warning btn-sm"
                    >
                      Edit
                    </button>

                    <button
                      className="btn btn-danger btm-sm"
                      onClick={() => handleDeleteClick(product.productId)}
                    >
                      Delete
                    </button>

                    <button
                      className="btn btn-success btn-sm"
                      onClick={() =>
                        addQuantity(product.productId, product.productQuantity)
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
                      className="btn btn-info btn-sm"
                      onClick={() =>
                        reduceQuantity(
                          product.productId,
                          product.productQuantity
                        )
                      }
                      disabled={product.productQuantity <= 0}
                      title={
                        product.productQuantity <= 0 ? 'Quantity already 0' : ''
                      }
                    >
                      Reduce Quantity
                    </button>

                    <button
                      onClick={e => {
                        e.stopPropagation();
                        navigate(`${product.productId}`);
                      }}
                      className="btn btn-info btn-sm"
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
      <button className="btn btn-add" onClick={() => setAddOpen(true)}>
        Add
      </button>
      <ConfirmationModal
        show={showConfirmation}
        message="Are you sure you want to delete this product?"
        onConfirm={deleteProduct}
        onCancel={cancelDelete}
      />
      {addOpen && (
        <AddSupplyToInventory
          open={addOpen}
          onClose={() => setAddOpen(false)}
          inventoryIdProp={inventoryId}
          onAdded={() => {
            setAddOpen(false);
            void loadProducts(); // refresh table after add
          }}
        />
      )}

      {editOpen && (
        <EditInventoryProducts
          open={editOpen}
          onClose={() => {
            setEditOpen(false);
            setEditProductId(null);
          }}
          inventoryIdProp={inventoryId}
          productIdProp={editProductId ?? undefined}
          onUpdated={() => {
            setEditOpen(false);
            setEditProductId(null);
            void loadProducts(); // refresh table after update
          }}
        />
      )}
    </div>
  );
};

export default InventoryProducts;
