import { useState, useEffect, JSX } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import { getProductByProductId } from '@/features/products/api/getProductByProductId.tsx'; // Import the function
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';
import './ProductList.css';
// import { patchGetProduct } from './api/patchGetProduct';

export default function ProductList(): JSX.Element {
  const [productList, setProductList] = useState<ProductModel[]>([]); // List of all products
  const [selectedProduct, setSelectedProduct] = useState<ProductModel | null>(null); // Selected product details

  const fetchProducts = async (): Promise<void> => {
    const list = await getAllProducts();
    setProductList(list);
  };

  useEffect(() => {
    fetchProducts();
  }, []);




  const handleProductClick = async (productId: string): Promise<void> => {
    try {

      const product = await getProductByProductId(productId);
      // First, execute patchGetProduct
      // await patchGetProduct(productId);
  
      // Then, execute getProductByProductId
      
  
      // Set the product details
      setSelectedProduct(product);
    } catch (error) {
      console.error('Failed to fetch product details:', error);
    }
  };

  const handleBackToList = (): void => {
    setSelectedProduct(null); 
  };


  if (selectedProduct) {
    return (

      <div>
        <h1>{selectedProduct.productName}</h1>
        <p>{selectedProduct.productDescription}</p>
        <p>Price: ${selectedProduct.productSalePrice.toFixed(2)}</p>
        <button onClick={handleBackToList}>Back to Products</button>
      </div>
    );
  }


  return (
    <div>
      <div className="grid">
        {productList
          .filter(data => data != null)
          .map((product: ProductModel) => (
            <div className="card" key={product.productId}>
              <h2 
                onClick={() => handleProductClick(product.productId)} 
                style={{ cursor: 'pointer', color: 'blue', textDecoration: 'underline' }} // Make name clickable
              >
                {product.productName}
              </h2>
              <p>{product.productDescription}</p>
              <p>Price: ${product.productSalePrice.toFixed(2)}</p>
            </div>
          ))}
      </div>
    </div>
  );
}
