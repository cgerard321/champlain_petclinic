import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getProductByProductIdInInventory,
  updateProductInInventory,
} from '@/features/inventories/api/EditInventoryProducts.ts';
import { ProductRequestModel } from '@/features/inventories/models/InventoryModels/ProductRequestModel';

interface ApiError {
  message: string;
}

const EditInventoryProducts: React.FC = (): JSX.Element => {
  const { inventoryId, productId } = useParams<{
    inventoryId: string;
    productId: string;
  }>(); // Get params from URL
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

  useEffect(() => {
    const fetchProduct = async (): Promise<void> => {
      if (inventoryId && productId) {
        try {
          const response = await getProductByProductIdInInventory(
              inventoryId,
              productId
          );
          setProduct(response);
        } catch (error) {
          console.error(`Error fetching product with ID ${productId}:`, error);
        }
      }
    };
    fetchProduct().catch(error =>
        console.error('Error in fetchProduct:', error)
    );
  }, [inventoryId, productId]);

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
      if (inventoryId && productId) {
        await updateProductInInventory(inventoryId, productId, product);
        setSuccessMessage('Product updated successfully');
        setShowNotification(true);
        setTimeout(() => {
          navigate(`/inventory/${inventoryId}/products`);
        }, 2000);
      }
    } catch (error) {
      const apiError = error as ApiError;
      setErrorMessage(`Error updating product: ${apiError.message}`);
    } finally {
      setLoading(false);
    }
  };

  // Handle input changes
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    setProduct({
      ...product,
      [e.target.name]:
          e.target.type === 'number'
              ? parseFloat(e.target.value)
              : e.target.value,
    });
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

        <br></br>

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

          <br></br>

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

          <br></br>

          <h6>Price</h6>
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

          <br></br>

          <h6>Quantity</h6>
          <div className="input-group mb-3">
            <input
                className="form-control"
                type="number"
                name="productQuantity"
                value={product.productQuantity}
                onChange={handleChange}
                required
            />
          </div>

          <br></br>

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
          {error && <p style={{ color: 'red' }}>{error.message}</p>}
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