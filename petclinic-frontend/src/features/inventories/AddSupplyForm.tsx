// eslint-disable-next-line import/default
import React, { useEffect, useState } from 'react';

interface SupplyFormProps {
  onClose: () => void;
  onSubmit: () => void;
}

interface InventoryType {
  typeId: string;
  type: string;
}

const AddSupplyForm: React.FC<SupplyFormProps> = ({ onClose, onSubmit }) => {
  const [inventoryType, setInventoryType] = useState('');
  const [inventoryTypes, setInventoryTypes] = useState<InventoryType[]>([]);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState(0);
  const [quantity, setQuantity] = useState(0);
  const [salePrice, setSalePrice] = useState(0);

  useEffect(() => {
    // Function to fetch inventory typess
    // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
    const fetchInventoryTypes = async () => {
      try {
        const response = await fetch(
          'http://localhost:8080/api/v2/gateway/inventories/types',
          {
            method: 'GET',
            headers: {
              Authorization: `Bearer YOUR_TOKEN_HERE`,
              'Content-Type': 'application/json',
            },
          }
        );
        if (response.ok) {
          const data: InventoryType[] = await response.json();
          setInventoryTypes(data);
        } else {
          console.error('Failed to fetch inventory types');
        }
      } catch (error) {
        console.error('Error fetching inventory types:', error);
      }
    };

    fetchInventoryTypes();
  }, []);

  // Function to handle adding the supply
  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleAddSupplySubmit = async () => {
    const inventory = {
      inventoryType,
      name,
      description,
      price,
      quantity,
      salePrice,
    };

    try {
      const response = await fetch(
        `http://localhost:8080/api/v2/gateway/inventories/${inventory.inventoryType}/supplies`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(inventory),
        }
      );

      if (response.ok) {
        // eslint-disable-next-line no-console
        console.log('Supply added successfully');
        onSubmit();
      } else {
        // eslint-disable-next-line no-console
        console.error('Failed to add supply');
      }
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error('Error adding supply:', error);
    }
  };

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleAddSupplySubmit();
    onClose();
  };

  return (
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
              value={inventoryType}
              onChange={e => setInventoryType(e.target.value)}
              className="form-control"
              required
            >
              <option value="">Select Inventory Type</option>
              {inventoryTypes.map(({ typeId, type }) => (
                <option key={typeId} value={typeId}>
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
  );
};

export default AddSupplyForm;
