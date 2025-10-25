import { useEffect, useState } from 'react';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import addInventoryType from '@/features/inventories/api/addInventoryType.ts';
import styles from './InvProForm.module.css';
import { createPortal } from 'react-dom';

interface AddInventoryTypeProps {
  show: boolean;
  handleClose: () => void;
  refreshInventoryTypes: () => void;
  existingTypeNames?: string[];
}

export default function AddInventoryType({
  show,
  handleClose,
  refreshInventoryTypes,
  existingTypeNames,
}: AddInventoryTypeProps): React.ReactElement | null {
  const [type, setType] = useState('');
  const [fieldError, setFieldError] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (show) {
      setType('');
      setFieldError('');
      setIsSubmitting(false);
    }
  }, [show]);

  const handleSubmit = async (event: React.FormEvent): Promise<void> => {
    event.preventDefault();
    setFieldError('');

    const trimmed = type.trim();

    if (!trimmed) {
      setFieldError('Type name is required.');
      return;
    }
    if (trimmed.length < 3 || trimmed.length > 50) {
      setFieldError('Type name must be between 3 and 50 characters.');
      return;
    }

    if (
      existingTypeNames?.some(
        n => n.trim().toLowerCase() === trimmed.toLowerCase()
      )
    ) {
      setFieldError('This inventory type already exists.');
      return;
    }

    setIsSubmitting(true);

    const payload: Omit<InventoryType, 'typeId'> = { type: trimmed };
    const { errorMessage } = await addInventoryType(payload);

    if (errorMessage) {
      setFieldError(errorMessage);
      setIsSubmitting(false);
      return;
    }

    await refreshInventoryTypes();
    handleClose();
    setIsSubmitting(false);
  };

  if (!show) return null; // Return null when `show` is false

  return createPortal(
    <div className={styles.overlay}>
      <div className={styles['form-container']}>
        <h2>Add Inventory Type</h2>
        <form onSubmit={handleSubmit}>
          <div>
            <label htmlFor="type">Type Name</label>
            <input
              type="text"
              id="type"
              className={fieldError ? 'invalid animate' : ''}
              value={type}
              onChange={e => {
                setType(e.target.value);
                if (fieldError) setFieldError('');
              }}
              required
              aria-invalid={!!fieldError}
              aria-describedby={fieldError ? 'type-error' : undefined}
            />
            {fieldError && (
              <div id="type-error" className="field-error">
                {fieldError}
              </div>
            )}
          </div>
          <button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Adding...' : 'Add'}
          </button>
          <button type="button" className="cancel" onClick={handleClose}>
            Cancel
          </button>
        </form>
      </div>
    </div>,
    document.body
  );
}
