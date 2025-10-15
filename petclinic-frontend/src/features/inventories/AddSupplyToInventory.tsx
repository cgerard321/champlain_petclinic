import * as React from 'react';
import { FormEvent, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { addSupplyToInventory } from '@/features/inventories/api/AddSupplyToInventory.ts';
import { ProductRequestModel } from '@/features/inventories/models/InventoryModels/ProductRequestModel';
import './AddSupplyToInventory.css';

const AddSupplyToInventory: React.FC = (): JSX.Element => {
  const { inventoryId } = useParams<{ inventoryId: string }>();
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

  const navigate = useNavigate();

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
    setTimeout(() => navigate(`/inventories/${inventoryId}/products`), 2000);
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

  return (
    <div className="add-supply-container">
      <div className="add-supply-form-container">
        <h1>Add Supply</h1>
        {errorMessage && (
          <p className="add-supply-error-message">{errorMessage}</p>
        )}
        <form onSubmit={handleSubmit} noValidate>
          <div className="add-supply-form-group">
            <label>Product Name</label>
            <input
              className={`add-supply-input ${error.productName ? 'invalid animate' : ''}`}
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
              <span id="err-productName" className="add-supply-error-text">
                {error.productName}
              </span>
            )}
          </div>

          <div className="add-supply-form-group">
            <label>Product Description</label>
            <input
              className={`add-supply-input ${error.productDescription ? 'invalid animate' : ''}`}
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
              <span
                id="err-productDescription"
                className="add-supply-error-text"
              >
                {error.productDescription}
              </span>
            )}
          </div>

          <div className="add-supply-form-group">
            <label>Product Price</label>
            <input
              className={`add-supply-input ${error.productPrice ? 'invalid animate' : ''}`}
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
              <span id="err-productPrice" className="add-supply-error-text">
                {error.productPrice}
              </span>
            )}
          </div>

          <div className="add-supply-form-group">
            <label>Product Quantity</label>
            <input
              className={`add-supply-input ${error.productQuantity ? 'invalid animate' : ''}`}
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
              <span id="err-productQuantity" className="add-supply-error-text">
                {error.productQuantity}
              </span>
            )}
          </div>

          <div className="add-supply-form-group">
            <label>Product Sale Price</label>
            <input
              className={`add-supply-input ${error.productSalePrice ? 'invalid animate' : ''}`}
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
              <span id="err-productSalePrice" className="add-supply-error-text">
                {error.productSalePrice}
              </span>
            )}
          </div>

          <button
            className="add-supply-submit-button"
            type="submit"
            disabled={loading || !canSubmit}
          >
            {loading ? 'Adding...' : 'Add Supply'}
          </button>
        </form>
        {showNotification && (
          <p className="add-supply-success-message">{successMessage}</p>
        )}
      </div>
    </div>
  );
};

export default AddSupplyToInventory;
