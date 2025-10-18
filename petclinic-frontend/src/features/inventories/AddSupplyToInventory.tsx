import * as React from 'react';
import { FormEvent, useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import { addSupplyToInventory } from '@/features/inventories/api/AddSupplyToInventory.ts';
import { ProductRequestModel } from '@/features/inventories/models/InventoryModels/ProductRequestModel';
import styles from './InvProForm.module.css';

type Props = {
  open: boolean;
  onClose: () => void;
  inventoryIdProp?: string;
  onAdded?: () => void;
  existingProductNames?: string[];
};

const AddSupplyToInventory: React.FC<Props> = ({
  open,
  onClose,
  inventoryIdProp,
  onAdded,
  existingProductNames = [],
}): JSX.Element | null => {
  const routeId = useParams<{ inventoryId: string }>().inventoryId;
  const inventoryId = inventoryIdProp ?? routeId;

  const [product, setProduct] = useState<ProductRequestModel>({
    productName: '',
    productDescription: '',
    productPrice: 0,
    productQuantity: 0,
    productSalePrice: 0,
  });
  const [error, setError] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);

  const validate = (): boolean => {
    const err: Record<string, string> = {};
    if (!product.productName?.trim())
      err.productName = 'Product name is required';
    if (!product.productDescription?.trim())
      err.productDescription = 'Product description is required';

    setError(err);
    return Object.keys(err).length === 0;
  };

  const isValidNumbers =
    Number(product.productPrice) > 0 &&
    Number(product.productQuantity) > 0 &&
    Number(product.productSalePrice) > 0;

  const canSubmit =
    !!product.productName.trim() &&
    !!product.productDescription.trim() &&
    isValidNumbers &&
    !loading;

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    if (!validate()) {
      setErrorMessage('Please fix the highlighted errors and try again.');
      return;
    }
    if (!isValidNumbers) {
      const next: Record<string, string> = {};
      if (!(Number(product.productPrice) > 0))
        next.productPrice = 'Product price must be greater than 0';
      if (!(Number(product.productQuantity) > 0))
        next.productQuantity = 'Product quantity must be greater than 0';
      if (!(Number(product.productSalePrice) > 0))
        next.productSalePrice = 'Product sale price must be greater than 0';
      setError(p => ({ ...p, ...next }));
      setErrorMessage('Please fix the highlighted errors and try again.');
      return;
    }

    const submitted = product.productName.trim().toLowerCase();
    const nameClashes = existingProductNames.some(
      n => n.trim().toLowerCase() === submitted
    );
    if (nameClashes) {
      setError(prev => ({
        ...prev,
        productName: 'This product name is already in use.',
      }));
      setErrorMessage(''); // no global error banner needed
      return;
    }

    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');
    setShowNotification(false);

    if (!inventoryId) {
      setErrorMessage('Inventory ID is missing in the URL.');
      setLoading(false);
      return;
    }

    const { errorMessage: apiError } = await addSupplyToInventory(
      inventoryId,
      product
    );

    setLoading(false);

    if (apiError) {
      if (/(already exists|duplicate|same name)/.test(apiError.toLowerCase())) {
        setError(p => ({
          ...p,
          productName: p.productName || 'This product name is already in use.',
        }));
        setErrorMessage('');
        return;
      }
      setErrorMessage(apiError);
      return;
    }

    setSuccessMessage('Supply added successfully');
    setShowNotification(true);
    onAdded?.();
    setTimeout(() => onClose?.(), 800);
  };

  // Handle input changes
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { name, value, type } = e.target;

    setProduct(prev => ({
      ...prev,
      [name]:
        type === 'number'
          ? value === ''
            ? ('' as unknown as number)
            : Number(value)
          : value,
    }));

    if (error[name]) setError(prev => ({ ...prev, [name]: '' }));
    if (errorMessage) setErrorMessage('');
  };

  const overlayRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!open) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    requestAnimationFrame(() => overlayRef.current?.focus());
    return () => {
      document.body.style.overflow = prev;
    };
  }, [open]);

  const handleEsc: React.KeyboardEventHandler<HTMLDivElement> = e => {
    if (e.key === 'Escape' || e.key === 'Esc') onClose();
  };

  const handleBackdropClick: React.MouseEventHandler<HTMLDivElement> = e => {
    if (e.target === e.currentTarget) onClose();
  };

  if (!open) return null;
  return (
    <div
      ref={overlayRef}
      className={styles.overlay}
      role="dialog"
      aria-modal="true"
      tabIndex={-1}
      onKeyDown={handleEsc}
      onMouseDown={handleBackdropClick}
    >
      <div className={styles['form-container']}>
        <h2>Add Supply</h2>

        {loading && (
          <div className="loading-overlay">
            <div className="loader" />
          </div>
        )}

        {errorMessage && (
          <div className="field-error" style={{ marginTop: 8 }}>
            {errorMessage}
          </div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          <div>
            <label htmlFor="as-productName">Product Name</label>
            <input
              id="as-productName"
              className={error.productName ? 'invalid animate' : ''}
              type="text"
              name="productName"
              value={product.productName}
              onChange={handleChange}
              aria-invalid={!!error.productName}
              aria-describedby={
                error.productName ? 'err-productName' : undefined
              }
              required
            />
            {error.productName && (
              <div id="err-productName" className="field-error">
                {error.productName}
              </div>
            )}
          </div>

          <div>
            <label htmlFor="as-productDescription">Product Description</label>
            <input
              id="as-productDescription"
              className={error.productDescription ? 'invalid animate' : ''}
              type="text"
              name="productDescription"
              value={product.productDescription}
              onChange={handleChange}
              aria-invalid={!!error.productDescription}
              aria-describedby={
                error.productDescription ? 'err-productDescription' : undefined
              }
              required
            />
            {error.productDescription && (
              <div id="err-productDescription" className="field-error">
                {error.productDescription}
              </div>
            )}
          </div>

          <div>
            <label htmlFor="as-productPrice">Product Price</label>
            <input
              id="as-productPrice"
              className={error.productPrice ? 'invalid animate' : ''}
              type="number"
              name="productPrice"
              value={product.productPrice ?? ''}
              onChange={handleChange}
              step="any"
              min={1}
              inputMode="decimal"
              onKeyDown={e => {
                if (['-', '+', 'e', 'E'].includes(e.key)) e.preventDefault();
              }}
              aria-invalid={!!error.productPrice}
              aria-describedby={
                error.productPrice ? 'err-productPrice' : undefined
              }
            />
            {error.productPrice && (
              <div id="err-productPrice" className="field-error">
                {error.productPrice}
              </div>
            )}
          </div>

          <div>
            <label htmlFor="as-productQuantity">Product Quantity</label>
            <input
              id="as-productQuantity"
              className={error.productQuantity ? 'invalid animate' : ''}
              type="number"
              name="productQuantity"
              step={1}
              min={1}
              inputMode="numeric"
              onKeyDown={e => {
                if (['-', '+', 'e', 'E', '.'].includes(e.key))
                  e.preventDefault();
              }}
              value={product.productQuantity ?? ''}
              onChange={handleChange}
              aria-invalid={!!error.productQuantity}
              aria-describedby={
                error.productQuantity ? 'err-productQuantity' : undefined
              }
              required
            />
            {error.productQuantity && (
              <div id="err-productQuantity" className="field-error">
                {error.productQuantity}
              </div>
            )}
          </div>

          <div>
            <label htmlFor="as-productSalePrice">Product Sale Price</label>
            <input
              id="as-productSalePrice"
              className={error.productSalePrice ? 'invalid animate' : ''}
              type="number"
              name="productSalePrice"
              value={product.productSalePrice ?? ''}
              onChange={handleChange}
              step="any"
              min={1}
              inputMode="decimal"
              onKeyDown={e => {
                if (['-', '+', 'e', 'E'].includes(e.key)) e.preventDefault();
              }}
              aria-invalid={!!error.productSalePrice}
              aria-describedby={
                error.productSalePrice ? 'err-productSalePrice' : undefined
              }
              required
            />
            {error.productSalePrice && (
              <div id="err-productSalePrice" className="field-error">
                {error.productSalePrice}
              </div>
            )}
          </div>

          <button type="submit" disabled={loading || !canSubmit}>
            {loading ? 'Adding...' : 'Add Supply'}
          </button>
          <button type="button" className="cancel" onClick={onClose}>
            Cancel
          </button>
        </form>

        {showNotification && successMessage && (
          <div
            style={{
              position: 'absolute',
              top: 8,
              right: 8,
              background: '#28a745',
              color: '#fff',
              padding: '6px 10px',
              borderRadius: 4,
              fontSize: 12,
            }}
            role="status"
            aria-live="polite"
          >
            {successMessage}
          </div>
        )}
      </div>
    </div>
  );
};

export default AddSupplyToInventory;
