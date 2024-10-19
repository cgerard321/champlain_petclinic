import * as React from 'react';
import { FormEvent, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getInventory,
  updateInventory,
} from '@/features/inventories/api/EditInventory.ts';
import { InventoryResponseModel } from '@/features/inventories/models/InventoryModels/InventoryResponseModel.ts';
import { InventoryRequestModel } from '@/features/inventories/models/InventoryModels/InventoryRequestModel.ts';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import { getAllInventoryTypes } from '@/features/inventories/api/getAllInventoryTypes.ts';
import EditInventoryFormStyles from './EditInventoryForm.module.css';

interface ApiError {
  message: string;
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
  const [error, setError] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const navigate = useNavigate();

  useEffect(() => {
    const fetchInventoryData = async (): Promise<void> => {
      if (inventoryId) {
        try {
          const response: InventoryResponseModel =
            await getInventory(inventoryId);
          setInventory({
            inventoryName: response.inventoryName,
            inventoryType: response.inventoryType,
            inventoryDescription: response.inventoryDescription,
            inventoryImage: response.inventoryImage,
            inventoryBackupImage: response.inventoryBackupImage,
            imageUploaded: response.imageUploaded,
          });
        } catch (error) {
          console.error(
            `Error fetching inventory with ID ${inventoryId}:`,
            error
          );
        }
      }
    };

    fetchInventoryData().catch(error =>
      console.error('Error in fetchInventoryData:', error)
    );

    const fetchInventoryTypes = async (): Promise<void> => {
      try {
        const types = await getAllInventoryTypes();
        setInventoryTypes(types); // Set the fetched types in state
      } catch (error) {
        console.error('Error fetching inventory types:', error);
      }
    };

    fetchInventoryTypes();
  }, [inventoryId]);

  const validate = (): boolean => {
    const newError: { [key: string]: string } = {};
    if (!inventory.inventoryName) {
      newError.inventoryName = 'Inventory name is required';
    }
    if (
      inventory.inventoryType != 'Equipment' &&
      inventory.inventoryType != 'Injections' &&
      inventory.inventoryType != 'Medications' &&
      inventory.inventoryType != 'Bandages'
    ) {
      newError.inventoryType = 'Inventory type is required';
    }
    if (!inventory.inventoryDescription) {
      newError.inventoryDescription = 'Inventory description is required';
    }
    setError(newError);
    return Object.keys(newError).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate()) return;

    setLoading(true);
    setErrorMessage('');
    setSuccessMessage('');
    setShowNotification(false);

    try {
      if (inventoryId) {
        await updateInventory(inventoryId, inventory);
        setSuccessMessage('Inventory updated successfully');
        setShowNotification(true);
        setTimeout(() => {
          navigate('/inventories');
        }, 2000);
      }
    } catch (error) {
      const apiError = error as ApiError;
      setErrorMessage(`Error updating inventory: ${apiError.message}`);
    } finally {
      setLoading(false);
    }
  };

  const handleFileChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ): void => {
    const file = event.target.files?.[0];
    if (file) {
      if (file.size > 160 * 1024) {
        alert('File size must be less than 160KB.');
        if (fileInputRef.current) {
          fileInputRef.current.value = '';
        }
        return;
      } else {
        convertToBase64(file);
      }
    }
  };

  const convertToBase64 = (file: File): void => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onloadend = () => {
      if (reader.result) {
        const base64String = (reader.result as string).split(',')[1];
        setInventory({
          ...inventory,
          imageUploaded: base64String,
        });
      }
    };
  };

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
                  placeholder="Inventory Name"
                  className="form-control"
                  value={inventory.inventoryName}
                  onChange={e =>
                    setInventory({
                      ...inventory,
                      inventoryName: e.target.value,
                    })
                  }
                  required
                />
                {error.inventoryName && (
                  <span className="error">{error.inventoryName}</span>
                )}
              </div>
            </div>
            <div className="col-4">
              <div className="form-group">
                <label>Inventory Type</label>
                <select
                  name="inventoryType"
                  className="form-control"
                  value={inventory.inventoryType}
                  onChange={e =>
                    setInventory({
                      ...inventory,
                      inventoryType: e.target.value,
                    })
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
                {error.inventoryType && (
                  <span className="error">{error.inventoryType}</span>
                )}
              </div>
            </div>
            <div className="col-4">
              <div className="form-group">
                <label>Inventory Description</label>
                <input
                  type="text"
                  name="inventoryDescription"
                  className="form-control"
                  placeholder="Inventory Description"
                  value={inventory.inventoryDescription}
                  onChange={e =>
                    setInventory({
                      ...inventory,
                      inventoryDescription: e.target.value,
                    })
                  }
                  required
                />
                {error.inventoryDescription && (
                  <span className="error">{error.inventoryDescription}</span>
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
                  className="form-control"
                  placeholder="Inventory Image"
                  value={inventory.inventoryImage}
                  onChange={e =>
                    setInventory({
                      ...inventory,
                      inventoryImage: e.target.value,
                    })
                  }
                  required
                />
                {error.inventoryImage && (
                  <span className="error">{error.inventoryImage}</span>
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
                  className="form-control"
                  placeholder="Inventory Backup Image"
                  value={inventory.inventoryBackupImage}
                  onChange={e =>
                    setInventory({
                      ...inventory,
                      inventoryBackupImage: e.target.value,
                    })
                  }
                  required
                />
                {error.inventoryBackupImage && (
                  <span className="error">{error.inventoryBackupImage}</span>
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
                  className="form-control"
                  accept="image/*"
                  onChange={handleFileChange}
                  ref={fileInputRef}
                  required
                />
                {error.uploadedImage && (
                  <span className="error">{error.uploadedImage}</span>
                )}
              </div>
            </div>
          </div>
          <br />
          <div className="row">
            <button type="submit" className="btn btn-info">
              Update
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
