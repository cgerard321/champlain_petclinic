import * as React from 'react';
import { FormEvent, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getInventory,
  updateInventory,
} from '@/features/inventories/api/EditInventory.ts';
import { InventoryRequestModel } from '@/features/inventories/models/InventoryModels/InventoryRequestModel.ts';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import { getAllInventoryTypes } from '@/features/inventories/api/getAllInventoryTypes.ts';

import styles from './InvProForm.module.css';

type EditInventoryProps = {
  open?: boolean;
  onClose?: () => void;
  inventoryIdProp?: string;
};
// interface ApiError {
//   message: string;
// }

const MAX_IMAGE_BYTES = 160 * 1024;

function base64ByteLength(b64: string): number {
  const pure = b64.includes(',') ? b64.split(',')[1] : b64;
  const len = pure.length;
  const padding = pure.endsWith('==') ? 2 : pure.endsWith('=') ? 1 : 0;
  return (len * 3) / 4 - padding;
}

const isHttpUrl = (url: string): boolean => {
  try {
    const u = new URL(url);
    return u.protocol === 'http:' || u.protocol === 'https:';
  } catch {
    return false;
  }
};

// Fields that support undo
type FieldKey =
  | 'inventoryName'
  | 'inventoryType'
  | 'inventoryDescription'
  | 'inventoryImage'
  | 'inventoryBackupImage'
  | 'uploadedImage';

type TextFieldKey =
  | 'inventoryName'
  | 'inventoryType'
  | 'inventoryDescription'
  | 'inventoryImage'
  | 'inventoryBackupImage';

function buildInventoryFieldErrorMessage(params: {
  inventoryName: string;
  inventoryType: string;
  inventoryDescription: string;
  inventoryImage: string;
  inventoryBackupImage: string;
  imageUploaded?: string;
}): Partial<Record<FieldKey, string>> {
  const {
    inventoryName,
    inventoryType,
    inventoryDescription,
    inventoryImage,
    inventoryBackupImage,
    imageUploaded,
  } = params;

  const next: Partial<Record<FieldKey, string>> = {};

  const nameTrim = inventoryName.trim();
  if (!nameTrim) next.inventoryName = 'Inventory name is required.';
  else if (nameTrim.length < 3)
    next.inventoryName = 'Name must be at least 3 characters.';

  const typeTrim = inventoryType.trim();
  if (!typeTrim) next.inventoryType = 'Inventory type is required.';

  const descTrim = inventoryDescription.trim();
  if (!descTrim) next.inventoryDescription = 'Description is required.';

  if (inventoryImage && !isHttpUrl(inventoryImage)) {
    next.inventoryImage = 'Must be a valid http/https URL.';
  }
  if (inventoryBackupImage && !isHttpUrl(inventoryBackupImage)) {
    next.inventoryBackupImage = 'Must be a valid http/https URL.';
  }
  if (imageUploaded && base64ByteLength(imageUploaded) > MAX_IMAGE_BYTES) {
    next.uploadedImage = 'Image too large (max 160KB).';
  }
  return next;
}

const EditInventory: React.FC<EditInventoryProps> = ({
  open = true,
  onClose,
  inventoryIdProp,
}: EditInventoryProps): JSX.Element | null => {
  const params = useParams<{ inventoryId: string }>();
  const inventoryId = inventoryIdProp ?? params.inventoryId;

  const [inventory, setInventory] = useState<InventoryRequestModel>({
    inventoryName: '',
    inventoryType: '',
    inventoryDescription: '',
    inventoryImage: '',
    inventoryBackupImage: '',
    imageUploaded: '',
  });
  const [inventoryTypes, setInventoryTypes] = useState<InventoryType[]>([]);
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<FieldKey, string>>
  >({});
  const [showNotification, setShowNotification] = useState<boolean>(false);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  // Undo state
  const [history, setHistory] = useState<Record<TextFieldKey, string[]>>({
    inventoryName: [''],
    inventoryType: [''],
    inventoryDescription: [''],
    inventoryImage: [''],
    inventoryBackupImage: [''],
  });
  const [lastEditedFields, setLastEditedFields] = useState<TextFieldKey[]>([]);

  // Keep the original loaded inventory so Cancel can discard edits
  const originalInventoryRef = useRef<InventoryRequestModel | null>(null);

  const navigate = useNavigate();

  const handleCancel = React.useCallback((): void => {
    if (originalInventoryRef.current) {
      setInventory(originalInventoryRef.current);
    }
    onClose?.();
    navigate('/inventories');
  }, [onClose, navigate]);

  useEffect(() => {
    const prev = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = prev;
    };
  }, []);

  useEffect(() => {
    const root = document.getElementById('app-root');
    if (!root) return;
    root.setAttribute('inert', '');
    return () => root.removeAttribute('inert');
  }, []);

  useEffect(() => {
    if (!open) return;

    const onEsc = (e: KeyboardEvent): void => {
      if (e.key === 'Escape' || e.key === 'Esc') {
        e.preventDefault?.();
        handleCancel();
      }
    };

    document.addEventListener('keydown', onEsc);
    return () => document.removeEventListener('keydown', onEsc);
  }, [open, handleCancel]);

  const overlayRef = useRef<HTMLDivElement | null>(null);

  const handleOverlayKeyDown: React.KeyboardEventHandler<
    HTMLDivElement
  > = e => {
    if (e.key === 'Escape' || e.key === 'Esc') {
      e.preventDefault();
      handleCancel();
    }
  };

  useEffect(() => {
    if (open) {
      requestAnimationFrame(() => overlayRef.current?.focus());
    }
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const fetchInventoryData = async (): Promise<void> => {
      if (!inventoryId) return;

      const res = await getInventory(inventoryId);
      if (res.errorMessage || !res.data) {
        setErrorMessage(res.errorMessage ?? 'Unable to load inventory.');
        return;
      }

      const response = res.data;
      setInventory({
        inventoryName: response.inventoryName,
        inventoryType: response.inventoryType,
        inventoryDescription: response.inventoryDescription,
        inventoryImage: response.inventoryImage,
        inventoryBackupImage: response.inventoryBackupImage,
        imageUploaded: response.imageUploaded,
      });

      // store original values so Cancel can restore them
      originalInventoryRef.current = {
        inventoryName: response.inventoryName,
        inventoryType: response.inventoryType,
        inventoryDescription: response.inventoryDescription,
        inventoryImage: response.inventoryImage,
        inventoryBackupImage: response.inventoryBackupImage,
        imageUploaded: response.imageUploaded || '',
      };

      // Initialize undo history with loaded values
      setHistory({
        inventoryName: [response.inventoryName],
        inventoryType: [response.inventoryType],
        inventoryDescription: [response.inventoryDescription],
        inventoryImage: [response.inventoryImage],
        inventoryBackupImage: [response.inventoryBackupImage],
      });
      setFieldErrors({});
      setErrorMessage('');
      setSuccessMessage('');
      setShowNotification(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    };

    const fetchInventoryTypes = async (): Promise<void> => {
      const res = await getAllInventoryTypes();
      if (res.errorMessage) {
        setErrorMessage(prev => prev || res.errorMessage || '');
      }
      setInventoryTypes(res.data ?? []);
    };

    void (async () => {
      await Promise.all([fetchInventoryData(), fetchInventoryTypes()]);
    })();
  }, [inventoryId, open]);

  // Word-count helper
  const countWords = (s: string): number => {
    const trimmed = s.trim();
    if (trimmed === '') return 0;
    return trimmed.split(/\s+/).filter(Boolean).length;
  };

  // Push snapshots at word boundaries
  const handleFieldChange = (field: TextFieldKey, value: string): void => {
    setHistory(prev => {
      const fieldHist = prev[field] ?? [''];
      const lastRecorded = fieldHist[fieldHist.length - 1] ?? '';

      const isWordBoundary =
        value.endsWith(' ') ||
        value.trim() === '' ||
        countWords(value) < countWords(lastRecorded);

      if (isWordBoundary && value !== lastRecorded) {
        return {
          ...prev,
          [field]: [...fieldHist, value],
        };
      }
      return prev;
    });

    setLastEditedFields(prev => {
      const updated = prev.filter(f => f !== field);
      return [...updated, field];
    });

    setFieldErrors(prev => ({ ...prev, [field]: undefined }));

    if (field === 'inventoryImage') {
      setInventory(prev => ({
        ...prev,
        inventoryImage: value,
        imageUploaded: '',
      }));
    } else {
      setInventory(prev => ({ ...prev, [field]: value }));
    }
  };

  // Undo handler
  const handleUndo = (): void => {
    const order = [...lastEditedFields];

    while (order.length > 0) {
      const candidate = order[order.length - 1] as TextFieldKey;
      const fieldHist = history[candidate];

      if (fieldHist && fieldHist.length > 1) {
        const newHist = fieldHist.slice(0, -1);
        const restoredValue = newHist[newHist.length - 1] ?? '';

        setHistory(prev => ({
          ...prev,
          [candidate]: newHist,
        }));

        setLastEditedFields(prev => {
          const filtered = prev.filter(f => f !== candidate);
          if (newHist.length > 1) {
            return [...filtered, candidate];
          }
          return filtered;
        });

        setInventory({ ...inventory, [candidate]: restoredValue });
        return;
      }

      order.pop();
    }
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    const errorsBefore = buildInventoryFieldErrorMessage({
      inventoryName: inventory.inventoryName,
      inventoryType: inventory.inventoryType,
      inventoryDescription: inventory.inventoryDescription,
      inventoryImage: inventory.inventoryImage,
      inventoryBackupImage: inventory.inventoryBackupImage,
      imageUploaded:
        typeof inventory.imageUploaded === 'string'
          ? inventory.imageUploaded
          : undefined,
    });
    const typeExists = inventoryTypes.some(
      t => t.type === inventory.inventoryType
    );
    if (!typeExists) {
      errorsBefore.inventoryType =
        errorsBefore.inventoryType ?? 'Please select a valid inventory type.';
    }
    if (Object.keys(errorsBefore).length) {
      setFieldErrors(errorsBefore);
      return;
    }

    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');
    setShowNotification(false);

    try {
      const res = inventoryId
        ? await updateInventory(inventoryId, inventory)
        : { data: undefined, errorMessage: 'No inventory ID' };

      setLoading(false);

      if (res.errorMessage) {
        const msg = res.errorMessage.toLowerCase();

        if (msg.includes('already exists') || msg.includes('same name')) {
          setFieldErrors(prev => ({
            ...prev,
            inventoryName:
              prev.inventoryName ||
              'An inventory with this name already exists.',
          }));
          return;
        }

        if (msg.includes('uploaded image') || msg.includes('160kb')) {
          setFieldErrors(prev => ({
            ...prev,
            uploadedImage: 'Image too large (max 160KB).',
          }));
          return;
        }
        if (msg.includes('invalid inventory data')) {
          const errorsAfter = buildInventoryFieldErrorMessage({
            inventoryName: inventory.inventoryName,
            inventoryType: inventory.inventoryType,
            inventoryDescription: inventory.inventoryDescription,
            inventoryImage: inventory.inventoryImage,
            inventoryBackupImage: inventory.inventoryBackupImage,
            imageUploaded:
              typeof inventory.imageUploaded === 'string'
                ? inventory.imageUploaded
                : undefined,
          });
          if (Object.keys(errorsAfter).length) {
            setFieldErrors(prev => ({ ...prev, ...errorsAfter }));
          }
        }
        setErrorMessage(res.errorMessage || 'Failed to update inventory.');
        return;
      }

      setSuccessMessage('Inventory updated successfully');
      setShowNotification(true);
      setTimeout(() => {
        if (onClose) onClose();
        navigate('/inventories');
      }, 2000);
    } finally {
      setLoading(false);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Clear the URL image via the same path as other text edits (so it's undoable)
    handleFieldChange('inventoryImage', '');

    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onloadend = () => {
      if (!reader.result) return;

      const base64String = (reader.result as string).split(',')[1];
      const byteLength = base64ByteLength(base64String);

      if (byteLength > MAX_IMAGE_BYTES) {
        setFieldErrors(prev => ({
          ...prev,
          uploadedImage: 'Image too large (max 160KB).',
        }));
        // keep imageUploaded empty
        setInventory(prev => ({ ...prev, imageUploaded: '' }));
        if (fileInputRef.current) fileInputRef.current.value = '';
        return;
      }

      setInventory(prev => ({ ...prev, imageUploaded: base64String }));
      setFieldErrors(prev => ({ ...prev, uploadedImage: undefined }));
    };
  };

  const previewSrc = inventory.imageUploaded
    ? `data:image/*;base64,${inventory.imageUploaded}`
    : isHttpUrl(inventory.inventoryImage)
      ? inventory.inventoryImage
      : '';

  if (!open) return null;

  const handleOverlayMouseDown: React.MouseEventHandler<HTMLDivElement> = e => {
    if (e.target === e.currentTarget) handleCancel();
  };

  return (
    <div
      ref={overlayRef}
      className={styles.overlay}
      role="dialog"
      aria-modal="true"
      tabIndex={-1}
      onKeyDown={handleOverlayKeyDown}
      onMouseDown={handleOverlayMouseDown}
    >
      {/* use the same container class as Add form */}
      <div className={styles['form-container']}>
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
        <h2>Edit Inventory</h2>

        {loading && (
          <div
            style={{
              position: 'absolute',
              inset: 0,
              background: 'rgba(255,255,255,0.6)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              borderRadius: 8,
              zIndex: 1,
            }}
          >
            <div className="loader">Loading...</div>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Inventory Name */}
          <div>
            <label htmlFor="edit-inventoryName">Inventory Name:</label>
            <input
              id="edit-inventoryName"
              type="text"
              className={fieldErrors.inventoryName ? 'invalid animate' : ''}
              value={inventory.inventoryName}
              onChange={e => handleFieldChange('inventoryName', e.target.value)}
              aria-invalid={!!fieldErrors.inventoryName}
              aria-describedby={
                fieldErrors.inventoryName ? 'err-inventoryName' : undefined
              }
            />
            {fieldErrors.inventoryName && (
              <div id="err-inventoryName" className="field-error">
                {fieldErrors.inventoryName}
              </div>
            )}
          </div>

          {/* Inventory Type */}
          <div>
            <label htmlFor="edit-inventoryType">Inventory Type:</label>
            <select
              id="edit-inventoryType"
              className={fieldErrors.inventoryType ? 'invalid animate' : ''}
              value={inventory.inventoryType}
              onChange={e => handleFieldChange('inventoryType', e.target.value)}
              aria-invalid={!!fieldErrors.inventoryType}
              aria-describedby={
                fieldErrors.inventoryType ? 'err-inventoryType' : undefined
              }
              required
            >
              <option value="">Select Type</option>
              {inventoryTypes.map(t => (
                <option key={t.typeId} value={t.type}>
                  {t.type}
                </option>
              ))}
            </select>
            {fieldErrors.inventoryType && (
              <div id="err-inventoryType" className="field-error">
                {fieldErrors.inventoryType}
              </div>
            )}
          </div>

          {/* Inventory Description */}
          <div>
            <label htmlFor="edit-inventoryDescription">
              Inventory Description:
            </label>
            <input
              id="edit-inventoryDescription"
              type="text"
              className={
                fieldErrors.inventoryDescription ? 'invalid animate' : ''
              }
              value={inventory.inventoryDescription}
              onChange={e =>
                handleFieldChange('inventoryDescription', e.target.value)
              }
              aria-invalid={!!fieldErrors.inventoryDescription}
              aria-describedby={
                fieldErrors.inventoryDescription
                  ? 'err-inventoryDescription'
                  : undefined
              }
              required
            />
            {fieldErrors.inventoryDescription && (
              <div id="err-inventoryDescription" className="field-error">
                {fieldErrors.inventoryDescription}
              </div>
            )}
          </div>

          {/* Inventory Image */}
          <div>
            <label htmlFor="edit-inventoryImage">Inventory Image:</label>
            <input
              id="edit-inventoryImage"
              type="text"
              className={fieldErrors.inventoryImage ? 'invalid animate' : ''}
              value={inventory.inventoryImage}
              onChange={e =>
                handleFieldChange('inventoryImage', e.target.value)
              }
              aria-invalid={!!fieldErrors.inventoryImage}
              aria-describedby={
                fieldErrors.inventoryImage ? 'err-inventoryImage' : undefined
              }
              placeholder="http(s)://…"
            />
            {fieldErrors.inventoryImage && (
              <div id="err-inventoryImage" className="field-error">
                {fieldErrors.inventoryImage}
              </div>
            )}
          </div>

          {/* Backup Image */}
          <div>
            <label htmlFor="edit-inventoryBackupImage">
              Inventory Backup Image:
            </label>
            <input
              id="edit-inventoryBackupImage"
              type="text"
              className={
                fieldErrors.inventoryBackupImage ? 'invalid animate' : ''
              }
              value={inventory.inventoryBackupImage}
              onChange={e =>
                handleFieldChange('inventoryBackupImage', e.target.value)
              }
              aria-invalid={!!fieldErrors.inventoryBackupImage}
              aria-describedby={
                fieldErrors.inventoryBackupImage
                  ? 'err-inventoryBackupImage'
                  : undefined
              }
              placeholder="http(s)://…"
            />
            {fieldErrors.inventoryBackupImage && (
              <div id="err-inventoryBackupImage" className="field-error">
                {fieldErrors.inventoryBackupImage}
              </div>
            )}
          </div>

          {/* Upload Image */}
          <div>
            <label htmlFor="edit-uploadedImage">Upload Image:</label>
            <input
              id="edit-uploadedImage"
              type="file"
              accept="image/*"
              onChange={handleFileChange}
              ref={fileInputRef}
              className={fieldErrors.uploadedImage ? 'invalid animate' : ''}
              aria-invalid={!!fieldErrors.uploadedImage}
              aria-describedby={
                fieldErrors.uploadedImage ? 'err-uploadedImage' : undefined
              }
            />
            {fieldErrors.uploadedImage && (
              <div id="err-uploadedImage" className="field-error">
                {fieldErrors.uploadedImage}
              </div>
            )}
          </div>

          {/* Optional preview (same style as Add) */}
          {previewSrc && (
            <div style={{ marginTop: 8 }}>
              <img
                src={previewSrc}
                alt="Preview"
                style={{ maxWidth: '100%', borderRadius: 4 }}
              />
            </div>
          )}

          {/* Server error + actions */}
          {errorMessage && (
            <div className="field-error" style={{ marginTop: 8 }}>
              {errorMessage}
            </div>
          )}

          <button type="submit">Update</button>
          <button type="button" className="cancel" onClick={handleCancel}>
            Cancel
          </button>
          <button type="button" className="undo" onClick={handleUndo}>
            Undo
          </button>
        </form>
      </div>
    </div>
  );
};
export default EditInventory;
