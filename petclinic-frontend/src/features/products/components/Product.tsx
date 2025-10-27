import { JSX, useEffect, useState } from 'react';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import ImageContainer from './ImageContainer';
import { generatePath, useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import './Product.css';
import { useAddToCart } from '@/features/carts/api/addToCartFromProducts.ts';
import {
  IsInventoryManager,
  IsVet,
  IsReceptionist,
} from '@/context/UserContext';
import { useAddToWishlist } from '@/features/carts/api/addToWishlistFromProducts';
import StarRating from './StarRating';
import { FaHeart, FaRegHeart } from 'react-icons/fa';

export default function Product({
  product,
}: {
  product: ProductModel;
}): JSX.Element {
  const isInventoryManager = IsInventoryManager();
  const isVet = IsVet();
  const isReceptionist = IsReceptionist();

  const [currentProduct] = useState<ProductModel>(product);
  const [selectedProduct, setSelectedProduct] = useState<ProductModel | null>(
    null
  );
  const [successMessageCart, setSuccessMessageCart] = useState<string | null>(
    null
  );
  const [successMessageWishlist, setSuccessMessageWishlist] = useState<
    string | null
  >(null);
  const [, setTooLong] = useState<boolean>(false);
  const [isWishlisted, setIsWishlisted] = useState(false);

  const navigate = useNavigate();
  const { addToCart } = useAddToCart();
  const { addToWishlist } = useAddToWishlist();

  const handleProductClick = (): void => {
    navigate(
      generatePath(AppRoutePaths.ProductDetails, {
        productId: product.productId,
      })
    );
  };

  const getDeliveryTypeLabel = (deliveryType: string): string => {
    switch (deliveryType) {
      case 'DELIVERY':
        return 'Delivery';
      case 'PICKUP':
        return 'Pickup';
      case 'DELIVERY_AND_PICKUP':
        return 'Delivery & Pickup';
      case 'NO_DELIVERY_OPTION':
        return 'No delivery option';
      default:
        return 'Unknown Delivery Type';
    }
  };

  useEffect(() => {
    setTooLong(product.productDescription.length > 100);
  }, [product.productDescription]);

  const handleBackToList = (): void => setSelectedProduct(null);

  const handleAddToCart = async (): Promise<void> => {
    const isSuccess = await addToCart(
      currentProduct.productId,
      currentProduct.productQuantity
    );
    if (isSuccess) {
      setSuccessMessageCart('Product added to cart successfully!');
      setTimeout(() => setSuccessMessageCart(null), 3000);
    }
  };

  const handleAddToWishlist = async (): Promise<void> => {
    const isSuccess = await addToWishlist(currentProduct.productId, 1);
    if (isSuccess) {
      setSuccessMessageWishlist('Product added to wishlist successfully!');
      setIsWishlisted(true); // stays true after adding
      setTimeout(() => setSuccessMessageWishlist(null), 3000);
    }
  };

  if (selectedProduct) {
    return (
      <div>
        <h1>{selectedProduct.productName}</h1>
        <p>{selectedProduct.productDescription}</p>
        <p>Price: ${selectedProduct.productSalePrice.toFixed(2)}</p>
        <div className="deliveryType-container">
          <p>{getDeliveryTypeLabel(currentProduct.deliveryType)}</p>
        </div>
        <button onClick={handleBackToList}>Back to Catalog</button>
      </div>
    );
  }

  return (
    <div
      className={`card product-card product-card-no-bg ${
        currentProduct.productQuantity === 0
          ? 'out-of-stock'
          : currentProduct.productQuantity < 10
            ? 'low-quantity'
            : ''
      }`}
      key={currentProduct.productId}
      style={{ position: 'relative' }}
    >
      {/* Wishlist Heart Button */}
      {!isInventoryManager && !isVet && !isReceptionist && (
        <button
          className="wishlist-heart-btn"
          title="Add to Wishlist"
          onClick={handleAddToWishlist}
        >
          {isWishlisted ? (
            <FaHeart style={{ color: '#e11d48' }} />
          ) : (
            <FaRegHeart style={{ color: '#000' }} />
          )}
        </button>
      )}

      <div onClick={handleProductClick} className="product-title">
        <ImageContainer imageId={currentProduct.imageId} />
        <h2 className="product-title">{currentProduct.productName}</h2>
      </div>

      <div className="deliveryType-container">
        <p>{getDeliveryTypeLabel(currentProduct.deliveryType)}</p>
      </div>

      <p>Price: ${currentProduct.productSalePrice.toFixed(2)}</p>

      <div className="avgrating-container">
        <StarRating
          currentRating={currentProduct.averageRating}
          viewOnly={true}
        />
      </div>

      {/* Only show Add to Cart for customers */}
      {!isInventoryManager && !isVet && !isReceptionist && (
        <>
          <button
            className={`add-to-cart-btn${currentProduct.productQuantity === 0 ? ' disabled' : ''}`}
            onClick={handleAddToCart}
            disabled={currentProduct.productQuantity === 0}
          >
            {currentProduct.productQuantity === 0
              ? 'Out of Stock'
              : 'Add to Cart'}
          </button>
          {successMessageCart && (
            <p className="success-message">{successMessageCart}</p>
          )}
          {successMessageWishlist && (
            <p className="success-message">{successMessageWishlist}</p>
          )}
        </>
      )}

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
