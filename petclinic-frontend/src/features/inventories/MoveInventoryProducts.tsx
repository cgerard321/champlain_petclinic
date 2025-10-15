import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getAllInventories,
  updateProductInventoryId,
} from '@/features/inventories/api/moveInventoryProduct.ts';

type InventoryLite = { inventoryId: string; inventoryName: string };

export default function MoveInventoryProducts(): JSX.Element {
  const { inventoryId, productId } = useParams<{
    inventoryId: string;
    productId: string;
  }>();
  const [inventories, setInventories] = useState<InventoryLite[]>([]);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [showNotification, setShowNotification] = useState<boolean>(false);
  const [newInventoryId, setNewInventoryId] = useState<string>('');

  const navigate = useNavigate();

  useEffect(() => {
    const fetchInventories = async (): Promise<void> => {
      setLoading(true);
      const res = await getAllInventories(); // ApiResponse<InventoryResponseModel[]>
      if (res.errorMessage) setFetchError(res.errorMessage);
      const list =
        res.data?.map(i => ({
          inventoryId: i.inventoryId,
          inventoryName: i.inventoryName,
        })) ?? [];
      setInventories(list);
      setLoading(false);
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

    const res = await updateProductInventoryId(
      inventoryId,
      productId,
      newInventoryId
    );
    setLoading(false);

    if (res.errorMessage) {
      setErrorMessage(res.errorMessage);
      return;
    }

    setSuccessMessage('Product moved successfully');
    setShowNotification(true);
    setTimeout(() => {
      navigate(`/inventories/${newInventoryId}/products`);
    }, 2000);
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
