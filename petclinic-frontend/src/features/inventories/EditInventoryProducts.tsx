import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getProductByProductIdInInventory,
  updateProductInInventory,
} from '@/features/inventories/api/EditInventoryProducts.ts';
import { ProductRequestModel } from '@/features/inventories/models/InventoryModels/ProductRequestModel';
import './EditInventoryProduct.css';

const MAX_QTY = 100;

type ProductKeys = keyof ProductRequestModel;

const EditInventoryProducts: React.FC = (): JSX.Element => {
  const { inventoryId, productId } = useParams<{
    inventoryId: string;
    productId: string;
  }>();

  const [product, setProduct] = useState<ProductRequestModel>({
    productName: '',
    productDescription: '',
    productPrice: 0,
    productQuantity: 0,
    productSalePrice: 0,
  });

  const [error, setError] = useState<
    { [key: string]: string } & { message?: string }
  >({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);

  const canSubmit =
    !!product.productName.trim() &&
    !!product.productDescription.trim() &&
    Number(product.productPrice) > 0 &&
    Number(product.productQuantity) > 0 &&
    Number(product.productSalePrice) > 0 &&
    Number(product.productQuantity) <= MAX_QTY &&
    !loading;

  const navigate = useNavigate();

  useEffect(() => {
    const fetchProduct = async (): Promise<void> => {
      if (!inventoryId || !productId) return;
      setLoading(true);

      const res = await getProductByProductIdInInventory(
        inventoryId,
        productId
      );
      if (res.errorMessage || !res.data) {
        setErrorMessage(res.errorMessage ?? 'Unable to fetch product data.');
        setLoading(false);
        return;
      }

      const p = res.data;
      setProduct({
        productName: p.productName,
        productDescription: p.productDescription,
        productPrice: p.productPrice,
        productQuantity: p.productQuantity,
        productSalePrice: p.productSalePrice,
      });
      setLoading(false);
    };
    void fetchProduct();
  }, [inventoryId, productId]);

  const validate = (): boolean => {
    const errors: Record<string, string> = {};

    if (!product.productName?.trim())
      errors.productName = 'Product name is required';
    if (!product.productDescription?.trim())
      errors.productDescription = 'Product description is required';

    if (!(Number(product.productPrice) > 0))
      errors.productPrice = 'Product price must be greater than 0';
    if (!(Number(product.productQuantity) > 0))
      errors.productQuantity = 'Product quantity must be greater than 0';
    else if (Number(product.productQuantity) > MAX_QTY)
      errors.productQuantity = `Product quantity must not exceed ${MAX_QTY}`;
    if (!(Number(product.productSalePrice) > 0))
      errors.productSalePrice = 'Sale price must be greater than 0';

    setError(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate()) {
      setErrorMessage('Please fix the highlighted errors and try again.');
      return;
    }

    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');
    setShowNotification(false);

    const res =
      inventoryId && productId
        ? await updateProductInInventory(inventoryId, productId, product)
        : { data: undefined, errorMessage: 'Missing inventory or product ID.' };

    setLoading(false);

    if (res.errorMessage) {
      const msg = (res.errorMessage ?? '').toLowerCase().trim();

      if (msg.includes('already exists') && msg.includes('product')) {
        setError(prev => ({
          ...prev,
          productName:
            'A product with this name already exists in this inventory.',
        }));
        setErrorMessage('');
        return;
      }
      if (msg.includes('price') && msg.includes('greater than 0')) {
        setError(prev => ({
          ...prev,
          productPrice: 'Product price must be greater than 0',
        }));
        setErrorMessage('');
        return;
      }
      if (msg.includes('quantity') && msg.includes('greater than 0')) {
        setError(prev => ({
          ...prev,
          productQuantity: 'Product quantity must be greater than 0',
        }));
        setErrorMessage('');
        return;
      }
      if (msg.includes('sale price') && msg.includes('greater than 0')) {
        setError(prev => ({
          ...prev,
          productSalePrice: 'Sale price must be greater than 0',
        }));
        setErrorMessage('');
        return;
      }
      setErrorMessage(res.errorMessage ?? 'An unexpected error occurred');
      return;
    }

    setSuccessMessage('Product updated successfully');
    setShowNotification(true);
    setTimeout(() => {
      navigate(`/inventories/${inventoryId}/products`);
    }, 2000);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { name, type, value } = e.target as HTMLInputElement & {
      name: ProductKeys;
    };

    if (name === 'productQuantity') {
      const digitsOnly = value.replace(/[^\d]/g, '');
      setProduct(prev => ({
        ...prev,
        productQuantity:
          digitsOnly === '' ? ('' as unknown as number) : Number(digitsOnly),
      }));

      const qty = Number(digitsOnly);
      setError(prev => {
        const next = { ...prev };
        if (!Number.isFinite(qty) || qty <= 0) {
          next.productQuantity = 'Product quantity must be greater than 0';
        } else if (qty > MAX_QTY) {
          next.productQuantity = `Product quantity must not exceed ${MAX_QTY}`;
        } else {
          next.productQuantity = '';
        }
        return next;
      });

      if (errorMessage) setErrorMessage('');
      return;
    }
    setProduct(prev => ({
      ...prev,
      [name]:
        type === 'number'
          ? Number(value)
          : (value as ProductRequestModel[typeof name]),
    }));

    setError(prev => {
      const next = { ...prev };
      if (name === 'productPrice') {
        const n = Number(value);
        next.productPrice =
          !Number.isFinite(n) || n <= 0
            ? 'Product price must be greater than 0'
            : '';
      } else if (name === 'productSalePrice') {
        const n = Number(value);
        next.productSalePrice =
          !Number.isFinite(n) || n <= 0
            ? 'Sale price must be greater than 0'
            : '';
      } else if (name === 'productName') {
        next.productName = value.trim() ? '' : 'Product name is required';
      } else if (name === 'productDescription') {
        next.productDescription = value.trim()
          ? ''
          : 'Product description is required';
      }
      return next;
    });

    if (errorMessage) setErrorMessage('');

    if (error[name]) {
      setError(prev => ({ ...prev, [name]: '' }));
    }
    if (error.message) setErrorMessage('');
  };

  return (
    <div className="edit-product-container">
      <div className="edit-product-form-container">
        <h2>Edit Product</h2>

        {errorMessage && (
          <div className="edit-product-error-message">{errorMessage}</div>
        )}
        {successMessage && showNotification && (
          <div className="edit-product-success-message">{successMessage}</div>
        )}

        <form onSubmit={handleSubmit} noValidate>
          <div className="edit-product-form-group">
            <label htmlFor="productName">Name</label>
            <input
              id="productName"
              className={`edit-product-input ${error.productName ? 'invalid animate' : ''}`}
              type="text"
              name="productName"
              value={product.productName}
              onChange={handleChange}
              aria-invalid={!!error.productName}
              aria-describedby={
                error.productName ? 'err-productName' : undefined
              }
            />
            {error.productName && (
              <div id="err-productName" className="edit-product-error-text">
                {error.productName}
              </div>
            )}
          </div>

          <div className="edit-product-form-group">
            <label htmlFor="productDescription">Description</label>
            <input
              id="productDescription"
              className={`edit-product-input ${error.productDescription ? 'invalid animate' : ''}`}
              type="text"
              name="productDescription"
              value={product.productDescription}
              onChange={handleChange}
              aria-invalid={!!error.productDescription}
              aria-describedby={
                error.productDescription ? 'err-productDescription' : undefined
              }
            />
            {error.productDescription && (
              <div
                id="err-productDescription"
                className="edit-product-error-text"
              >
                {error.productDescription}
              </div>
            )}
          </div>

          <div className="edit-product-form-group">
            <label htmlFor="productPrice">Cost Price</label>
            <input
              id="productPrice"
              className={`edit-product-input ${error.productPrice ? 'invalid animate' : ''}`}
              type="number"
              name="productPrice"
              value={product.productPrice}
              onChange={handleChange}
              min={1}
              step="any"
              aria-invalid={!!error.productPrice}
              aria-describedby={
                error.productPrice ? 'err-productPrice' : undefined
              }
            />
            {error.productPrice && (
              <div id="err-productPrice" className="edit-product-error-text">
                {error.productPrice}
              </div>
            )}
          </div>
          <div className="edit-product-form-group">
            <label htmlFor="productQuantity">Quantity</label>
            <input
              id="productQuantity"
              className={`edit-product-input ${error.productQuantity ? 'invalid animate' : ''}`}
              type="number"
              name="productQuantity"
              value={product.productQuantity ?? ''}
              onChange={handleChange}
              inputMode="numeric"
              pattern="\d*"
              step={1}
              min={1}
              max={MAX_QTY}
              onKeyDown={e => {
                if (['.', '-', 'e', 'E', '+'].includes(e.key))
                  e.preventDefault();
              }}
              aria-invalid={!!error.productQuantity}
              aria-describedby={
                error.productQuantity ? 'err-productQuantity' : undefined
              }
            />
            {error.productQuantity && (
              <div id="err-productQuantity" className="edit-product-error-text">
                {error.productQuantity}
              </div>
            )}
          </div>

          <div className="edit-product-form-group">
            <label htmlFor="productSalePrice">Sale Price</label>
            <input
              id="productSalePrice"
              className={`edit-product-input ${error.productSalePrice ? 'invalid animate' : ''}`}
              type="number"
              name="productSalePrice"
              value={product.productSalePrice}
              onChange={handleChange}
              min={1}
              step="any"
              aria-invalid={!!error.productSalePrice}
              aria-describedby={
                error.productSalePrice ? 'err-productSalePrice' : undefined
              }
            />
            {error.productSalePrice && (
              <div
                id="err-productSalePrice"
                className="edit-product-error-text"
              >
                {error.productSalePrice}
              </div>
            )}
          </div>

          <button
            type="submit"
            className="edit-product-submit-button"
            disabled={!canSubmit}
          >
            {loading ? 'Updating…' : 'Update'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default EditInventoryProducts;
