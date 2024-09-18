import { useState } from 'react';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import addInventoryType from '@/features/inventories/api/addInventoryType.ts';
import './AddInventoryType.css'; // Import CSS for styling

interface AddInventoryTypeProps {
  show: boolean;
  handleClose: () => void;
  refreshInventoryTypes: () => void; // Ensure this prop is used
}

export default function AddInventoryType({
  show,
  handleClose,
  refreshInventoryTypes,
}: AddInventoryTypeProps): React.ReactElement | null {
  const [type, setType] = useState('');

  const handleSubmit = async (event: React.FormEvent): Promise<void> => {
    event.preventDefault();
    const newInventoryType: Omit<InventoryType, 'typeId'> = {
      type,
    };

    try {
      await addInventoryType(newInventoryType);
      refreshInventoryTypes(); // Call this function to update the list
      handleClose();
    } catch (error) {
      console.error('Error adding inventory type:', error);
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
              onChange={e => setType(e.target.value)}
              required
            />
          </div>
          <button type="submit">Add</button>
          <button type="button" className="cancel" onClick={handleClose}>
            Cancel
          </button>
        </form>
      </div>
    </div>
  );
}
