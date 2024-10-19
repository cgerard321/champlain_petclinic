import { JSX, useEffect, useState } from 'react';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import { getProductByProductId } from '@/features/products/api/getProductByProductId.tsx';
import ImageContainer from './ImageContainer';
import { changeProductQuantity } from '../api/changeProductQuantity';
import { generatePath, useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import StarRating from './StarRating';
import './Product.css';
import { addToWishlist } from './addToWishlist';

interface ProductProps {
  product: ProductModel;
  cartId: string | null; // Add cartId prop
}

export default function Product({
  product,
  cartId, // Destructure cartId prop
}: ProductProps): JSX.Element {
  const [currentProduct, setCurrentProduct] = useState<ProductModel>(product);
  const [selectedProduct, setSelectedProduct] = useState<ProductModel | null>(
    null
  );
  const [selectedProductForQuantity, setSelectedProductForQuantity] =
    useState<ProductModel | null>(null);
  const [quantity, setQuantity] = useState<number>(0);
  const [wishlistQuantity, setWishlistQuantity] = useState<number>(1); // State for wishlist quantity
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

  const handleAddToWishlist = async (): Promise<void> => {
    try {
      await addToWishlist(currentProduct.productId, cartId, wishlistQuantity); // Pass wishlistQuantity
      alert(
        `${wishlistQuantity} of ${currentProduct.productName} has been added to your wishlist!`
      );
    } catch (error) {
      console.error('Failed to add product to wishlist:', error);
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
      <ImageContainer imageId={product.imageId} />
      <span
        onClick={() => handleProductClickForProductQuantity(product.productId)}
        style={{ cursor: 'pointer', color: 'blue', fontWeight: 'bold' }}
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
      <StarRating
        currentRating={currentProduct.averageRating}
        viewOnly={true}
      />

      {/* Wishlist Quantity Input */}
      <input
        type="number"
        value={wishlistQuantity}
        onChange={e =>
          setWishlistQuantity(Math.max(1, parseInt(e.target.value)))
        }
        min="1"
        max={currentProduct.productQuantity}
        style={{ width: '50px', marginLeft: '10px' }}
      />

      {/* Add to Wishlist Button */}
      <button onClick={handleAddToWishlist}>Add to Wishlist</button>
    </div>
  );
}
