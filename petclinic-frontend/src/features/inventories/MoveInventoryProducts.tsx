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
  const [fetchError, setFetchError] = useState<string | null>(null);
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
        const msg =
          err instanceof Error ? err.message : 'Error fetching inventories';
        setFetchError(msg);
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

    if (!newInventoryId) {
      setErrorMessage('Please select a destination inventory.');
      return;
    }
    if (inventoryId && newInventoryId === inventoryId) {
      setErrorMessage(
        'Please choose a different inventory than the current one.'
      );
      return;
    }
    if (!productId || !inventoryId) {
      setErrorMessage('Missing product or inventory id.');
      return;
    }

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
          navigate(`/inventories/${newInventoryId}/products`);
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

    if (errorMessage) setErrorMessage('');
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
            value={newInventoryId}
            onChange={handleNewInventoryChange}
            disabled={loading}
          >
            <option value="">Select inventory</option>
            {filteredInventories.map(inventory => (
              <option key={inventory.inventoryId} value={inventory.inventoryId}>
                {inventory.inventoryName}
              </option>
            ))}
          </select>
          {filteredInventories.length === 0 && !loading && (
            <small style={{ color: '#444' }}>
              No other inventories available.
            </small>
          )}
        </div>
        <div style={{ display: 'flex', gap: '12px', marginTop: '12px' }}>
          <button
            type="submit"
            disabled={!newInventoryId || loading}
            style={{
              width: '100px',
              backgroundColor: '#333',
              color: 'white',
            }}
          >
            {loading ? 'Moving...' : 'Move'}
          </button>

          <button
            type="button"
            onClick={() => navigate(`/inventories/${inventoryId}/products`)}
            style={{
              width: '100px',
              backgroundColor: '#ff0000ff',
              color: 'white',
            }}
          >
            Cancel
          </button>
        </div>
      </form>
      <div>
        {loading && <p>Loading...</p>}
        {fetchError && <p style={{ color: 'red' }}>{fetchError}</p>}{' '}
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
