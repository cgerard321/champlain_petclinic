import { useState, useEffect, JSX } from 'react';
import { useNavigate } from 'react-router-dom';
import { Inventory } from '@/features/inventories/models/Inventory.ts';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import useSearchInventories from '@/features/inventories/hooks/useSearchInventories.ts';
import { getAllInventoryTypes } from '@/features/inventories/api/getAllInventoryTypes.ts';
import deleteAllInventories from '@/features/inventories/api/deleteAllInventories.ts';
import deleteInventory from '@/features/inventories/api/deleteInventory.ts';
import AddInventory from '@/features/inventories/AddInventoryForm.tsx';
import AddInventoryType from '@/features/inventories/AddInventoryType.tsx';
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';
import inventoryStyles from './InventoriesListTable.module.css';
import cardStylesInventory from './CardInventoryTeam.module.css';
import DefaultInventoryImage from '@/assets/Inventory/DefaultInventoryImage.jpg';

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
  const navigate = useNavigate();
  const [lowStockProductsByInventory, setLowStockProductsByInventory] =
    useState<{ [inventoryName: string]: ProductModel[] }>({});
  const [showLowStock, setShowLowStock] = useState(false);
  const [productQuantities, setProductQuantities] = useState<{
    [key: string]: number;
  }>({});
  const [openMenuId, setOpenMenuId] = useState<string | null>(null);

  const handleMenuClick = (
    e: React.MouseEvent<SVGElement>,
    inventoryId: string
  ): void => {
    e.stopPropagation();
    setOpenMenuId(openMenuId === inventoryId ? null : inventoryId);
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

  const refreshInventoryTypes = async (): Promise<void> => {
    await fetchAllInventoryTypes();
  };

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
    if (currentPage > 0) {
      setCurrentPage(prevPage => prevPage - 1);
    }
  };
  const pageAfter = (): void => {
    if (inventoryList.length > 0) {
      setCurrentPage(prevPage => prevPage + 1);
    }
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

  useEffect(() => {
    if (inventoryList.length > 0) {
      inventoryList.forEach(inventory => {
        fetchProductQuantity(inventory.inventoryId);
      });
    }
  }, [inventoryList]);

  const fetchProductQuantity = async (inventoryId: string): Promise<void> => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/v2/gateway/inventories/${inventoryId}/productquantity`,
        {
          method: 'GET',
          credentials: 'include', // <-- Add this line to include cookies or credentials
          headers: { 'Content-Type': 'application/json' },
        }
      );

      if (response.ok) {
        const quantity = await response.json();
        setProductQuantities(prevQuantities => ({
          ...prevQuantities,
          [inventoryId]: quantity,
        }));
      } else {
        console.error('Failed to fetch product quantity:', response.statusText);
      }
    } catch (error) {
      console.error('Error fetching product quantity:', error);
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
    <>
      <div>
        <table className="table table-striped">
          <thead>
            <tr>
              <td></td>
              <td style={{ fontWeight: 'bold' }}>Name</td>
              <td style={{ fontWeight: 'bold' }}>Type</td>
              <td style={{ fontWeight: 'bold' }}>Description</td>
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
                  className="btn btn-info"
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
                  className="btn btn-info"
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
              <td></td>
            </tr>
          </thead>
        </table>
        {/*//Cards start here*/}
        <div className={cardStylesInventory.cardContainerCustom}>
          {inventoryList.map(inventory => (
            <div
              className={cardStylesInventory.card}
              key={inventory.inventoryName}
              onClick={() =>
                navigate(`/inventory/${inventory.inventoryId}/products`)
              }
              onMouseLeave={() => setOpenMenuId(null)}
              style={{ cursor: 'pointer' }}
            >
              <div className={cardStylesInventory.imageContainer}>
                <img
                  src={inventory.inventoryImage}
                  alt={inventory.inventoryName}
                  className={cardStylesInventory.cardImage}
                  onError={(
                    e: React.SyntheticEvent<HTMLImageElement, Event>
                  ) => {
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
              <div className={cardStylesInventory.inventoryNameSection}>
                <p id={cardStylesInventory.inventoryNameText}>
                  {inventory.inventoryName}
                </p>
                <div id={cardStylesInventory.iconSection}>
                  <p id={cardStylesInventory.productQuantityNumber}>
                    {productQuantities[inventory.inventoryId] !== undefined
                      ? productQuantities[inventory.inventoryId]
                      : 'Loading...'}{' '}
                  </p>
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="16"
                    height="16"
                    fill="currentColor"
                    className={`bi bi-box-fill ${cardStylesInventory.iconCustomized}`}
                    viewBox="0 0 16 16"
                  >
                    <path
                      fillRule="evenodd"
                      d="M15.528 2.973a.75.75 0 0 1 .472.696v8.662a.75.75 0 0 1-.472.696l-7.25 2.9a.75.75 0 0 1-.557 0l-7.25-2.9A.75.75 0 0 1 0 12.331V3.669a.75.75 0 0 1 .471-.696L7.443.184l.004-.001.274-.11a.75.75 0 0 1 .558 0l.274.11.004.001zm-1.374.527L8 5.962 1.846 3.5 1 3.839v.4l6.5 2.6v7.922l.5.2.5-.2V6.84l6.5-2.6v-.4l-.846-.339Z"
                    />
                  </svg>
                </div>
              </div>
              <div id={cardStylesInventory.cardTypeSection}>
                <p>Type: {inventory.inventoryType}</p>
              </div>
              <div id={cardStylesInventory.cardDescriptionSection}>
                <p>{inventory.inventoryDescription}</p>
              </div>
              <div className={cardStylesInventory.checkboxSection}>
                <svg
                  id={cardStylesInventory.cardMenu}
                  onClick={e => handleMenuClick(e, inventory.inventoryId)}
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  fill="currentColor"
                  className="bi bi-pencil-square"
                  viewBox="0 0 16 16"
                >
                  <path d="M15.502 1.94a.5.5 0 0 1 0 .706L14.459 3.69l-2-2L13.502.646a.5.5 0 0 1 .707 0l1.293 1.293zm-1.75 2.456-2-2L4.939 9.21a.5.5 0 0 0-.121.196l-.805 2.414a.25.25 0 0 0 .316.316l2.414-.805a.5.5 0 0 0 .196-.12l6.813-6.814z" />
                  <path
                    fillRule="evenodd"
                    d="M1 13.5A1.5 1.5 0 0 0 2.5 15h11a1.5 1.5 0 0 0 1.5-1.5v-6a.5.5 0 0 0-1 0v6a.5.5 0 0 1-.5.5h-11a.5.5 0 0 1-.5-.5v-11a.5.5 0 0 1 .5-.5H9a.5.5 0 0 0 0-1H2.5A1.5 1.5 0 0 0 1 2.5z"
                  />
                </svg>
                {openMenuId === inventory.inventoryId && (
                  <div className={cardStylesInventory.popupMenuDiv}>
                    <button
                      onClick={e => {
                        e.stopPropagation();
                        navigate(`inventory/${inventory.inventoryId}/edit`);
                      }}
                      className="btn btn-warning"
                    >
                      Edit
                    </button>
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
                  </div>
                )}
                <input
                  id={cardStylesInventory.checkboxCard}
                  type="checkbox"
                  checked={selectedInventories.some(
                    selectedInventory =>
                      selectedInventory.inventoryId === inventory.inventoryId
                  )}
                  onChange={e => handleInventorySelection(e, inventory)}
                  onClick={e => e.stopPropagation()}
                />
              </div>
            </div>
          ))}
        </div>
        <div
          className="d-flex justify-content-center"
          style={{ marginBottom: '100px' }}
        >
          <div className="text-center">
            <table>
              <tbody>
                <tr>
                  <td>
                    <button
                      className="btn btn-primary btn-sm"
                      onClick={pageBefore}
                    >
                      &lt;
                    </button>
                  </td>
                  <td>
                    <span className="mx-2">{realPage}</span>{' '}
                    {/* Added margin for space */}
                  </td>
                  <td>
                    <button
                      className="btn btn-primary btn-sm"
                      onClick={pageAfter}
                      disabled={inventoryList.length === 0}
                    >
                      &gt;
                    </button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
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
          disabled={selectedInventories.length === 0}
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

        {showLowStock &&
          Object.keys(lowStockProductsByInventory).length > 0 && (
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
          onClick={() => setShowAddTypeForm(true)}
        >
          Add InventoryType
        </button>
        {showAddTypeForm && (
          <AddInventoryType
            show={showAddTypeForm}
            handleClose={() => setShowAddTypeForm(false)}
            refreshInventoryTypes={refreshInventoryTypes}
          />
        )}

        {showConfirmDialog && (
          <>
            <div
              className={inventoryStyles.overlay}
              onClick={() => setShowConfirmDialog(false)}
            ></div>
            <div className={inventoryStyles.confirmDialog}>
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
      </div>
    </>
  );
}
