import { useState, useEffect, JSX } from 'react';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Inventory } from '@/features/inventories/models/Inventory.ts';

export default function InventoriesListTable(): JSX.Element {
  const [inventoryList, setInventoryList] = useState<Inventory[]>([]);
  const [inventoryName, setInventoryName] = useState('');
  const [inventoryType, setInventoryType] = useState('');
  const [inventoryDescription, setInventoryDescription] = useState('');
  const [realPage, setRealPage] = useState(1);
  const navigate = useNavigate();

  useEffect(() => {
    // Fetch inventory list from API
    axiosInstance
      .get<Inventory[]>(axiosInstance.defaults.baseURL + 'inventory')
      .then(response => {
        setInventoryList(response.data);
      });
    // axios.get('/api/inventory').then(response => {
    //   setInventoryList(response.data);
    // });
  }, []);

  const searchInventory = (): void => {
    // Implement search logic here
  };

  const clearQueries = (): void => {
    setInventoryName('');
    setInventoryType('');
    setInventoryDescription('');
  };

  const pageBefore = (): void => {
    setRealPage(prevPage => Math.max(prevPage - 1, 1));
  };

  const pageAfter = (): void => {
    setRealPage(prevPage => prevPage + 1);
  };

  const deleteInventory = (inventory: Inventory): void => {
    // Implement delete logic here
  };

  const undoDelete = (inventoryId: Inventory): void => {
    // Implement undo delete logic here
  };

  const deleteAllInventories = (): void => {
    // Implement delete all logic here
  };

  return (
    <div>
      <table className="table table-striped">
        <thead>
          <tr>
            <td>Inventory ID</td>
            <td>Name</td>
            <td>Type</td>
            <td>Description</td>
            <td></td>
            <td></td>
            <td></td>
          </tr>
          <tr>
            <td></td>
            <td>
              <input
                type="text"
                value={inventoryName}
                onChange={e => setInventoryName(e.target.value)}
                onKeyUp={e => e.key === 'Enter' && searchInventory()}
              />
            </td>
            <td>
              <select
                className="form-control col-sm-4"
                value={inventoryType}
                onChange={e => setInventoryType(e.target.value)}
                onKeyUp={e => e.key === 'Enter' && searchInventory()}
              >
                {/* Replace with your inventory type options */}
                <option value="">None</option>
                <option value="Type1">Type1</option>
                <option value="Type2">Type2</option>
              </select>
            </td>
            <td>
              <input
                type="text"
                value={inventoryDescription}
                onChange={e => setInventoryDescription(e.target.value)}
                onKeyUp={e => e.key === 'Enter' && searchInventory()}
              />
            </td>
            <td></td>
            <td>
              <button
                className="btn btn-success"
                onClick={clearQueries}
                title="Clear"
              >
                <img
                  src="https://cdn.lordicon.com/zxvuvcnc.json"
                  alt="icon"
                  style={{ width: '32px', height: '32px' }}
                />
                {/*<lord-icon*/}
                {/*  src="https://cdn.lordicon.com/zxvuvcnc.json"*/}
                {/*  trigger="hover"*/}
                {/*  style={{ width: '32px', height: '32px' }}*/}
                {/*></lord-icon>*/}
              </button>
            </td>
            <td>
              <button
                className="btn btn-success"
                onClick={searchInventory}
                title="Search"
              >
                <img
                  src="https://cdn.lordicon.com/fkdzyfle.json"
                  alt="icon"
                  style={{ width: '32px', height: '32px' }}
                />
                {/*<lord-icon*/}
                {/*  src="https://cdn.lordicon.com/fkdzyfle.json"*/}
                {/*  trigger="hover"*/}
                {/*  style={{ width: '32px', height: '32px' }}*/}
                {/*></lord-icon>*/}
              </button>
            </td>
          </tr>
        </thead>
        <tbody>
          {inventoryList.map(inventory => (
            <tr
              key={inventory.inventoryId}
              onClick={() => navigate(`/productList/${inventory.inventoryId}`)}
            >
              <td>{inventory.inventoryId}</td>
              <td>
                <a
                  style={{ textDecoration: 'none' }}
                  href={`/productList/${inventory.inventoryId}`}
                >
                  {inventory.inventoryName}
                </a>
              </td>
              <td>{inventory.inventoryType}</td>
              <td>{inventory.inventoryDescription}</td>
              <td>
                <button
                  className="btn btn-warning"
                  onClick={e => {
                    e.stopPropagation();
                    navigate(`/updateInventory/${inventory.inventoryId}/edit`);
                  }}
                  title="Edit"
                >
                  {/*<lord-icon*/}
                  {/*  src="https://cdn.lordicon.com/wkvacbiw.json"*/}
                  {/*  trigger="hover"*/}
                  {/*  style={{ width: '32px', height: '32px' }}*/}
                  {/*></lord-icon>*/}
                  <img
                    src="https://cdn.lordicon.com/wkvacbiw.json"
                    alt="icon"
                    style={{ width: '32px', height: '32px' }}
                  />
                </button>
              </td>
              <td>
                {!inventory.isTemporarilyDeleted ? (
                  <button
                    className="btn btn-danger"
                    onClick={e => {
                      e.stopPropagation();
                      deleteInventory(inventory);
                    }}
                    title="Delete"
                  >
                    <img
                      src="https://cdn.lordicon.com/skkahier.json"
                      alt="icon"
                      style={{ width: '32px', height: '32px' }}
                    />
                    {/*<icon*/}
                    {/*  src="https://cdn.lordicon.com/skkahier.json"*/}
                    {/*  trigger="hover"*/}
                    {/*  style={{ width: '32px', height: '32px' }}*/}
                    {/*></icon>*/}
                  </button>
                ) : (
                  <button
                    className="btn btn-info"
                    onClick={e => {
                      e.stopPropagation();
                      undoDelete(inventory);
                    }}
                    title="Restore"
                  >
                    Restore
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="text-center">
        <table className="mx-auto">
          <tr>
            <td>
              <button className="btn btn-success btn-sm" onClick={pageBefore}>
                &lt;
              </button>
            </td>
            <td>
              <span>{realPage}</span>
            </td>
            <td>
              <button className="btn btn-success btn-sm" onClick={pageAfter}>
                &gt;
              </button>
            </td>
          </tr>
        </table>
      </div>
      <div id="loadingObject" style={{ display: 'none' }}>
        Loading...
      </div>
      <div
        id="notification"
        style={{
          display: 'none',
          position: 'fixed',
          bottom: '10px',
          right: '10px',
          backgroundColor: '#4CAF50',
          color: 'white',
          padding: '10px',
          borderRadius: '5px',
        }}
      >
        Notification Text Here
      </div>
      <button
        className="delete-bundle-button btn btn-success"
        onClick={deleteAllInventories}
      >
        Delete All Inventory
      </button>
      <button
        className="add-inventory-button btn btn-success"
        onClick={() => navigate('/inventoryNew')}
      >
        Add Inventory
      </button>
    </div>
  );
}
