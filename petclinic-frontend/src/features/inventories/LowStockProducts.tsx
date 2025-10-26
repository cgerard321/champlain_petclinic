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
        const response = await axiosInstance.get<string>('/inventories/all', {
          useV2: false,
          responseType: 'text',
        });

        const raw = String(response.data ?? '');

        // Parse SSE: split events, take "data:" lines, JSON.parse; flatten arrays
        const items: Inventory[] = raw
          .split(/\r?\n\r?\n/)
          .map(block => {
            const dataLines = block
              .split(/\r?\n/)
              .filter(line => line.startsWith('data:'))
              .map(line => line.slice(5).trim());

            if (dataLines.length === 0) return null;

            const jsonText = dataLines.join('\n').trim();
            if (!jsonText || jsonText === '__END__') return null;

            try {
              const v = JSON.parse(jsonText);
              return Array.isArray(v) ? (v as Inventory[]) : [v as Inventory];
            } catch (e) {
              console.error("Can't parse JSON from SSE event:", e, jsonText);
              return null;
            }
          })
          .filter((x): x is Inventory[] => x !== null)
          .flat();

        setInventories(items);
      } catch (error) {
        console.error('Error fetching inventories:', error);
        setInventories([]); // ensure array to keep downstream logic safe
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
          const response = await axiosInstance.get<ProductResponseModel[]>(
            `/inventories/${inventory.inventoryId}/products/lowstock`,
            { useV2: false, responseType: 'text' }
          );
          const raw = String(response.data ?? '');

          // Parse SSE: blank line–separated events; collect "data:" lines; JSON.parse each
          const parsed: ProductResponseModel[] = raw
            .split(/\r?\n\r?\n/)
            .map(block => {
              const dataLines = block
                .split(/\r?\n/)
                .filter(line => line.startsWith('data:'))
                .map(line => line.slice(5).trim());

              if (dataLines.length === 0) return null;

              const jsonText = dataLines.join('\n').trim();
              if (!jsonText || jsonText === '__END__') return null;

              try {
                const v = JSON.parse(jsonText);
                // server could send single item or an array per event
                return Array.isArray(v)
                  ? (v as ProductResponseModel[])
                  : [v as ProductResponseModel];
              } catch (e) {
                console.error("Can't parse JSON from SSE event:", e, jsonText);
                return null;
              }
            })
            .filter((x): x is ProductResponseModel[] => x !== null)
            .flat();

          const needingRestock = (parsed || []).filter(
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
