import { useState, useEffect, JSX } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import './ProductList.css';
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';
import {getProductsByType} from "@/features/products/api/getProductsByType.ts";

export default function ProductList(): JSX.Element {
  const [productList, setProductList] = useState<ProductModel[]>([]);
  const [filterType, setFilterType] = useState<string>(''); // Add state for filter


  const fetchProducts = async (): Promise<void> => {


      if (filterType.trim() == '') {
          const list = await getAllProducts();
          setProductList(list);
      } else {
          const filteredList = await getProductsByType(filterType);
          setProductList(filteredList);
      }
  };


    useEffect(() => {
    fetchProducts();
  }, [filterType]);

  return (
      <div>
        <input
            type="text"
            placeholder="Enter product type"
            value={filterType}
            onChange={e => setFilterType(e.target.value)} // Handle filter input

        />
        <div className="grid">
          {productList.map((product: ProductModel) => (
              <div className="card" key={product.productId}>
                <h2>{product.productName}</h2>
                <p>{product.productDescription}</p>
                <p>Price: ${product.productSalePrice.toFixed(2)}</p>
                <p>Type: {product.productType}</p> {/* Display product type */}
              </div>
          ))}
        </div>
      </div>
  );

  return (
    <div>
      <div className="grid">
        {productList
          .filter(data => data != null)
          .map(
            (product: {
              productId: string;
              productName: string;
              productDescription: string;
              productSalePrice: number;
            }) => (
              <div className="card" key={product.productId}>
                <h2>{product.productName}</h2>
                <p>{product.productDescription}</p>
                <p>Price: ${product.productSalePrice.toFixed(2)}</p>
              </div>
            )
          )}
      </div>
    </div>
  );
}
