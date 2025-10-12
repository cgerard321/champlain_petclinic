import { useState, useEffect } from 'react';
import { Inventory } from './models/Inventory';
import { getAllInventoryTypes } from '@/features/inventories/api/getAllInventoryTypes.ts';
import addInventory from '@/features/inventories/api/addInventory.ts';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import './AddInventoryForm.css';

interface AddInventoryProps {
  showAddInventoryForm: boolean;
  handleInventoryClose: () => void;
  refreshInventoryTypes: () => void;
}
const isHttpUrl = (url: string): boolean => {
  try {
    const u = new URL(url);
    return u.protocol === 'http:' || u.protocol === 'https:';
  } catch {
    return false;
  }
};

function buildIventoryFieldErrorMessage(params: {
  inventoryName: string;
  inventoryType: string;
  inventoryDescription: string;
  inventoryImage: string;
  inventoryBackupImage: string;
  imageUploaded: Uint8Array | null;
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
  if (imageUploaded && imageUploaded.length > 160 * 1024) {
    next.inventoryImage = 'Image too large (max 160KB).';
  }

  return next;
}

type FieldKey =
  | 'inventoryName'
  | 'inventoryType'
  | 'inventoryDescription'
  | 'inventoryImage'
  | 'inventoryBackupImage';

const AddInventoryForm: React.FC<AddInventoryProps> = ({
  showAddInventoryForm,
  handleInventoryClose,
  refreshInventoryTypes,
}: AddInventoryProps): React.ReactElement | null => {
  const [inventoryName, setInventoryName] = useState<string>('');
  const [inventoryType, setInventoryType] = useState<string>('');
  const [inventoryDescription, setInventoryDescription] = useState<string>('');
  const [inventoryImage, setInventoryImage] = useState<string>('');
  const [inventoryBackupImage, setInventoryBackupImage] = useState<string>('');
  const [inventoryTypes, setInventoryTypes] = useState<InventoryType[]>([]);
  const [imageUploaded, setImageUploaded] = useState<Uint8Array | null>(null);
  const [fieldErrors, setFieldErrors] = useState<
    Partial<Record<FieldKey, string>>
  >({});

  // Per-field history arrays. Initialize with the initial/current value so there is always a baseline.
  const [history, setHistory] = useState<Record<FieldKey, string[]>>({
    inventoryName: [''],
    inventoryType: [''],
    inventoryDescription: [''],
    inventoryImage: [''],
    inventoryBackupImage: [''],
  });

  // Track edit order. Last element = most recently edited field.
  const [lastEditedFields, setLastEditedFields] = useState<string[]>([]);

  useEffect(() => {
    async function fetchInventoryTypes(): Promise<void> {
      try {
        const types = await getAllInventoryTypes();
        setInventoryTypes(types);
      } catch (error) {
        console.error('Error fetching inventory types:', error);
      }
    }

    fetchInventoryTypes();
  }, []);

  // When form opens, initialize history baseline with current values
  useEffect(() => {
    if (showAddInventoryForm) {
      setHistory({
        inventoryName: [inventoryName],
        inventoryType: [inventoryType],
        inventoryDescription: [inventoryDescription],
        inventoryImage: [inventoryImage],
        inventoryBackupImage: [inventoryBackupImage],
      });
      setLastEditedFields([]);
      setFieldErrors({});
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [showAddInventoryForm]);

  // helper: count words in a string (0 for empty/whitespace-only)
  const countWords = (s: string): number => {
    const trimmed = s.trim();
    if (trimmed === '') return 0;
    return trimmed.split(/\s+/).filter(Boolean).length;
  };

  // helper: push a normalized snapshot for a field
  const pushSnapshot = (field: FieldKey, rawValue: string): void => {
    const normalized = rawValue.trim(); // trim trailing spaces
    setHistory(prev => {
      const fieldHist = prev[field] ?? [''];
      const lastRecorded = fieldHist[fieldHist.length - 1] ?? '';
      if (normalized !== lastRecorded) {
        return {
          ...prev,
          [field]: [...fieldHist, normalized],
        };
      }
      return prev;
    });
    setLastEditedFields(prev => {
      const updated = prev.filter(f => f !== field);
      return [...updated, field];
    });
  };

  // Save snapshot for field only when the *word count* changed
  const handleFieldChange = (
    field: FieldKey,
    setter: React.Dispatch<React.SetStateAction<string>>,
    value: string
  ): void => {
    const fieldHist = history[field] ?? [''];
    const lastRecorded = fieldHist[fieldHist.length - 1] ?? '';

    const isWordBoundary =
      value.endsWith(' ') ||
      value.trim() === '' ||
      countWords(value) < countWords(lastRecorded);

    if (isWordBoundary) {
      pushSnapshot(field, value);
    }

    setFieldErrors(prev => ({ ...prev, [field]: undefined })); // clear error on change

    setter(value);
  };

  // Undo handler
  const handleUndo = (): void => {
    const order = [...lastEditedFields];

    while (order.length > 0) {
      const candidate = order[order.length - 1] as FieldKey;
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

        switch (candidate) {
          case 'inventoryName':
            setInventoryName(restoredValue);
            break;
          case 'inventoryType':
            setInventoryType(restoredValue);
            break;
          case 'inventoryDescription':
            setInventoryDescription(restoredValue);
            break;
          case 'inventoryImage':
            setInventoryImage(restoredValue);
            break;
          case 'inventoryBackupImage':
            setInventoryBackupImage(restoredValue);
            break;
        }
        return;
      }

      order.pop();
    }
  };

  // Handling form submission
  const handleSubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    const errorsBeforeSubmit = buildIventoryFieldErrorMessage({
      inventoryName,
      inventoryType,
      inventoryDescription,
      inventoryImage,
      inventoryBackupImage,
      imageUploaded,
    });
    if (Object.keys(errorsBeforeSubmit).length) {
      setFieldErrors(errorsBeforeSubmit);
      return;
    }

    const selectedInventoryType = inventoryTypes.find(
      type => type.type === inventoryType
    );

    if (!selectedInventoryType) {
      setFieldErrors(prev => ({
        ...prev,
        inventoryType: 'Please select a valid inventory type.',
      }));
      return;
    }

    const base64Image = imageUploaded
      ? arrayBufferToBase64(imageUploaded)
      : null;

    const newInventory: Omit<Inventory, 'inventoryId'> = {
      inventoryName,
      inventoryType: selectedInventoryType.type,
      inventoryDescription,
      inventoryImage,
      inventoryBackupImage,
      imageUploaded: base64Image,
    };

    try {
      await addInventory(newInventory as Omit<Inventory, 'inventoryId'>);
      setInventoryName('');
      setInventoryType('');
      setInventoryDescription('');
      setInventoryImage('');
      setImageUploaded(null);
      setFieldErrors({});
      refreshInventoryTypes();
      handleInventoryClose();
    } catch (error) {
      if (error instanceof Error) {
        const msg = error.message.toLowerCase();

        if (msg.includes('already exists')) {
          setFieldErrors(prev => ({
            ...prev,
            inventoryName:
              prev.inventoryName ||
              'An inventory with this name already exists.',
          }));
          return;
        }
      }
      const errorsAfter = buildIventoryFieldErrorMessage({
        inventoryName,
        inventoryType,
        inventoryDescription,
        inventoryImage,
        inventoryBackupImage,
        imageUploaded,
      });
      if (Object.keys(errorsAfter).length) {
        setFieldErrors(prev => ({ ...prev, ...errorsAfter }));
        return;
      }
    }
  };

  if (!showAddInventoryForm) return null;

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 160 * 1024) {
        setFieldErrors(prev => ({
          ...prev,
          inventoryImage: 'Image too large (max 160KB).',
        }));
        setImageUploaded(null);
        e.target.value = '';
        return;
      }

      const reader = new FileReader();
      reader.onload = event => {
        if (event.target?.result instanceof ArrayBuffer) {
          // push snapshot before changing image
          pushSnapshot('inventoryImage', inventoryImage);
          const uint8Array = new Uint8Array(event.target.result as ArrayBuffer);
          setImageUploaded(uint8Array);
          setFieldErrors(prev => ({ ...prev, inventoryImage: undefined })); // clear error
        }
      };
      reader.readAsArrayBuffer(file);
    }
  };

  function arrayBufferToBase64(buffer: Uint8Array): string {
    const binary = String.fromCharCode(...buffer);
    return window.btoa(binary);
  }

  return (
    <div className="overlay">
      <div className="form-container">
        <h2>Add Inventory</h2>
        <form onSubmit={handleSubmit}>
          <div>
            <label htmlFor="inventoryName">Inventory Name:</label>
            <input
              type="text"
              id="inventoryName"
              className={fieldErrors.inventoryName ? 'invalid animate' : ''}
              value={inventoryName}
              onChange={e =>
                handleFieldChange(
                  'inventoryName',
                  setInventoryName,
                  e.target.value
                )
              }
              onBlur={() => pushSnapshot('inventoryName', inventoryName)}
              aria-invalid={!!fieldErrors.inventoryName}
              aria-describedby={
                fieldErrors.inventoryName ? 'err-inventoryName' : undefined
              }
              required
            />
            {fieldErrors.inventoryName && (
              <div id="err-inventoryName" className="field-error">
                {fieldErrors.inventoryName}
              </div>
            )}
          </div>

          <div>
            <label htmlFor="inventoryType">Inventory Type:</label>
            <select
              id="inventoryType"
              className={fieldErrors.inventoryType ? 'invalid animate' : ''}
              value={inventoryType}
              onChange={e =>
                handleFieldChange(
                  'inventoryType',
                  setInventoryType,
                  e.target.value
                )
              }
              onBlur={() => pushSnapshot('inventoryType', inventoryType)}
              aria-invalid={!!fieldErrors.inventoryType}
              aria-describedby={
                fieldErrors.inventoryType ? 'err-inventoryType' : undefined
              }
              required
            >
              <option value="">Select Type</option>
              {inventoryTypes.map((type, index) => (
                <option key={index} value={type.type}>
                  {type.type}
                </option>
              ))}
            </select>
            {fieldErrors.inventoryType && (
              <div id="err-inventoryType" className="field-error">
                {fieldErrors.inventoryType}
              </div>
            )}
          </div>

          <div>
            <label htmlFor="inventoryDescription">Inventory Description:</label>
            <input
              type="text"
              id="inventoryDescription"
              className={
                fieldErrors.inventoryDescription ? 'invalid animate' : ''
              }
              value={inventoryDescription}
              onChange={e =>
                handleFieldChange(
                  'inventoryDescription',
                  setInventoryDescription,
                  e.target.value
                )
              }
              onBlur={() =>
                pushSnapshot('inventoryDescription', inventoryDescription)
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

          <div>
            <label htmlFor="inventoryImage">Inventory Image:</label>
            <input
              type="text"
              id="inventoryImage"
              className={fieldErrors.inventoryImage ? 'invalid animate' : ''}
              value={inventoryImage}
              onChange={e =>
                handleFieldChange(
                  'inventoryImage',
                  setInventoryImage,
                  e.target.value
                )
              }
              onBlur={() => pushSnapshot('inventoryImage', inventoryImage)}
              aria-invalid={!!fieldErrors.inventoryImage}
              aria-describedby={
                fieldErrors.inventoryImage ? 'err-inventoryImage' : undefined
              }
            />
            {fieldErrors.inventoryImage && (
              <div id="err-inventoryImage" className="field-error">
                {fieldErrors.inventoryImage}
              </div>
            )}
          </div>

          <div>
            <label htmlFor="inventoryBackupImage">
              Inventory Backup Image:
            </label>
            <input
              type="text"
              id="inventoryBackupImage"
              className={
                fieldErrors.inventoryBackupImage ? 'invalid animate' : ''
              }
              value={inventoryBackupImage}
              onChange={e =>
                handleFieldChange(
                  'inventoryBackupImage',
                  setInventoryBackupImage,
                  e.target.value
                )
              }
              onBlur={() =>
                pushSnapshot('inventoryBackupImage', inventoryBackupImage)
              }
              aria-invalid={!!fieldErrors.inventoryBackupImage}
              aria-describedby={
                fieldErrors.inventoryBackupImage
                  ? 'err-inventoryBackupImage'
                  : undefined
              }
            />
            {fieldErrors.inventoryBackupImage && (
              <div id="err-inventoryBackupImage" className="field-error">
                {fieldErrors.inventoryBackupImage}
              </div>
            )}
          </div>

          <div>
            <label htmlFor="imageUpload">Upload Image:</label>
            <input
              type="file"
              id="imageUpload"
              accept="image/*"
              onChange={handleImageUpload}
            />
          </div>

          <button type="submit">Add Inventory</button>
          <button
            type="button"
            className="cancel"
            onClick={handleInventoryClose}
          >
            Cancel
          </button>

          {/* Undo button */}
          <button type="button" className="undo" onClick={handleUndo}>
            Undo
          </button>
        </form>
      </div>
    </div>
  );
};

export default AddInventoryForm;
