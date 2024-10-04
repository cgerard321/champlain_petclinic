import { useEffect, useState } from 'react';
import { getAllInventoryNames } from '@/features/inventories/api/getAllInventoryNames.ts';
import { addProductToInventoryByName } from '@/features/inventories/api/addProductToInventoryByName.ts'; // Import the function
import { InventoryName } from '@/features/inventories/models/InventoryName.ts';
import './AddProductForm.css';
import { ProductModelINVT } from '@/features/inventories/models/ProductModels/ProductModelINVT.ts';

interface ProductFormProps {
  onClose: () => void;
  onSubmit: () => void;
}

// eslint-disable-next-line react/prop-types
const AddProductForm: React.FC<ProductFormProps> = ({ onClose, onSubmit }) => {
  const [inventoryNames, setInventoryNames] = useState<InventoryName[]>([]);
  const [selectedName, setSelectedName] = useState<string>('');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState(0);
  const [quantity, setQuantity] = useState(0);
  const [salePrice, setSalePrice] = useState(0);

  useEffect(() => {
    // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
    async function fetchInventoryNames() {
      try {
        const names = await getAllInventoryNames();
        setInventoryNames(names);
      } catch (error) {
        console.error('Error fetching inventory names:', error);
      }
    }

    fetchInventoryNames();
  }, []);

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleAddProductSubmit = async () => {
    const product: ProductModelINVT = {
      productName: name,
      productDescription: description,
      productPrice: price,
      productQuantity: quantity,
      productSalePrice: salePrice,
      productStatus: 'AVAILABLE',
    };

    try {
      await addProductToInventoryByName(selectedName, product);
      onSubmit();
    } catch (error) {
      console.error('Error adding supply:', error);
    }
  };

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleAddProductSubmit();
    onClose();
  };

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedName(e.target.value);
  };

  return (
    <>
      <div className="overlay">
        <div className="form-container">
          <div id="first-row">
            <h2 style={{ marginTop: '20px' }}>Add Product To Inventory</h2>
            <button
              className="btn btn-danger"
              onClick={onClose}
              style={{
                backgroundColor: 'black',
                color: 'whitesmoke',
                marginLeft: 'auto',
                height: '50px',
                fontWeight: 'bold',
              }}
            >
              Close
            </button>
          </div>

          <form onSubmit={handleSubmit}>
            <div id="second-row">
              <div id="first-item">
                <p className="p-elements">Select an inventory:</p>
                <select
                  id="inventoryName"
                  value={selectedName}
                  onChange={handleChange}
                  className="custom-input"
                  required
                >
                  <option value="" disabled>
                    -- Select Inventory Name --
                  </option>
                  {inventoryNames.map(({ nameId, name }) => (
                    <option key={nameId} value={name}>
                      {name}
                    </option>
                  ))}
                </select>
              </div>
              <div id="second-item">
                <p className="p-elements">Enter a name for the supply:</p>
                <input
                  type="text"
                  id="supplyName"
                  value={name}
                  onChange={e => setName(e.target.value)}
                  className="custom-input"
                  required
                />
              </div>
            </div>

            <div id="third-row">
              <div id="third-item">
                <p className="p-elements">Description of supply:</p>
                <textarea
                  id="supplyDescription"
                  value={description}
                  onChange={e => setDescription(e.target.value)}
                  className="custom-input"
                  placeholder="Enter a description for the supply"
                  required
                />
              </div>
              <div id="fourth-item">
                <p className="p-elements">Price of supply:</p>
                <input
                  type="text"
                  id="supplyPrice"
                  value={price}
                  onChange={e => {
                    const value = e.target.value;
                    if (/^\d*\.?\d*$/.test(value)) {
                      setPrice(value ? parseFloat(value) : 0);
                    }
                  }}
                  className="custom-input"
                  placeholder="Enter the price"
                  required
                  style={{ width: '100px' }}
                />
              </div>
            </div>

            <div id="fourth-row">
              <div id="fifth-item">
                <p className="p-elements">Quantity of supply:</p>
                <input
                  type="number"
                  id="supplyQuantity"
                  value={quantity}
                  onChange={e => setQuantity(Number(e.target.value))}
                  className="custom-input"
                  required
                  style={{ width: '100px' }}
                />
              </div>
              <div id="sixth-item">
                <p className="p-elements">Sale Price of supply:</p>
                <input
                  type="text"
                  id="supplySalePrice"
                  value={salePrice}
                  onChange={e => {
                    const value = e.target.value;
                    if (/^\d*\.?\d*$/.test(value)) {
                      setSalePrice(value ? parseFloat(value) : 0);
                    }
                  }}
                  className="custom-input"
                  placeholder="Enter the sale price"
                  style={{ width: '100px' }}
                />
              </div>
            </div>

            <div id="fifth-row">
              <button
                type="submit"
                className="btn btn-primary"
                style={{
                  width: '200px',
                  backgroundColor: '#89CFF0',
                  color: 'whitesmoke',
                  fontWeight: 'bold',
                }}
              >
                Submit
              </button>
            </div>
          </form>
        </div>
      </div>
    </>
  );
};

export default AddProductForm;
