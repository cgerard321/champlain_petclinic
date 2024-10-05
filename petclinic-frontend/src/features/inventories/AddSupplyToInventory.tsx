import * as React from 'react';
import { FormEvent, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { addSupplyToInventory } from '@/features/inventories/api/AddSupplyToInventory.ts';
import { ProductRequestModel } from '@/features/inventories/models/InventoryModels/ProductRequestModel';

interface ApiError {
  message: string;
}

const AddSupplyToInventory: React.FC = (): JSX.Element => {
  const { inventoryId } = useParams<{ inventoryId: string }>(); // Get params from URL
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
          navigate(`/inventory/${inventoryId}/products`);
        }, 2000);
      }
    } catch (error) {
      const apiError = error as ApiError;
      setErrorMessage(`Error adding supply: ${apiError.message}`);
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
    <div>
      <h1>Add Supply</h1>
      {error && <p style={{ color: 'red' }}>{error.message}</p>}

      <form onSubmit={handleSubmit}>
        <div>
          <label>Product Name</label>
          <input
            type="text"
            name="productName"
            value={product.productName}
            onChange={handleChange}
            required
          />
        </div>

        <div>
          <label>Product Description</label>
          <input
            type="text"
            name="productDescription"
            value={product.productDescription}
            onChange={handleChange}
            required
          />
        </div>

        <div>
          <label>Product Price</label>
          <input
            type="number"
            name="productPrice"
            value={product.productPrice}
            onChange={handleChange}
            required
          />
        </div>

        <div>
          <label>Product Quantity</label>
          <input
            type="number"
            name="productQuantity"
            value={product.productQuantity}
            onChange={handleChange}
            required
          />
        </div>

        <div>
          <label>Product Sale Price</label>
          <input
            type="number"
            name="productSalePrice"
            value={product.productSalePrice}
            onChange={handleChange}
            required
          />
        </div>

        <button type="submit">Add Supply</button>
      </form>
      <div>
        {loading && <p>Loading...</p>}
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

export default AddSupplyToInventory;
