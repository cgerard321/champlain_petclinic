import { useEffect, useState } from 'react';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import addInventoryType from '@/features/inventories/api/addInventoryType.ts';
import './AddInventoryType.css';

interface AddInventoryTypeProps {
  show: boolean;
  handleClose: () => void;
  refreshInventoryTypes: () => void;
}

export default function AddInventoryType({
  show,
  handleClose,
  refreshInventoryTypes,
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
    setIsSubmitting(true);

    try {
      const newInventoryType: Omit<InventoryType, 'typeId'> = {
        type: trimmed,
      };
      await addInventoryType(newInventoryType);
      refreshInventoryTypes(); // Call this function to update the list
      handleClose();
    } catch (error) {
      setFieldError(
        error instanceof Error
          ? error.message
          : 'Failed to add inventory type. Please try again.'
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!show) return null; // Return null when `show` is false

  return (
    <div className="overlay">
      <div className="form-container">
        <h2>Add Inventory Type</h2>
        <form onSubmit={handleSubmit}>
          <div>
            <label htmlFor="type">Type Name</label>
            <input
              type="text"
              id="type"
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
              <div
                id="type-error"
                className="field-error"
                style={{ color: 'red', marginTop: 6 }}
              >
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
    </div>
  );
}
