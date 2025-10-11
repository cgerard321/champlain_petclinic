import { useState, useEffect } from 'react';
import { Inventory } from './models/Inventory';
import { getAllInventoryTypes } from '@/features/inventories/api/getAllInventoryTypes.ts';
import addInventory from '@/features/inventories/api/addInventory.ts';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import './AddInventoryForm.css';
import axios from 'axios';

interface AddInventoryProps {
  showAddInventoryForm: boolean;
  handleInventoryClose: () => void;
  refreshInventoryTypes: () => void;
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
  const [errorMessage, setErrorMessage] = useState<string>('');
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
    const selectedInventoryType = inventoryTypes.find(
      type => type.type === inventoryType
    );

    if (!selectedInventoryType) {
      console.error('Invalid inventory type selected.');
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
      alert('Inventory added successfully!');
      setInventoryName('');
      setInventoryType('');
      setInventoryDescription('');
      setInventoryImage('');
      setImageUploaded(null);
      refreshInventoryTypes();
      handleInventoryClose();
      setErrorMessage('');
    } catch (error) {
      console.error('Error adding inventory:', error);

      setFieldErrors({});

      if (error instanceof Error) {
        setErrorMessage(error.message);

        setFieldErrors({
          inventoryName: 'Name must be at least 3 characters.',
          inventoryImage: 'Image URL must start with https://',
          // inventoryType: 'Please select a type.',
          // inventoryDescription: 'Description is required.',
          // inventoryBackupImage: 'Provide a backup image URL.',
        });
        return;
      }
      //if something odd slip through
      if (axios.isAxiosError(error)) {
        setErrorMessage('An unexpected error occurred. Please try again.');
        return;
      }
      setErrorMessage('An unexpected error occurred. Please try again.');
    }
  };

  if (!showAddInventoryForm) return null;

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const file = e.target.files?.[0];
    if (file) {
      if (file.size > 160 * 1024) {
        alert('Select a smaller image that does not exceed 160kb');
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
          {errorMessage && (
            <div
              className="form-error"
              role="alert"
              style={{ color: 'red', marginBottom: 12 }}
            >
              {errorMessage}
            </div>
          )}
          <div>
            <label htmlFor="inventoryName">Inventory Name:</label>
            <input
              type="text"
              id="inventoryName"
              value={inventoryName}
              onChange={e =>
                handleFieldChange(
                  'inventoryName',
                  setInventoryName,
                  e.target.value
                )
              }
              onBlur={() => pushSnapshot('inventoryName', inventoryName)}
              required
            />
            {fieldErrors.inventoryName && (
              <div className="field-error" style={{ color: 'red' }}>
                {fieldErrors.inventoryName}
              </div>
            )}
          </div>

          <div>
            <label htmlFor="inventoryType">Inventory Type:</label>
            <select
              id="inventoryType"
              value={inventoryType}
              onChange={e =>
                handleFieldChange(
                  'inventoryType',
                  setInventoryType,
                  e.target.value
                )
              }
              onBlur={() => pushSnapshot('inventoryType', inventoryType)}
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
              <div className="field-error" style={{ color: 'red' }}>
                {fieldErrors.inventoryType}
              </div>
            )}
          </div>

          <div>
            <label htmlFor="inventoryDescription">Inventory Description:</label>
            <input
              type="text"
              id="inventoryDescription"
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
              required
            />
            {fieldErrors.inventoryDescription && (
              <div className="field-error" style={{ color: 'red' }}>
                {fieldErrors.inventoryDescription}
              </div>
            )}
          </div>

          <div>
            <label htmlFor="inventoryImage">Inventory Image:</label>
            <input
              type="text"
              id="inventoryImage"
              value={inventoryImage}
              onChange={e =>
                handleFieldChange(
                  'inventoryImage',
                  setInventoryImage,
                  e.target.value
                )
              }
              onBlur={() => pushSnapshot('inventoryImage', inventoryImage)}
              required
            />
            {fieldErrors.inventoryImage && (
              <div className="field-error" style={{ color: 'red' }}>
                {fieldErrors.inventoryImage}
              </div>
            )}
          </div>

          <div>
            <label htmlFor="inventoryImage">Inventory Backup Image:</label>
            <input
              type="text"
              id="inventoryBackupImage"
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
              required
            />
            {fieldErrors.inventoryBackupImage && (
              <div className="field-error" style={{ color: 'red' }}>
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
