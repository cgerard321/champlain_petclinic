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

    const newInventory: Omit<Inventory, 'inventoryId'> = {
      inventoryName,
      inventoryType: selectedInventoryType.type,
      inventoryDescription,
      inventoryImage,
      inventoryBackupImage,
    };

    try {
      await addInventory(newInventory as Omit<Inventory, 'inventoryId'>);
      alert('Inventory added successfully!');
      setInventoryName('');
      setInventoryType('');
      setInventoryDescription('');
      setInventoryImage('');
      refreshInventoryTypes(); // Call the function to refresh inventory types
      handleInventoryClose(); // Close the form after adding the inventory
    } catch (error) {
      console.error('Error adding inventory:', error);
    }
  };

  // Conditionally render the form based on the show prop
  if (!showAddInventoryForm) return null; // Do not render if show is false

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
              value={inventoryName}
              onChange={e => setInventoryName(e.target.value)}
              required
            />
          </div>

          <div>
            <label htmlFor="inventoryType">Inventory Type:</label>
            <select
              id="inventoryType"
              value={inventoryType}
              onChange={e => setInventoryType(e.target.value)}
              required
            >
              <option value="">Select Type</option>
              {inventoryTypes.map((type, index) => (
                <option key={index} value={type.type}>
                  {type.type}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label htmlFor="inventoryDescription">Inventory Description:</label>
            <input
              type="text"
              id="inventoryDescription"
              value={inventoryDescription}
              onChange={e => setInventoryDescription(e.target.value)}
              required
            />
          </div>

          <div>
            <label htmlFor="inventoryImage">Inventory Image:</label>
            <input
              type="text"
              id="inventoryImage"
              value={inventoryImage}
              onChange={e => setInventoryImage(e.target.value)}
              required
            />
          </div>

          <div>
            <label htmlFor="inventoryImage">Inventory Backup Image:</label>
            <input
              type="text"
              id="inventoryBackupImage"
              value={inventoryBackupImage}
              onChange={e => setInventoryBackupImage(e.target.value)}
              required
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
        </form>
      </div>
    </div>
  );
};

export default AddInventoryForm;
