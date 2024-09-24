import {useEffect, useState} from "react";
//import {useParams} from "react-router-dom";
import {ProductModel} from "@/features/inventories/models/ProductModels/ProductModel.ts";
import useSearchProducts from "@/features/inventories/hooks/useSearchProducts.ts";
export default function InventoryProducts(): JSX.Element {

    //const { inventoryId } = useParams<{ inventoryId: string }>();
  const inventoryId = '4040d18a-d098-4f60-9c57-6093fa747bb8';

  const [productName, setProductName] = useState('');
  const [productDescription, setProductDescription] = useState('');

  const {
    productList,
    getProductList,
  } = useSearchProducts();

  useEffect(() => {
    getProductList('', '', '');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  });
  const clearQueries = (): void => {
    setProductName('');
    setProductDescription('');
    getProductList(inventoryId, '', '');
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
                  value={productName}
                  onChange={e => setProductName(e.target.value)}
                  onKeyUp={e =>
                      e.key === 'Enter' &&
                      getProductList(
                            inventoryId,
                            productName,
                            productDescription
                      )
                  }
              />
            </td>
            <td></td>
            <td>
              <input
                  type="text"
                  value={productDescription}
                  onChange={e => setProductDescription(e.target.value)}
                  onKeyUp={e =>
                      e.key === 'Enter' &&
                      getProductList(
                          inventoryId,
                          productName,
                          productDescription
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
                      getProductList(
                          inventoryId,
                          productName,
                          productDescription
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
          {productList.map((product: ProductModel) => (
              <tr key={product.productId}>
                  <td>{product.productId}</td>
                  <td>{product.productName}</td>
                  <td>{product.productDescription}</td>
                  <td>${product.productSalePrice}</td>
                  <td>{product.productQuantity}</td>
                  <td
                      style={{
                          color:
                              product.status === 'RE_ORDER'
                                  ? '#f4a460' // Tan for RE_ORDER
                                  : product.status === 'OUT_OF_STOCK'
                                      ? 'red' // Red for OUT_OF_STOCK
                                      : product.status === 'AVAILABLE'
                                          ? 'green' // Green for AVAILABLE
                                          : 'inherit', // Default color
                      }}
                  >
                  {product.status.replace('_', ' ')}
                  </td>
              </tr>
              ))}
            </tbody>
        </table>
        <div id="loadingObject" style={{ display: 'none' }}>
          Loading...
        </div>
      </div>
  );
}
