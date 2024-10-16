import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Inventory } from '@/features/inventories/models/Inventory';
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel';

const LowStockPage: React.FC = () => {
  const [lowStockProductsByInventory, setLowStockProductsByInventory] =
    useState<{ [inventoryName: string]: ProductModel[] }>({});
  const [inventories, setInventories] = useState<Inventory[]>([]);
  const navigate = useNavigate();

  // Fetch inventories from the backend
  useEffect(() => {
    const fetchInventories = async (): Promise<void> => {
      try {
        const response = await axios.get<Inventory[]>(
          'http://localhost:8080/api/v2/gateway/inventories',
          {
            withCredentials: true,
          }
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
      const lowStockData: { [key: string]: ProductModel[] } = {};
      const promises = inventories.map(async inventory => {
        try {
          const response = await axios.get<ProductModel[]>(
            `http://localhost:8080/api/gateway/inventory/${inventory.inventoryId}/products/lowstock`,
            {
              withCredentials: true,
              params: { threshold: 20 },
            }
          );
          if (response.data && response.data.length > 0) {
            lowStockData[inventory.inventoryName] = response.data;
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

  return (
    <div className="low-stock-page">
      <button
        className="btn btn-secondary"
        onClick={() => navigate('/inventories')}
      >
        Back to Inventories
      </button>

      <div className="low-stock-products">
        {Object.keys(lowStockProductsByInventory).length > 0 ? (
          Object.keys(lowStockProductsByInventory).map(inventoryName => (
            <div key={inventoryName} className="inventory-section">
              <h3>Low Stock Supplies for Inventory: {inventoryName}</h3>
              <table className="table table-striped">
                <thead>
                  <tr>
                    <th>Supply Name</th>
                    <th>Description</th>
                    <th>Price</th>
                    <th>Quantity</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {lowStockProductsByInventory[inventoryName].map(
                    (product: ProductModel) => (
                      <tr key={product.productId}>
                        <td>{product.productName}</td>
                        <td>{product.productDescription}</td>
                        <td>${product.productSalePrice}</td>
                        <td>{product.productQuantity}</td>
                        <td
                          style={{
                            color:
                              product.status === 'PRE_ORDER'
                                ? '#f4a460'
                                : product.status === 'OUT_OF_STOCK'
                                  ? 'red'
                                  : 'green',
                          }}
                        >
                          {product.status.replace('_', ' ')}
                        </td>
                      </tr>
                    )
                  )}
                </tbody>
              </table>
            </div>
          ))
        ) : (
          <p>No low stock supplies found across all inventories.</p>
        )}
      </div>
    </div>
  );
};

export default LowStockPage;
