import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
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
    imageUploaded: null,
  });
  const [inventoryTypes, setInventoryTypes] = useState<InventoryType[]>([]);
  const [error, setError] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);

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

  const handleUploadImageClick = (): void => {
    const fileInput = document.querySelector(
      'input[name="imageUploaded"]'
    ) as HTMLInputElement;
    if (fileInput) {
      fileInput.click();
    }
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
                    Inventory Image{' '}
                    <svg
                      onClick={handleUploadImageClick}
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                      fill="currentColor"
                      className={`bi bi-upload ${EditInventoryFormStyles.uploadIcon}`}
                      viewBox="0 0 16 16"
                    >
                      <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5" />
                      <path d="M7.646 1.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1-.708.708L8.5 2.707V11.5a.5.5 0 0 1-1 0V2.707L5.354 4.854a.5.5 0 1 1-.708-.708z" />
                    </svg>
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
                  <div>
                    <svg
                      onClick={handleUploadImageClick}
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                      fill="currentColor"
                      className={`bi bi-upload ${EditInventoryFormStyles.uploadIcon}`}
                      viewBox="0 0 16 16"
                    >
                      <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5" />
                      <path d="M7.646 1.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1-.708.708L8.5 2.707V11.5a.5.5 0 0 1-1 0V2.707L5.354 4.854a.5.5 0 1 1-.708-.708z" />
                    </svg>
                  </div>
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
                  <label>Upload Image</label>
                  <div>
                    <svg
                      onClick={handleUploadImageClick}
                      xmlns="http://www.w3.org/2000/svg"
                      width="16"
                      height="16"
                      fill="currentColor"
                      className={`bi bi-upload ${EditInventoryFormStyles.uploadIcon}`}
                      viewBox="0 0 16 16"
                    >
                      <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5" />
                      <path d="M7.646 1.146a.5.5 0 0 1 .708 0l3 3a.5.5 0 0 1-.708.708L8.5 2.707V11.5a.5.5 0 0 1-1 0V2.707L5.354 4.854a.5.5 0 1 1-.708-.708z" />
                    </svg>
                  </div>
                </div>
                <input
                  type="file"
                  name="imageUploaded"
                  className="form-control"
                  placeholder="Inventory Uploaded Image"
                  onChange={e => {
                    const files = e.target.files;
                    if (files && files.length > 0) {
                      const file = files[0];

                      const reader = new FileReader();
                      reader.onload = event => {
                        if (event.target?.result instanceof ArrayBuffer) {
                          const uint8Array = new Uint8Array(
                            event.target.result as ArrayBuffer
                          );
                          setInventory({
                            ...inventory,
                            imageUploaded: uint8Array,
                          });
                        }
                      };
                      reader.readAsArrayBuffer(file);
                    } else {
                      setInventory({
                        ...inventory,
                        imageUploaded: null,
                      });
                    }
                  }}
                  required
                />
                {error.imageUploaded && (
                  <span className="error">{error.imageUploaded}</span>
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
