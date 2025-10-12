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
    const newError: { [key: string]: string } = {};
    if (!product.productName) {
      newError.productName = 'Product name is required';
    }
    if (!product.productDescription) {
      newError.productDescription = 'Product description is required';
    }
    if (!product.productPrice) {
      newError.productPrice = 'Product price is required';
    }
    if (!product.productQuantity) {
      newError.productQuantity = 'Product quantity is required';
    }
    if (!product.productSalePrice) {
      newError.productSalePrice = 'Product sale price is required';
    }
    setError(newError);
    return Object.keys(newError).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate()) return;

    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');
    setShowNotification(false);

    try {
      if (inventoryId) {
        await addSupplyToInventory(inventoryId, product);
        setSuccessMessage('Supply added successfully');
        setShowNotification(true);
        setTimeout(() => {
          navigate(`/inventories/${inventoryId}/products`);
        }, 2000);
      }
    } catch (error) {
      const msg = error instanceof Error ? error.message || '' : '';

      if (
        msg.toLowerCase().includes('same name') ||
        msg.toLowerCase().includes('already exists')
      ) {
        setError(prev => ({
          ...prev,
          productName:
            prev.productName || 'This product name is already in use.',
        }));
      }

      const next: Record<string, string> = {};
      if (product.productPrice <= 0)
        next.productPrice = 'Must be greater than 0';
      if (product.productQuantity <= 0)
        next.productQuantity = 'Must be greater than 0';
      if (product.productSalePrice <= 0)
        next.productSalePrice = 'Must be greater than 0';
      if (Object.keys(next).length) setError(prev => ({ ...prev, ...next }));
    } finally {
      setLoading(false);
    }
  };

  // Handle input changes
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { name, value, type } = e.target;

    setProduct(prev => ({
      ...prev,
      [name]: type === 'number' ? Number(value || 0) : value,
    }));

    setError(prev => ({ ...prev, [name]: '' })); // Clear error on change
  };

  return (
    <div className="add-supply-container">
      <div className="add-supply-form-container">
        <h1>Add Supply</h1>
        {errorMessage && (
          <p className="add-supply-error-message">{errorMessage}</p>
        )}
        <form onSubmit={handleSubmit}>
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
              value={product.productPrice}
              onChange={handleChange}
              aria-invalid={!!error.productPrice}
              aria-describedby={
                error.productPrice ? 'err-productPrice' : undefined
              }
              required
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
              value={product.productQuantity}
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
              value={product.productSalePrice}
              onChange={handleChange}
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
            disabled={loading}
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
