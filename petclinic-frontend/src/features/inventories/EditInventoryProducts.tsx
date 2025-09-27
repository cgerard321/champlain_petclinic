import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getProductByProductIdInInventory,
  updateProductInInventory,
} from '@/features/inventories/api/EditInventoryProducts.ts';
import { ProductRequestModel } from '@/features/inventories/models/InventoryModels/ProductRequestModel';

const MAX_QTY = 100;

function validateQuantityValue(n: unknown): string | null {
  if (n === null || n === undefined || Number.isNaN(Number(n))) {
    return 'Quantity is required';
  }
  const num = Number(n);
  if (!Number.isInteger(num)) return 'Quantity must be a whole number';
  if (num <= 0) return 'Quantity must be greater than 0';
  if (num > MAX_QTY) return `Quantity cannot exceed ${MAX_QTY}`;
  return null;
}

interface ApiError {
  message: string;
}

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

  const navigate = useNavigate();

  useEffect(() => {
    const fetchProduct = async (): Promise<void> => {
      if (inventoryId && productId) {
        try {
          const response = await getProductByProductIdInInventory(
            inventoryId,
            productId
          );
          setProduct(response);
        } catch (err) {
          console.error(`Error fetching product with ID ${productId}:`, err);
        }
      }
    };
    fetchProduct().catch(err => console.error('Error in fetchProduct:', err));
  }, [inventoryId, productId]);

  const validate = (): boolean => {
    const newError: { [key: string]: string } = {};

    if (!product.productName) newError.productName = 'Product name is required';
    if (!product.productDescription)
      newError.productDescription = 'Product description is required';

    if (product.productPrice === undefined || product.productPrice === null) {
      newError.productPrice = 'Product price is required';
    }

    const qtyMsg = validateQuantityValue(product.productQuantity);
    if (qtyMsg) newError.productQuantity = qtyMsg;

    if (
      product.productSalePrice === undefined ||
      product.productSalePrice === null
    ) {
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
      if (inventoryId && productId) {
        await updateProductInInventory(inventoryId, productId, product);
        setSuccessMessage('Product updated successfully');
        setShowNotification(true);
        setTimeout(() => {
          navigate(`/inventories/${inventoryId}/products`);
        }, 2000);
      }
    } catch (err) {
      const apiError = err as ApiError;
      setErrorMessage(`Error updating product: ${apiError.message}`);
    } finally {
      setLoading(false);
    }
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

      const msg =
        digitsOnly === ''
          ? 'Quantity is required'
          : validateQuantityValue(Number(digitsOnly));
      setError(prev => ({ ...prev, productQuantity: msg ?? '' }));
      return;
    }

    setProduct(prev => ({
      ...prev,
      [name]:
        type === 'number'
          ? Number(value)
          : (value as ProductRequestModel[typeof name]),
    }));
  };

  return (
    <div
      className="card d-flex justify-content-center align-items-center"
      style={{
        width: '500px',
        height: '700px',
        backgroundColor: 'lightgray',
        margin: '0 auto',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
      }}
    >
      <h2>Edit Product</h2>

      <br />

      <form onSubmit={handleSubmit}>
        <h6>Name</h6>
        <div className="input-group mb-3">
          <input
            className="form-control"
            type="text"
            name="productName"
            value={product.productName}
            onChange={handleChange}
            required
          />
        </div>

        <br />

        <h6>Description</h6>
        <div className="input-group mb-3">
          <input
            className="form-control"
            type="text"
            name="productDescription"
            value={product.productDescription}
            onChange={handleChange}
            required
          />
        </div>

        <br />

        <h6>Cost Price</h6>
        <div className="input-group mb-3">
          <span className="input-group-text">$</span>
          <input
            className="form-control"
            aria-label="Amount (to the nearest dollar)"
            type="number"
            name="productPrice"
            value={product.productPrice}
            onChange={handleChange}
            required
          />
          <span className="input-group-text">.00</span>
        </div>

        <br />

        <h6>Quantity</h6>
        <div className="input-group mb-3">
          <input
            className="form-control"
            type="number"
            name="productQuantity"
            value={product.productQuantity}
            onChange={handleChange}
            inputMode="numeric"
            pattern="\d*"
            step={1}
            min={1}
            max={MAX_QTY}
            onKeyDown={e => {
              // prevent '.', '-', 'e', 'E', '+'
              if (['.', '-', 'e', 'E', '+'].includes(e.key)) e.preventDefault();
            }}
            required
          />
        </div>
        {error.productQuantity && (
          <p style={{ color: 'red', marginTop: -8 }}>{error.productQuantity}</p>
        )}

        <br />

        <h6>Sale Price</h6>
        <div className="input-group mb-3">
          <span className="input-group-text">$</span>
          <input
            className="form-control"
            aria-label="Amount (to the nearest dollar)"
            type="number"
            name="productSalePrice"
            value={product.productSalePrice}
            onChange={handleChange}
            required
          />
          <span className="input-group-text">.00</span>
        </div>

        <button
          type="submit"
          style={{
            width: '100px',
            backgroundColor: '#333',
            color: 'white',
          }}
        >
          Update
        </button>
      </form>

      <div>
        {loading && <p>Loading...</p>}
        {error.message && <p style={{ color: 'red' }}>{error.message}</p>}
        {errorMessage && <p style={{ color: 'red' }}>{errorMessage}</p>}
        {showNotification ? (
          <div className="notification">
            {successMessage && (
              <p style={{ color: 'white' }}>{successMessage}</p>
            )}
          </div>
        ) : null}
      </div>
    </div>
  );
};

export default EditInventoryProducts;
