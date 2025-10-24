import * as React from 'react';
import { useEffect, useRef, useState } from 'react';
import {
  getAllInventories,
  updateProductInventoryId,
} from '@/features/inventories/api/moveInventoryProduct.ts';
import styles from './InvProForm.module.css';

type InventoryLite = { inventoryId: string; inventoryName: string };

type Props = {
  open: boolean;
  onClose: () => void;
  inventoryIdProp: string;
  productIdProp: string;
  onMoved?: (newInventoryId: string) => void;
};

const MoveInventoryProducts: React.FC<Props> = ({
  open,
  onClose,
  inventoryIdProp,
  productIdProp,
  onMoved,
}) => {
  const [inventories, setInventories] = useState<InventoryLite[]>([]);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [showNotification, setShowNotification] = useState<boolean>(false);
  const [newInventoryId, setNewInventoryId] = useState<string>('');

  const overlayRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!open) return;
    const run = async (): Promise<void> => {
      setLoading(true);
      setFetchError(null);
      const res = await getAllInventories();
      if (res.errorMessage) {
        setFetchError(res.errorMessage);
      }
      const list =
        res.data?.map(i => ({
          inventoryId: i.inventoryId,
          inventoryName: i.inventoryName,
        })) ?? [];
      setInventories(list);
      setLoading(false);
      requestAnimationFrame(() => overlayRef.current?.focus());
    };
    void run();
  }, [open]);

  const handleSubmit: React.FormEventHandler<HTMLFormElement> = async event => {
    event.preventDefault();
    setErrorMessage('');
    setSuccessMessage('');
    setShowNotification(false);

    if (!newInventoryId) {
      setErrorMessage('Please select a destination inventory.');
      return;
    }
    if (newInventoryId === inventoryIdProp) {
      setErrorMessage(
        'Please choose a different inventory than the current one.'
      );
      return;
    }

    setLoading(true);
    const res = await updateProductInventoryId(
      inventoryIdProp,
      productIdProp,
      newInventoryId
    );
    setLoading(false);

    if (res.errorMessage) {
      setErrorMessage(res.errorMessage);
      return;
    }

    setSuccessMessage('Product moved successfully');
    setShowNotification(true);
    onMoved?.(newInventoryId);
    setTimeout(() => onClose(), 800);
  };

  const handleNewInventoryChange = (
    event: React.ChangeEvent<HTMLSelectElement>
  ): void => {
    setNewInventoryId(event.target.value);
    if (errorMessage) setErrorMessage('');
  };

  const handleEsc: React.KeyboardEventHandler<HTMLDivElement> = e => {
    if (e.key === 'Escape' || e.key === 'Esc') onClose();
  };
  const onBackdrop: React.MouseEventHandler<HTMLDivElement> = e => {
    if (e.target === e.currentTarget) onClose();
  };

  if (!open) return null;

  const filteredInventories = inventories.filter(
    inv => inv.inventoryId !== inventoryIdProp
  );

  return (
    <div
      ref={overlayRef}
      className={styles.overlay}
      role="dialog"
      aria-modal="true"
      tabIndex={-1}
      onKeyDown={handleEsc}
      onMouseDown={onBackdrop}
    >
      <div className={styles['form-container']}>
        <h2>Move Product to New Inventory</h2>

        {loading && (
          <div className="loading-overlay">
            <div className="loader" />
          </div>
        )}

        {fetchError && (
          <div className="field-error" style={{ marginTop: 8 }}>
            {fetchError}
          </div>
        )}
        {errorMessage && (
          <div className="field-error" style={{ marginTop: 8 }}>
            {errorMessage}
          </div>
        )}
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

        <form onSubmit={handleSubmit}>
          <div>
            <label htmlFor="move-newInventorySelect">
              Select New Inventory
            </label>
            <select
              id="move-newInventorySelect"
              className="form-control"
              value={newInventoryId}
              onChange={handleNewInventoryChange}
              disabled={loading}
            >
              <option value="">Select inventory</option>
              {filteredInventories.map(inv => (
                <option key={inv.inventoryId} value={inv.inventoryId}>
                  {inv.inventoryName}
                </option>
              ))}
            </select>
            {filteredInventories.length === 0 && !loading && (
              <small style={{ color: '#444' }}>
                No other inventories available.
              </small>
            )}
          </div>

          <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
            <button
              type="submit"
              disabled={!newInventoryId || loading}
              style={{ width: 100 }}
            >
              {loading ? 'Moving…' : 'Move'}
            </button>
            <button
              type="button"
              className="cancel"
              onClick={onClose}
              style={{ width: 100 }}
            >
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
export default MoveInventoryProducts;
