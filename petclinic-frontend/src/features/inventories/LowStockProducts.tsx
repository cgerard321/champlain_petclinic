import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
// import axios from 'axios';
import { Inventory } from '@/features/inventories/models/Inventory';
import { ProductResponseModel } from '@/features/inventories/models/InventoryModels/ProductResponseModel.ts';
import 'bootstrap/dist/css/bootstrap.min.css';
import { Status } from '@/features/inventories/models/ProductModels/Status.ts';
import axiosInstance from '@/shared/api/axiosInstance';

const LowStockPage: React.FC = () => {
  const [lowStockProductsByInventory, setLowStockProductsByInventory] =
    useState<{ [inventoryName: string]: ProductResponseModel[] }>({});
  const [inventories, setInventories] = useState<Inventory[]>([]);
  const [showRestockForm, setShowRestockForm] = useState<boolean>(false);
  const [selectedProduct, setSelectedProduct] =
    useState<ProductResponseModel | null>(null);
  const [restockQuantity, setRestockQuantity] = useState<number>(0);
  const navigate = useNavigate();
  const [totalPrice, setTotalPrice] = useState<number>(0);
  const [selectedInventoryId, setSelectedInventoryId] = useState<string | null>(
    null
  );

  // Fetch inventories from the backend
  useEffect(() => {
    const fetchInventories = async (): Promise<void> => {
      try {
        const response = await axiosInstance.get<Inventory[]>(
          '/inventories/all',
          { useV2: false }
        );
        setInventories(response.data);
      } catch (error) {
        console.error('Error fetching inventories:', error);
      }
    };

    fetchInventories();
  }, []);

  // Fetch low stock products for all inventories
  useEffect(() => {
    const fetchAllLowStockProducts = async (): Promise<void> => {
      const lowStockData: { [key: string]: ProductResponseModel[] } = {};
      const promises = inventories.map(async inventory => {
        try {
          const { data } = await axiosInstance.get<ProductResponseModel[]>(
            `/inventories/${inventory.inventoryId}/products/lowstock`,
            { useV2: false }
          );
          const needingRestock = (data || []).filter(
            p =>
              p.status === Status.RE_ORDER || p.status === Status.OUT_OF_STOCK
          );

          if (needingRestock.length > 0) {
            lowStockData[inventory.inventoryId] = needingRestock;
          }
        } catch (error) {
          console.error(
            `Error fetching low stock supplies for inventory ${inventory.inventoryName}:`,
            error
          );
        }
      });

      await Promise.all(promises);
      setLowStockProductsByInventory(lowStockData);
    };

    if (inventories.length > 0) {
      fetchAllLowStockProducts();
    }
  }, [inventories]);

  const restockProduct = async (
    inventoryId: string,
    productId: string,
    quantity: number
  ): Promise<void> => {
    try {
      const response = await axiosInstance.put(
        `/inventories/${inventoryId}/products/${productId}/restockProduct`,
        null,
        {
          params: { productQuantity: quantity },
          useV2: false,
        }
      );

      // eslint-disable-next-line
      console.log('Restocked product:', response.data);

      alert(
        `Successfully restocked ${quantity} units for ${selectedProduct?.productName}.`
      );

      window.location.reload();

      setShowRestockForm(false);
      setSelectedProduct(null);
      setRestockQuantity(0);
    } catch (error) {
      console.error(`Error restocking product ${productId}:`, error);
    }
  };

  return (
    <div className="low-stock-page">
      <button
        className="btn btn-secondary"
        onClick={() => navigate('/inventories')}
      >
        Back to Inventories
      </button>
      <div className="low-stock-products">
        {Object.entries(lowStockProductsByInventory).length > 0 ? (
          Object.entries(lowStockProductsByInventory).map(
            ([invId, products]) => {
              const inv = inventories.find(i => i.inventoryId === invId);
              const invName = inv?.inventoryName ?? invId;

              return (
                <div key={invId} className="inventory-section">
                  <h3>Low Stock Supplies for Inventory: {invName}</h3>
                  <table className="table table-striped">
                    <thead>
                      <tr>
                        <th>Supply Name</th>
                        <th>Description</th>
                        <th>Price</th>
                        <th>Quantity</th>
                        <th>Status</th>
                        <th>Restock</th>
                      </tr>
                    </thead>
                    <tbody>
                      {products.map(product => (
                        <tr key={product.productId}>
                          <td>{product.productName}</td>
                          <td>{product.productDescription}</td>
                          <td>${product.productSalePrice}</td>
                          <td>{product.productQuantity}</td>
                          <td
                            style={{
                              color:
                                product.status === Status.RE_ORDER
                                  ? '#f4a460'
                                  : product.status === Status.OUT_OF_STOCK
                                    ? 'red'
                                    : product.status === Status.AVAILABLE
                                      ? 'green'
                                      : 'inherit',
                            }}
                          >
                            {product.status.replace('_', ' ')}
                          </td>
                          <td>
                            <button
                              className="btn btn-primary"
                              onClick={() => {
                                setSelectedProduct(product);
                                setSelectedInventoryId(invId); // << keep the context
                                setShowRestockForm(true);
                              }}
                            >
                              Restock
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              );
            }
          )
        ) : (
          <p>No low stock supplies found across all inventories.</p>
        )}
      </div>

      {/* Restock Form Modal (Bootstrap) */}
      {showRestockForm && selectedProduct && (
        <div className="modal fade show d-block" tabIndex={-1} role="dialog">
          <div className="modal-dialog" role="document">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">
                  Restock Supply: {selectedProduct.productName}
                </h5>
                <button
                  type="button"
                  className="close"
                  onClick={() => setShowRestockForm(false)}
                >
                  <span>&times;</span>
                </button>
              </div>
              <div className="modal-body">
                <form
                  onSubmit={e => {
                    e.preventDefault();
                    if (selectedProduct && selectedInventoryId) {
                      restockProduct(
                        selectedInventoryId,
                        selectedProduct.productId,
                        restockQuantity
                      );
                    } else {
                      console.error('Inventory not found');
                    }
                  }}
                >
                  <label htmlFor="quantity">Enter Quantity:</label>
                  <input
                    type="number"
                    id="quantity"
                    value={restockQuantity}
                    onChange={e => {
                      const quantity = parseInt(e.target.value, 10);
                      setRestockQuantity(quantity);

                      if (selectedProduct) {
                        setTotalPrice(
                          quantity * selectedProduct.productSalePrice
                        );
                      }
                    }}
                    min="1"
                    required
                  />

                  <div>
                    <label>Total Price: </label>
                    <span>${totalPrice.toFixed(2)}</span>
                  </div>

                  <button type="submit" className="btn btn-primary">
                    Submit
                  </button>
                  <button
                    type="button"
                    className="btn btn-secondary"
                    onClick={() => setShowRestockForm(false)}
                  >
                    Cancel
                  </button>
                </form>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default LowStockPage;
