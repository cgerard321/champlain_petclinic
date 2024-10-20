import { JSX, useEffect, useState } from 'react';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import { getProductByProductId } from '@/features/products/api/getProductByProductId.tsx';
import ImageContainer from './ImageContainer';
import { changeProductQuantity } from '../api/changeProductQuantity';
import { generatePath, useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import StarRating from './StarRating';
import './Product.css';
import {addToCartFromProducts} from "@/features/carts/api/addToCartFromProducts.ts";

export default function Product({
  product,
}: {
  product: ProductModel;
}): JSX.Element {
  const [currentProduct, setCurrentProduct] = useState<ProductModel>(product);
  const [selectedProduct, setSelectedProduct] = useState<ProductModel | null>(
    null
  );
  const [selectedProductForQuantity, setSelectedProductForQuantity] =
    useState<ProductModel | null>(null);
  const [quantity, setQuantity] = useState<number>(0);
  const [tooLong, setTooLong] = useState<boolean>(false);

  const navigate = useNavigate();

  const handleProductTitleClick = (): void => {
    navigate(
      generatePath(AppRoutePaths.ProductDetails, {
        productId: product.productId,
      })
    );
  };

  useEffect(() => {
    if (product.productDescription.length > 100) {
      setTooLong(true);
    } else {
      setTooLong(false);
    }
  }, [product.productDescription]);

  // const handleProductClick = async (productId: string): Promise<void> => {
  //   try {
  //     const product = await getProductByProductId(productId);
  //     setSelectedProduct(product);
  //   } catch (error) {
  //     console.error('Failed to fetch product details:', error);
  //   }
  // };

  const handleProductClickForProductQuantity = async (
    productId: string
  ): Promise<void> => {
    try {
      const product = await getProductByProductId(productId);
      setSelectedProductForQuantity(product);
      setQuantity(product.productQuantity);
    } catch (error) {
      console.error('Failed to fetch product details:', error);
    }
  };

  const handleQuantitySubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    if (selectedProductForQuantity) {
      try {
        await changeProductQuantity(
          selectedProductForQuantity.productId,
          quantity
        );
        const updatedProduct = await getProductByProductId(
          selectedProductForQuantity.productId
        );
        setCurrentProduct(updatedProduct);
        setSelectedProductForQuantity(null); // Close the quantity update form
      } catch (error) {
        console.error('Failed to update product quantity:', error);
      }
    }
  };

  const handleBackToList = (): void => {
    setSelectedProduct(null);
    setSelectedProductForQuantity(null);
  };

  const handleAddToCart = async (): Promise<void> => {
    try {
      await addToCartFromProducts.addToCart(currentProduct.productId);
      console.log('Added to cart:', currentProduct.productId);
    } catch (error) {
      console.error('Failed to add product to cart:', error);
    }
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

  if (selectedProductForQuantity) {
    return (
      <div>
        <h3>Change Product Quantity</h3>
        <h2>{selectedProductForQuantity.productName}</h2>
        <form onSubmit={handleQuantitySubmit}>
          <label>
            Quantity:
            <input
              type="number"
              value={quantity}
              onChange={e => setQuantity(parseInt(e.target.value))}
              placeholder="Enter new quantity"
            />
          </label>
          <button type="submit">Update Quantity</button>
        </form>
        <button onClick={handleBackToList}>Back to Products</button>
      </div>
    );
  }

  return (
      <div
          className={`card ${
              product.productQuantity === 0
                  ? 'out-of-stock'
                  : product.productQuantity < 10
                      ? 'low-quantity'
                      : ''
          }`}
          key={product.productId}
      >
        <ImageContainer imageId={product.imageId}/>
        <span
            onClick={() => handleProductClickForProductQuantity(product.productId)}
            style={{cursor: 'pointer', color: 'blue', fontWeight: 'bold'}}
        >
        +
      </span>

        <h2
            onClick={handleProductTitleClick}
            style={{
              cursor: 'pointer',
              color: 'blue',
              textDecoration: 'underline',
            }}
        >
          {currentProduct.productName}
        </h2>
        <p>
          {!tooLong
              ? currentProduct.productDescription
              : `${currentProduct.productDescription.substring(0, 100)}...`}
        </p>
        <p>Price: ${currentProduct.productSalePrice.toFixed(2)}</p>

        <button onClick={handleAddToCart} disabled={product.productQuantity === 0}>
          {product.productQuantity === 0 ? 'Out of Stock' : 'Add to Cart'}
        </button>

        <StarRating
            currentRating={currentProduct.averageRating}
            viewOnly={true}
        />
        {currentProduct.productStatus === 'PRE_ORDER' && (
            <div
                style={{
                  position: 'absolute',
                  top: '10px',
                  right: '10px',
                  backgroundColor: '#FFD700',
                  padding: '5px 10px',
                  borderRadius: '5px',
                  fontWeight: 'bold',
                  color: '#333',
                  zIndex: 1,
                  boxShadow: '0 2px 4px rgba(0,0,0,0.2)',
                }}
            >
              PRE-ORDER
            </div>
        )}
      </div>
  );
}
