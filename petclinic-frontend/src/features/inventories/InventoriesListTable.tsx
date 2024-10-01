import { useState, useEffect, JSX } from 'react';
import { useNavigate } from 'react-router-dom';
import { Inventory } from '@/features/inventories/models/Inventory.ts';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import useSearchInventories from '@/features/inventories/hooks/useSearchInventories.ts';
import { getAllInventoryTypes } from '@/features/inventories/api/getAllInventoryTypes.ts';
import deleteAllInventories from '@/features/inventories/api/deleteAllInventories.ts';
import './InventoriesListTable.css';
import deleteInventory from '@/features/inventories/api/deleteInventory.ts';
import AddInventory from '@/features/inventories/AddInventoryForm.tsx';
import AddInventoryType from '@/features/inventories/AddInventoryType.tsx';
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';
import AddSupplyForm from '@/features/inventories/AddSupplyForm.tsx';
import './models/Card.css';
import DefaultInventoryImage from '@/assets/Inventory/DefaultInventoryImage.jpg';

//TODO: create add inventory form component and change the component being shown on the inventories page on the onClick event of the add inventory button
export default function InventoriesListTable(): JSX.Element {
  const [selectedInventories, setSelectedInventories] = useState<Inventory[]>(
    []
  );
  const [inventoryName, setInventoryName] = useState('');
  const [inventoryType, setInventoryType] = useState('');
  const [inventoryTypeList, setInventoryTypeList] = useState<InventoryType[]>(
    []
  );
  const [inventoryDescription, setInventoryDescription] = useState('');
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [showAddInventoryForm, setShowAddInventoryForm] = useState(false);
  const [showAddTypeForm, setShowAddTypeForm] = useState(false);
  const [showAddSupplyModal, setShowAddSupplyModal] = useState(false);
  const navigate = useNavigate();
  const [lowStockProductsByInventory, setLowStockProductsByInventory] =
    useState<{ [inventoryName: string]: ProductModel[] }>({});
  const [showLowStock, setShowLowStock] = useState(false);

  const toggleAddSupplyModal = (): void => {
    setShowAddSupplyModal(prev => !prev);
  };

  const {
    inventoryList,
    setInventoryList,
    currentPage,
    realPage,
    getInventoryList,
    setCurrentPage,
  } = useSearchInventories();

  useEffect(() => {
    getInventoryList('', '', '');
    fetchAllInventoryTypes();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage]);

  useEffect(() => {
    getInventoryList('', '', '');
    refreshInventoryTypes();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage]);

  const clearQueries = (): void => {
    setInventoryName('');
    setInventoryType('');
    setInventoryDescription('');
    getInventoryList('', '', '');
  };

  const pageBefore = (): void => {
    setCurrentPage(prevPage => Math.max(prevPage - 1, 0));
  };
  const pageAfter = (): void => {
    setCurrentPage(prevPage => prevPage + 1);
  };

  const deleteInventoryHandler = (inventoryToDelete: Inventory): void => {
    deleteInventory(inventoryToDelete);
    getInventoryList(inventoryName, inventoryType, inventoryDescription);
  };

  const handleDeleteAllInventories = (confirm: boolean): void => {
    if (confirm) {
      setInventoryList([]);
      deleteAllInventories();
      setShowConfirmDialog(false);
    } else {
      if (showConfirmDialog) {
        setShowConfirmDialog(false);
        return;
      }
      setShowConfirmDialog(true);
    }
  };

  const getAllLowStockProducts = async (
    inventory: Inventory
  ): Promise<void> => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/gateway/inventory/${inventory.inventoryId}/products/lowstock`,
        {
          method: 'GET',
          credentials: 'include',
          headers: {
            Accept: 'application/json',
          },
        }
      );

      if (response.status === 404) {
        // eslint-disable-next-line no-console
        console.log(
          `No products below threshold in inventory: ${inventory.inventoryName}`
        );
      }

      const data = await response.json();
      if (data && data.length > 0) {
        setLowStockProductsByInventory(prevState => ({
          ...prevState,
          [inventory.inventoryName]: data, //Group products by inventory name
        }));
        setShowLowStock(true);
      }
    } catch (error) {
      console.error('Error fetching low stock products:', error);
    }
  };

  const fetchAllInventoryTypes = async (): Promise<void> => {
    const data = await getAllInventoryTypes();
    setInventoryTypeList(data);
  };

  const refreshInventoryTypes = async (): Promise<void> => {
    await fetchAllInventoryTypes();
  };

  const handleAddSupplySubmit = (): void => {
    setShowAddSupplyModal(false);
  };

  const handleInventorySelection = (
    e: React.ChangeEvent<HTMLInputElement>,
    inventory: Inventory
  ): void => {
    const isChecked = e.target.checked;
    setSelectedInventories(prevSelected => {
      if (isChecked) {
        return [...prevSelected, inventory];
      } else {
        return prevSelected.filter(
          selectedInventory =>
            selectedInventory.inventoryId !== inventory.inventoryId
        );
      }
    });
  };

  const deleteSelectedInventories = async (): Promise<void> => {
    for (const inventory of selectedInventories) {
      await deleteInventory(inventory); // Delete each selected inventory from the database
    }

    // Refresh the inventory list after deleting
    getInventoryList(inventoryName, inventoryType, inventoryDescription);

    // Clear the selected inventories
    setSelectedInventories([]);
  };

  return (
    <div>
      <table className="table table-striped">
        <thead>
          <tr>
            {/* <td>Inventory ID</td> */}
            <td></td>
            <td>Name</td>
            <td>Type</td>
            <td>Description</td>
            <td></td>
            <td></td>
          </tr>
          <tr>
            {/* <td></td> */}
            <td></td>
            <td>
              <input
                type="text"
                value={inventoryName}
                onChange={e => setInventoryName(e.target.value)}
                onKeyUp={e =>
                  e.key === 'Enter' &&
                  getInventoryList(
                    inventoryName,
                    inventoryType,
                    inventoryDescription
                  )
                }
              />
            </td>
            <td>
              <select
                className="form-control col-sm-4"
                value={inventoryType}
                onChange={e => setInventoryType(e.target.value)}
                onKeyUp={e =>
                  e.key === 'Enter' &&
                  getInventoryList(
                    inventoryName,
                    inventoryType,
                    inventoryDescription
                  )
                }
              >
                <option value="">None</option>
                {inventoryTypeList.map(type => (
                  <option key={type.type}>{type.type}</option>
                ))}
              </select>
            </td>
            <td>
              <input
                type="text"
                value={inventoryDescription}
                onChange={e => setInventoryDescription(e.target.value)}
                onKeyUp={e =>
                  e.key === 'Enter' &&
                  getInventoryList(
                    inventoryName,
                    inventoryType,
                    inventoryDescription
                  )
                }
              />
            </td>
            <td>
              <button
                className="btn btn-success"
                onClick={clearQueries}
                title="Clear"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="32"
                  height="32"
                  fill="white"
                  className="bi bi-x-circle"
                  viewBox="0 0 16 16"
                >
                  <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14m0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16" />
                  <path d="M4.646 4.646a.5.5 0 0 1 .708 0L8 7.293l2.646-2.647a.5.5 0 0 1 .708.708L8.707 8l2.647 2.646a.5.5 0 0 1-.708.708L8 8.707l-2.646 2.647a.5.5 0 0 1-.708-.708L7.293 8 4.646 5.354a.5.5 0 0 1 0-.708" />
                </svg>
              </button>
            </td>
            <td>
              <button
                className="btn btn-success"
                onClick={() =>
                  getInventoryList(
                    inventoryName,
                    inventoryType,
                    inventoryDescription
                  )
                }
                title="Search"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="32"
                  height="32"
                  fill="white"
                  className="bi bi-search"
                  viewBox="0 0 16 16"
                >
                  <path d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001q.044.06.098.115l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85a1 1 0 0 0-.115-.1zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0" />
                </svg>
              </button>
            </td>
          </tr>
        </thead>
        <tbody>
          {inventoryList.map(inventory => (
            <tr key={inventory.inventoryId}>
              <td>
                <input
                  type="checkbox"
                  checked={selectedInventories.some(
                    selectedInventory =>
                      selectedInventory.inventoryId === inventory.inventoryId
                  )}
                  onChange={e => handleInventorySelection(e, inventory)}
                />
              </td>
              {/* <td>{inventory.inventoryId}</td> */}
              <td
                onClick={() =>
                  navigate(`/inventory/${inventory.inventoryId}/products`)
                }
                style={{
                  cursor: 'pointer',
                  textDecoration: 'underline',
                  color: 'blue',
                }}
              >
                {inventory.inventoryName}
              </td>
              <td>{inventory.inventoryType}</td>
              <td>{inventory.inventoryDescription}</td>
              <td>
                <button
                  onClick={e => {
                    e.stopPropagation();
                    navigate(`inventory/${inventory.inventoryId}/edit`);
                  }}
                  className="btn btn-warning"
                >
                  Edit
                </button>
              </td>
              <td>
                <button
                  className="btn btn-danger"
                  onClick={e => {
                    e.stopPropagation();
                    deleteInventoryHandler(inventory);
                  }}
                  title="Delete"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="32"
                    height="32"
                    fill="currentColor"
                    className="bi bi-trash"
                    viewBox="0 0 16 16"
                  >
                    <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z" />
                    <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4zM2.5 3h11V2h-11z" />
                  </svg>
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <div className="text-center">
        <table className="mx-auto">
          <tbody>
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
          </tbody>
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
        className="btn btn-danger"
        onClick={deleteSelectedInventories}
        disabled={selectedInventories.length === 0} // Disable if no items selected
      >
        Delete Selected Inventories
      </button>
      <button
        className="delete-bundle-button btn btn-success mx-1"
        onClick={() => {
          handleDeleteAllInventories(false);
        }}
      >
        Delete All Inventories
      </button>

      <button
        className="add-inventory-button btn btn-success"
        onClick={() => setShowAddInventoryForm(true)}
      >
        Add Inventory
      </button>

      {showAddInventoryForm && (
        <AddInventory
          showAddInventoryForm={showAddInventoryForm}
          handleInventoryClose={() => setShowAddInventoryForm(false)}
          refreshInventoryTypes={refreshInventoryTypes}
        />
      )}

      <button
        className="low-stock-button btn btn-warning mx-1"
        onClick={async () => {
          if (inventoryList.length > 0) {
            setLowStockProductsByInventory({});
            try {
              for (const inventory of inventoryList) {
                await getAllLowStockProducts(inventory);
              }
            } catch (error) {
              console.error('Error fetching low stock products:', error);
            }
          } else {
            console.error('No inventories found');
          }
        }}
      >
        Check Low Stock for All Inventories
      </button>

      {showLowStock && Object.keys(lowStockProductsByInventory).length > 0 && (
        <div>
          <h3>Low Stock Products</h3>
          {Object.entries(lowStockProductsByInventory).map(
            ([inventoryName, products]) => (
              <div key={inventoryName}>
                <h4>Inventory: {inventoryName}</h4>
                <ul>
                  {products.map(product => (
                    <li key={product.productId}>
                      {product.productName}: {product.productQuantity} units
                      left
                    </li>
                  ))}
                </ul>
              </div>
            )
          )}
        </div>
      )}

      <button
        className="add-inventorytype-button btn btn-primary"
        onClick={() => setShowAddTypeForm(true)} // Show the form when clicked
      >
        Add InventoryType
      </button>
      {showAddTypeForm && (
        <AddInventoryType
          show={showAddTypeForm}
          handleClose={() => setShowAddTypeForm(false)}
          refreshInventoryTypes={refreshInventoryTypes} // Pass the function to refresh inventory types
        />
      )}

      {showConfirmDialog && (
        <>
          <div
            className="overlay"
            onClick={() => setShowConfirmDialog(false)}
          ></div>
          <div className="confirm-dialog">
            <p>Are you sure you want to delete all inventories?</p>
            <button
              className={'btn-danger mx-1'}
              onClick={() => handleDeleteAllInventories(true)}
            >
              Yes
            </button>
            <button
              className={'btn-warning mx-1'}
              onClick={() => setShowConfirmDialog(false)}
            >
              No
            </button>
          </div>
        </>
      )}
      <button className="btn btn-primary" onClick={toggleAddSupplyModal}>
        Add Supply
      </button>
      {showAddSupplyModal && (
        <AddSupplyForm
          onClose={toggleAddSupplyModal}
          onSubmit={handleAddSupplySubmit}
        />
      )}
      <div className="card-container-custom">
        {inventoryList.map(inventory => (
          <div
            className="card"
            key={inventory.inventoryId}
            onClick={() =>
              navigate(`/inventory/${inventory.inventoryId}/products`)
            }
            style={{ cursor: 'pointer' }}
          >
            <div className="image-container">
              <img
                src={inventory.inventoryImage}
                alt={inventory.inventoryName}
                className="card-image"
                onError={(e: React.SyntheticEvent<HTMLImageElement, Event>) => {
                  const target = e.target as HTMLImageElement;
                  if (inventory.inventoryBackupImage) {
                    target.src = inventory.inventoryBackupImage;
                    target.onerror = () => {
                      target.onerror = null;
                      target.src = DefaultInventoryImage;
                    };
                  } else {
                    target.src = DefaultInventoryImage;
                  }
                }}
              />
            </div>
            <h2>{inventory.inventoryName}</h2>
            <p>Type: {inventory.inventoryType}</p>
            <p>{inventory.inventoryDescription}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
