import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getAllInventories,
  updateProductInventoryId,
} from '@/features/inventories/api/moveInventoryProduct.ts';

interface ApiError {
  message: string;
}

export default function MoveInventoryProducts(): JSX.Element {
  const { inventoryId, productId } = useParams<{
    inventoryId: string;
    productId: string;
  }>();
  const [inventories, setInventories] = useState<
    { inventoryId: string; inventoryName: string }[]
  >([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [showNotification, setShowNotification] = useState<boolean>(false);
  const [newInventoryId, setNewInventoryId] = useState<string>('');

  const navigate = useNavigate();

  useEffect(() => {
    const fetchInventories = async (): Promise<void> => {
      try {
        const response = await getAllInventories();
        setInventories(response);
      } catch (err) {
        setError('Error fetching inventories');
      } finally {
        setLoading(false);
      }
    };

    fetchInventories();
  }, []);

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');
    setShowNotification(false);

    try {
      if (productId && newInventoryId && inventoryId) {
        await updateProductInventoryId(inventoryId, productId, newInventoryId);
        setSuccessMessage('Product updated successfully');
        setShowNotification(true);
        setTimeout(() => {
          navigate(`/inventory/${newInventoryId}/products`);
        }, 2000);
      }
    } catch (error) {
      const apiError = error as ApiError;
      setErrorMessage(`Error updating product: ${apiError.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleNewInventoryChange = (
    event: React.ChangeEvent<HTMLSelectElement>
  ): void => {
    setNewInventoryId(event.target.value);
  };

  const filteredInventories = inventories.filter(
    inventory => inventory.inventoryId !== inventoryId
  );

  return (
    <div
      className="card d-flex justify-content-center align-items-center"
      style={{
        width: '500px',
        height: '350px',
        backgroundColor: 'lightgray',
        margin: '0 auto',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
      }}
    >
      <h2>Move Product to New Inventory</h2>
      <br />
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="newInventorySelect">Select New Inventory</label>
          <select
            className="form-control"
            id="newInventorySelect"
            onChange={handleNewInventoryChange}
          >
            {filteredInventories.map(inventory => (
              <option key={inventory.inventoryId} value={inventory.inventoryId}>
                {inventory.inventoryName}
              </option>
            ))}
          </select>
        </div>
        <button
          type="submit"
          style={{
            width: '100px',
            backgroundColor: '#333',
            color: 'white',
          }}
        >
          Move
        </button>
      </form>
      <div>
        {loading && <p>Loading...</p>}
        {error && <p style={{ color: 'red' }}>{error}</p>}{' '}
        {/* Error message directly */}
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
}
