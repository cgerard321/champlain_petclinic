import { useState, useEffect, JSX } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import './ProductList.css';
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';

export default function ProductList(): JSX.Element {
  const [productList, setProductList] = useState<ProductModel[]>([]);

  const fetchProducts = async (): Promise<void> => {
    const list = await getAllProducts();
    setProductList(list);
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  return (
    <div>
      <div className="grid">
        {productList
          .filter(data => data != null)
          .map((product: ProductModel) => (
            <div className="card" key={product.productId}>
              <h2>{product.productName}</h2>
              <p>{product.productDescription}</p>
              <p>Price: ${product.productSalePrice.toFixed(2)}</p>
              <p>Quantity: {product.productQuantity}</p>
              <p>Status: {product.status}</p>
            </div>
          ))}
      </div>
    </div>
  );
}
