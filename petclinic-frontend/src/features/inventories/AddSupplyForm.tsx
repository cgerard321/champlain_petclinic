// eslint-disable-next-line import/default
import React, { useState } from 'react';

interface SupplyFormProps {
  onClose: () => void;
  onSubmit: (inventory: {
    name: string;
    description: string;
    price: number;
    quantity: number;
    salePrice: number;
  }) => void;
}

// eslint-disable-next-line react/prop-types
const AddSupplyForm: React.FC<SupplyFormProps> = ({ onClose, onSubmit }) => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState(0);
  const [quantity, setQuantity] = useState(0);
  const [salePrice, setSalePrice] = useState(0);

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSubmit({ name, description, price, quantity, salePrice });
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
