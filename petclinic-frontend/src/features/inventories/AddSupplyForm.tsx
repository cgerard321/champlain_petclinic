import { useEffect, useState } from 'react';
import { getAllInventoryTypes } from '@/features/inventories/api/getAllInventoryTypes.ts';
import { addSupplyToInventoryByType } from '@/features/inventories/api/addSupplyToInventoryByType.ts'; // Import the function
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import { SupplyModel } from '@/features/inventories/models/ProductModels/SupplyModel.ts';

interface SupplyFormProps {
  onClose: () => void;
  onSubmit: () => void;
}

// eslint-disable-next-line react/prop-types
const AddSupplyForm: React.FC<SupplyFormProps> = ({ onClose, onSubmit }) => {
  const [inventoryTypes, setInventoryTypes] = useState<InventoryType[]>([]);
  const [selectedType, setSelectedType] = useState<string>('');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState(0);
  const [quantity, setQuantity] = useState(0);
  const [salePrice, setSalePrice] = useState(0);

  useEffect(() => {
    // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
    async function fetchInventoryTypes() {
      try {
        const types = await getAllInventoryTypes();
        setInventoryTypes(types);
      } catch (error) {
        console.error('Error fetching inventory types:', error);
      }
    }

    fetchInventoryTypes();
  }, []);

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleAddSupplySubmit = async () => {
    const supply: SupplyModel = {
      supplyName: name,
      supplyDescription: description,
      supplyPrice: price,
      supplyQuantity: quantity,
      supplySalePrice: salePrice,
    };

    try {
      await addSupplyToInventoryByType(selectedType, supply);
      onSubmit();
    } catch (error) {
      console.error('Error adding supply:', error);
    }
  };

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleAddSupplySubmit();
    onClose();
  };

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedType(e.target.value);
  };

  return (
    <>
      <div className="modal-overlay">
        <div className="modal-content">
          <h2>Add New Supply</h2>
          <button className="btn btn-danger" onClick={onClose}>
            Close
          </button>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="inventoryType">Inventory Type:</label>
              <select
                id="inventoryType"
                value={selectedType}
                onChange={handleChange}
                className="form-control"
                required
              >
                <option value="" disabled>
                  -- Select Inventory Type --
                </option>
                {inventoryTypes.map(({ typeId, type }) => (
                  <option key={typeId} value={type}>
                    {type}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="supplyName">Enter a name for the supply:</label>
              <input
                type="text"
                id="supplyName"
                value={name}
                onChange={e => setName(e.target.value)}
                className="form-control"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="supplyDescription">Description:</label>
              <textarea
                id="supplyDescription"
                value={description}
                onChange={e => setDescription(e.target.value)}
                className="form-control"
                placeholder="Enter a description for the supply"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="supplyPrice">Price:</label>
              <input
                type="number"
                id="supplyPrice"
                value={price}
                onChange={e => setPrice(Number(e.target.value))}
                className="form-control"
                placeholder="Enter the price"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="supplyQuantity">Quantity:</label>
              <input
                type="number"
                id="supplyQuantity"
                value={quantity}
                onChange={e => setQuantity(Number(e.target.value))}
                className="form-control"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="supplySalePrice">Sale Price:</label>
              <input
                type="number"
                id="supplySalePrice"
                value={salePrice}
                onChange={e => setSalePrice(Number(e.target.value))}
                className="form-control"
                placeholder="Enter the sale price"
              />
            </div>
            <button type="submit" className="btn btn-primary">
              Submit
            </button>
          </form>
        </div>
      </div>
    </>
  );
};

export default AddSupplyForm;
