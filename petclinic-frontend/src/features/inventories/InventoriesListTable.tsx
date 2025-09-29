import { useState, useEffect, JSX, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
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
// import axios from 'axios';
import axiosInstance from '@/shared/api/axiosInstance';
import { toggleInventoryImportant } from './api/toggleInventoryImportant';

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
  const lowStockProductsByInventory = useRef<{
    [inventoryName: string]: ProductModel[];
  }>({});

  const [productQuantities, setProductQuantities] = useState<{
    [key: string]: number;
  }>({});
  const [openMenuId, setOpenMenuId] = useState<string | null>(null);
  const [isActionsMenuVisible, setActionsMenu] = useState(false);

  const [showImportantOnly, setShowImportantOnly] = useState(false);

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
    updateFilters,
  } = useSearchInventories();

  useEffect(() => {
    getInventoryList('', '', '', showImportantOnly);
    fetchAllInventoryTypes();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage]);

  const refreshInventoryTypes = async (): Promise<void> => {
    await fetchAllInventoryTypes();
  };

  useEffect(() => {
    getInventoryList('', '', '', showImportantOnly);
    refreshInventoryTypes();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage]);

  const handleInventoryNameChange = (value: string): void => {
    setInventoryName(value);
    updateFilters({
      inventoryName: value,
      inventoryType,
      inventoryDescription,
      importantOnly: showImportantOnly,
    });
  };

  const handleInventoryTypeChange = (value: string): void => {
    setInventoryType(value);
    updateFilters({
      inventoryName,
      inventoryType: value,
      inventoryDescription,
      importantOnly: showImportantOnly,
    });
  };

  const handleInventoryDescriptionChange = (value: string): void => {
    setInventoryDescription(value);
    updateFilters({
      inventoryName,
      inventoryType,
      inventoryDescription: value,
      importantOnly: showImportantOnly,
    });
  };

  const handleToggleImportant = async (
    e: React.MouseEvent,
    inventory: Inventory
  ): Promise<void> => {
    e.stopPropagation();
    try {
      const newImportantStatus = !inventory.important;
      await toggleInventoryImportant(inventory.inventoryId, newImportantStatus);

      const updatedList = inventoryList.map(inv =>
        inv.inventoryId === inventory.inventoryId
          ? { ...inv, important: newImportantStatus }
          : inv
      );
      setInventoryList(updatedList);
    } catch (error) {
      console.error('Error toggling important status:', error);
      alert('Failed to update important status. Please try again.');
    }
  };

  const clearQueries = (): void => {
    setInventoryName('');
    setInventoryType('');
    setInventoryDescription('');
    setShowImportantOnly(false);
    updateFilters({
      inventoryName: '',
      inventoryType: '',
      inventoryDescription: '',
      importantOnly: false,
    });
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
      const response = await axiosInstance.get<number>(
        `/inventories/${inventoryId}/productquantity`,
        { useV2: false }
      );

      const quantity = await response.data;
      setProductQuantities(prevQuantities => ({
        ...prevQuantities,
        [inventoryId]: quantity,
      }));
    } catch (error) {
      console.error('Error fetching product quantity:', error);
    }
  };

  const getAllLowStockProducts = async (
    inventory: Inventory
  ): Promise<void> => {
    try {
      const response = await axiosInstance.get<ProductModel[]>(
        `/inventories/${inventory.inventoryId}/products/lowstock`,
        { useV2: false }
      );
      const data = response.data;
      if (data && data.length > 0) {
        // Update the ref directly
        lowStockProductsByInventory.current = {
          ...lowStockProductsByInventory.current,
          [inventory.inventoryName]: data,
        };
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

  const location = useLocation();
  const lastConsultedInventoryId =
    location.state?.lastConsultedInventoryId || null;

  const handleCardClick = (inventoryId: string): void => {
    navigate(`/inventories/${inventoryId}/products`, {
      state: { lastConsultedInventoryId: inventoryId },
    });
  };

  const arrayBufferToBase64 = (buffer: Uint8Array): string => {
    let binary = '';
    const bytes = new Uint8Array(buffer);
    const len = bytes.byteLength;
    for (let i = 0; i < len; i++) {
      binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
  };

  const toggleActionsMenu = (): void => {
    setActionsMenu(prevState => !prevState);
  };

  return (
    <>
      <div id={inventoryStyles.menuSection}>
        <div id={inventoryStyles.menuContainer}>
          <svg
            onClick={toggleActionsMenu}
            id={inventoryStyles.menuIcon}
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 16 16"
            fill="currentColor" // bars follow CSS 'color'
            className="bi bi-list"
            role="button"
            aria-label="Open menu"
          >
            <path
              fillRule="evenodd"
              d="M2.5 12a.5.5 0 0 1 .5-.5h10a.5.5 0 0 1 0 1H3a.5.5 0 0 1-.5-.5m0-4a.5.5 0 0 1 .5-.5h10a.5.5 0 0 1 0 1H3a.5.5 0 0 1-.5-.5m0-4a.5.5 0 0 1 .5-.5h10a.5.5 0 0 1 0 1H3a.5.5 0 0 1-.5-.5"
            />
          </svg>

          {isActionsMenuVisible && (
            <div id={inventoryStyles.actionsMenu}>
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
              <button
                className="low-stock-button btn btn-warning mx-1"
                onClick={async () => {
                  if (inventoryList.length > 0) {
                    lowStockProductsByInventory.current = {}; // Clear the current ref value
                    try {
                      // Collect all low stock products for each inventory
                      for (const inventory of inventoryList) {
                        await getAllLowStockProducts(inventory);
                      }

                      // Navigate to the other page and pass the collected data
                      navigate('/products/lowstock', {
                        state: {
                          lowStockProducts: lowStockProductsByInventory.current,
                        },
                      });
                    } catch (error) {
                      console.error(
                        'Error fetching low stock products:',
                        error
                      );
                    }
                  } else {
                    console.error('No inventories found');
                  }
                }}
              >
                Check Low Stock for All Inventories
              </button>
              <button
                className="add-inventorytype-button btn btn-primary"
                onClick={() => setShowAddTypeForm(true)}
              >
                Add InventoryType
              </button>
            </div>
          )}
        </div>
      </div>
      <div>
        <table
          className={`table table-striped${inventoryStyles.inventoryTable} ${inventoryStyles.fixedTable} ${inventoryStyles.cleanTable}`}
        >
          <thead>
            <tr>
              <td></td>
              <td style={{ fontWeight: 'bold' }}>Name</td>
              <td style={{ fontWeight: 'bold' }}>Type</td>
              <td style={{ fontWeight: 'bold' }}>Description</td>
              <td className="text-center" style={{ fontWeight: 'bold' }}>
                Important
              </td>
              <td className="text-center" style={{ fontWeight: 'bold' }}>
                Clear
              </td>
              <td></td>
            </tr>
            <tr>
              <td></td>
              <td>
                <input
                  type="text"
                  value={inventoryName}
                  onChange={e => handleInventoryNameChange(e.target.value)}
                />
              </td>
              <td>
                <select
                  className="form-control col-sm-4"
                  value={inventoryType}
                  onChange={e => handleInventoryTypeChange(e.target.value)}
                >
                  <option value="">None</option>
                  {inventoryTypeList.map(type => (
                    <option key={type.type} value={type.type}>
                      {type.type}
                    </option>
                  ))}
                </select>
              </td>
              <td>
                <input
                  type="text"
                  value={inventoryDescription}
                  onChange={e =>
                    handleInventoryDescriptionChange(e.target.value)
                  }
                />
              </td>
              <td className="text-center align-middle">
                <div className="form-check d-inline-flex align-items-center justify-content-center m-0">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    checked={showImportantOnly}
                    onChange={e => {
                      const value = e.target.checked;
                      setShowImportantOnly(value);
                      updateFilters({
                        inventoryName,
                        inventoryType,
                        inventoryDescription,
                        importantOnly: value,
                      });
                    }}
                  />
                </div>
              </td>
              <td>
                <button
                  className="btn btn-primary"
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
              <td></td>
            </tr>
          </thead>
        </table>
        {inventoryList.length === 0 &&
          (inventoryName !== '' ||
            inventoryType !== '' ||
            inventoryDescription !== '') && (
            <div className="text-center p-4">
              <div className="alert alert-info">
                <h5>No inventory found</h5>
                <p>No inventories match your current search criteria.</p>
              </div>
            </div>
          )}
        {/*//Cards start here*/}
        <div className={cardStylesInventory.cardContainerCustom}>
          {inventoryList.map(inventory => (
            <div
              className={`
              ${cardStylesInventory.card} 
              ${
                inventory.inventoryId === lastConsultedInventoryId
                  ? cardStylesInventory.highlightedCard
                  : ''
              }`}
              key={inventory.inventoryName}
              onClick={() => handleCardClick(inventory.inventoryId)}
              onMouseLeave={() => setOpenMenuId(null)}
              style={{ cursor: 'pointer' }}
            >
              <div className={cardStylesInventory.imageContainer}>
                <img
                  src={
                    inventory.imageUploaded instanceof Uint8Array
                      ? `data:image/jpeg;base64,${arrayBufferToBase64(inventory.imageUploaded)}`
                      : `data:image/jpeg;base64,${inventory.imageUploaded}`
                  }
                  alt={inventory.inventoryName}
                  className={cardStylesInventory.cardImage}
                />
              </div>
              <div className={cardStylesInventory.inventoryNameSection}>
                <p id={cardStylesInventory.inventoryNameText}>
                  {inventory.inventoryName}
                  <svg
                    onClick={e => handleToggleImportant(e, inventory)}
                    style={{
                      marginLeft: '10px',
                      cursor: 'pointer',
                      fill: inventory.important ? '#FFD700' : '#D3D3D3',
                    }}
                    xmlns="http://www.w3.org/2000/svg"
                    width="20"
                    height="20"
                    viewBox="0 0 16 16"
                  >
                    <path d="M3.612 15.443c-.386.198-.824-.149-.746-.592l.83-4.73L.173 6.765c-.329-.314-.158-.888.283-.95l4.898-.696L7.538.792c.197-.39.73-.39.927 0l2.184 4.327 4.898.696c.441.062.612.636.282.95l-3.522 3.356.83 4.73c.078.443-.36.79-.746.592L8 13.187l-4.389 2.256z" />
                  </svg>
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
                        navigate(`/inventories/${inventory.inventoryId}/edit`);
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

        {showAddInventoryForm && (
          <AddInventory
            showAddInventoryForm={showAddInventoryForm}
            handleInventoryClose={() => setShowAddInventoryForm(false)}
            refreshInventoryTypes={refreshInventoryTypes}
          />
        )}

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
