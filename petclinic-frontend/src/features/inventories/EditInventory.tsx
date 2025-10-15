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
import EditInventoryFormStyles from './EditInventoryForm.module.css';

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

const EditInventory: React.FC = (): JSX.Element => {
  const { inventoryId } = useParams<{ inventoryId: string }>();
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

  useEffect(() => {
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
  }, [inventoryId]);

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

  // Cancel handler: discard local edits and close form (navigate back)
  const handleCancel = (): void => {
    if (originalInventoryRef.current) {
      setInventory(originalInventoryRef.current);
    }
    navigate('/inventories');
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

  return (
    <div className="edit-inventory-form">
      <h3 className="text-center">
        Inventories &nbsp;
        <small className="text-muted">Edit Form</small>
      </h3>
      {loading && <div className="loader">Loading...</div>}
      <br />
      <div className="container">
        <form onSubmit={handleSubmit} className="text-center">
          <div className="row">
            <div className="col-4">
              <div className="form-group">
                <label>Inventory Name</label>
                <input
                  type="text"
                  name="inventoryName"
                  className={`form-control ${fieldErrors.inventoryName ? 'invalid animate' : ''}`}
                  value={inventory.inventoryName}
                  onChange={e =>
                    handleFieldChange('inventoryName', e.target.value)
                  }
                  aria-invalid={!!fieldErrors.inventoryName}
                  aria-describedby={
                    fieldErrors.inventoryName ? 'err-inventoryName' : undefined
                  }
                />
                {fieldErrors.inventoryName && (
                  <span id="err-inventoryName" className="error">
                    {fieldErrors.inventoryName}
                  </span>
                )}
              </div>
            </div>
            <div className="col-4">
              <div className="form-group">
                <label>Inventory Type</label>
                <select
                  name="inventoryType"
                  className={`form-control ${fieldErrors.inventoryType ? 'invalid animate' : ''}`}
                  value={inventory.inventoryType}
                  onChange={e =>
                    handleFieldChange('inventoryType', e.target.value)
                  }
                  aria-invalid={!!fieldErrors.inventoryType}
                  aria-describedby={
                    fieldErrors.inventoryType ? 'err-inventoryType' : undefined
                  }
                  required
                >
                  <option value="" disabled>
                    Select inventory type
                  </option>
                  {inventoryTypes.map(type => (
                    <option key={type.typeId} value={type.type}>
                      {type.type}
                    </option>
                  ))}
                </select>
                {fieldErrors.inventoryType && (
                  <span id="err-inventoryType" className="error">
                    {fieldErrors.inventoryType}
                  </span>
                )}
              </div>
            </div>
            <div className="col-4">
              <div className="form-group">
                <label>Inventory Description</label>
                <input
                  type="text"
                  name="inventoryDescription"
                  className={`form-control ${fieldErrors.inventoryDescription ? 'invalid animate' : ''}`}
                  placeholder="Inventory Description"
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
                  <span id="err-inventoryDescription" className="error">
                    {fieldErrors.inventoryDescription}
                  </span>
                )}
              </div>
            </div>
            <div className="col-4">
              <div className="form-group">
                <label>
                  <div className={EditInventoryFormStyles.labelContainer}>
                    Inventory Image
                  </div>
                </label>
                <input
                  type="text"
                  name="inventoryImage"
                  className={`form-control ${fieldErrors.inventoryImage ? 'invalid animate' : ''}`}
                  placeholder="Inventory Image"
                  value={inventory.inventoryImage}
                  onChange={e =>
                    handleFieldChange('inventoryImage', e.target.value)
                  }
                  aria-invalid={!!fieldErrors.inventoryImage}
                  aria-describedby={
                    fieldErrors.inventoryImage
                      ? 'err-inventoryImage'
                      : undefined
                  }
                />
                {fieldErrors.inventoryImage && (
                  <span id="err-inventoryImage" className="error">
                    {fieldErrors.inventoryImage}
                  </span>
                )}
              </div>
            </div>
            <div className="col-4">
              <div className="form-group">
                <div className={EditInventoryFormStyles.labelContainer}>
                  <label>Inventory Backup Image</label>
                </div>
                <input
                  type="text"
                  name="inventoryBackupImage"
                  className={`form-control ${fieldErrors.inventoryBackupImage ? 'invalid animate' : ''}`}
                  placeholder="Inventory Backup Image"
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
                />
                {fieldErrors.inventoryBackupImage && (
                  <span id="err-inventoryBackupImage" className="error">
                    {fieldErrors.inventoryBackupImage}
                  </span>
                )}
              </div>
            </div>
            <div className="col-4">
              <div className="form-group">
                <div className={EditInventoryFormStyles.labelContainer}>
                  <label>Upload Image:</label>
                </div>
                <input
                  type="file"
                  name="uploadedImage"
                  className={`form-control ${fieldErrors.uploadedImage ? 'invalid animate' : ''}`}
                  accept="image/*"
                  onChange={handleFileChange}
                  ref={fileInputRef}
                  aria-invalid={!!fieldErrors.uploadedImage}
                  aria-describedby={
                    fieldErrors.uploadedImage ? 'err-uploadedImage' : undefined
                  }
                />
                {fieldErrors.uploadedImage && (
                  <span id="err-uploadedImage" className="error">
                    {fieldErrors.uploadedImage}
                  </span>
                )}
                {previewSrc && (
                  <div style={{ marginTop: 8 }}>
                    <img
                      src={previewSrc}
                      alt="Preview"
                      style={{ maxWidth: 120, maxHeight: 120, borderRadius: 4 }}
                    />
                  </div>
                )}
              </div>
            </div>
          </div>
          <br />
          <div className="row">
            <button type="submit" className="btn btn-info">
              Update
            </button>
            {/* Cancel button: discard edits and close form */}
            <button
              type="button"
              className="btn btn-light"
              onClick={handleCancel}
            >
              Cancel
            </button>
            {/* Undo button */}
            <button
              type="button"
              className="btn btn-secondary"
              onClick={handleUndo}
            >
              Undo
            </button>
          </div>
        </form>
      </div>
      {successMessage && <p className="success-message">{successMessage}</p>}
      {errorMessage && <p className="error-message">{errorMessage}</p>}
      {showNotification && (
        <div className="notification">
          <p>Inventory updated successfully</p>
        </div>
      )}
    </div>
  );
};
export default EditInventory;
