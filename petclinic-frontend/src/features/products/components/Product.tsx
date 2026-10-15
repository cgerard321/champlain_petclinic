import { useTranslation } from 'react-i18next';
import {
  localizedProduct,
  formatProductPrice,
} from '@/features/products/utils/localizedProduct';
import { JSX, useEffect, useState } from 'react';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import ImageContainer from './ImageContainer';
import { generatePath, useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import './Product.css';
import { useAddToCart } from '@/features/carts/api/addToCartFromProducts.ts';
import {
  useUser,
  IsInventoryManager,
  IsVet,
  IsReceptionist,
} from '@/context/UserContext';
import { useAddToWishlist } from '@/features/carts/api/addToWishlistFromProducts';
import { useRemoveFromWishlistByIcon } from '@/features/carts/api/removeProductFromWishlist';
import StarRating from './StarRating';
import { FaHeart, FaRegHeart } from 'react-icons/fa';

export default function Product({
  product,
}: {
  product: ProductModel;
}): JSX.Element {
  const { t, i18n } = useTranslation('products');
  const language = i18n.resolvedLanguage || i18n.language || 'en';
  const { isAuthenticated } = useUser();
  const isInventoryManager = IsInventoryManager();
  const isVet = IsVet();
  const isReceptionist = IsReceptionist();

  const currentProduct = product;
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
  const { removeFromWishlistByIcon } = useRemoveFromWishlistByIcon();

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
        return t('delivery');
      case 'PICKUP':
        return t('pickup');
      case 'DELIVERY_AND_PICKUP':
        return t('deliveryPickup');
      case 'NO_DELIVERY_OPTION':
        return t('noDelivery');
      default:
        return t('unknownDelivery');
    }
  };

  useEffect(() => {
    setTooLong(product.productDescription.length > 100);
  }, [product.productDescription]);

  const handleBackToList = (): void => setSelectedProduct(null);

  const handleAddToCart = async (): Promise<void> => {
    if (!isAuthenticated) {
      navigate(AppRoutePaths.Login);
      return;
    }
    const isSuccess = await addToCart(currentProduct.productId, 1);
    if (isSuccess) {
      setSuccessMessageCart('cartSuccess');
      setTimeout(() => setSuccessMessageCart(null), 3000);
    }
  };

  const handleAddToWishlist = async (): Promise<void> => {
    if (!isAuthenticated) {
      navigate(AppRoutePaths.Login);
      return;
    }
    const isSuccess = await addToWishlist(currentProduct.productId, 1);
    if (isSuccess) {
      setSuccessMessageWishlist('wishSuccess');
      setIsWishlisted(true); // stays true after adding
      setTimeout(() => setSuccessMessageWishlist(null), 3000);
    }
  };

  const handleRemoveFromWishlist = async (): Promise<void> => {
    if (!isAuthenticated) {
      navigate(AppRoutePaths.Login);
      return;
    }
    const isSuccess = await removeFromWishlistByIcon(currentProduct.productId);
    if (isSuccess) {
      setSuccessMessageWishlist('wishRemoved');
      setIsWishlisted(false); // stays false after removing
      setTimeout(() => setSuccessMessageWishlist(null), 3000);
    }
  };

  const handleOnClickHeartIcon = (): void => {
    if (!isWishlisted) {
      handleAddToWishlist();
    } else if (isWishlisted) {
      handleRemoveFromWishlist();
    }
  };

  if (selectedProduct) {
    return (
      <div>
        <h1>{localizedProduct(selectedProduct, language).name}</h1>
        <p>{localizedProduct(selectedProduct, language).description}</p>
        <p>
          {t('price')}{' '}
          {formatProductPrice(selectedProduct.productSalePrice, language)}
        </p>
        <div className="deliveryType-container">
          <p>{getDeliveryTypeLabel(currentProduct.deliveryType)}</p>
        </div>
        <button onClick={handleBackToList}>{t('back')}</button>
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
      {currentProduct.productQuantity < 10 && (
        <span className="stock-label">
          {currentProduct.productQuantity === 0 ? t('outStock') : t('lowStock')}
        </span>
      )}
      {/* Wishlist Heart Button */}
      {!isInventoryManager && !isVet && !isReceptionist && (
        <button
          className="wishlist-heart-btn"
          title={isWishlisted ? t('removeWishlist') : t('addWishlist')}
          onClick={handleOnClickHeartIcon}
        >
          {isWishlisted ? (
            <FaHeart style={{ color: '#e11d48' }} />
          ) : (
            <FaRegHeart style={{ color: '#000' }} />
          )}
        </button>
      )}

      <div onClick={handleProductClick} className="product-title">
        <ImageContainer
          image={currentProduct.image}
          imageId={currentProduct.imageId}
        />
        <h2 className="product-title">
          {localizedProduct(currentProduct, language).name}
        </h2>
      </div>

      <div className="deliveryType-container">
        <p>{getDeliveryTypeLabel(currentProduct.deliveryType)}</p>
      </div>

      <p>
        {t('price')}{' '}
        {formatProductPrice(currentProduct.productSalePrice, language)}
      </p>

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
              ? t('outStock')
              : t('addCart')}
          </button>
          {successMessageCart && (
            <p className="success-message">{t(successMessageCart)}</p>
          )}
          {successMessageWishlist && (
            <p className="success-message">{t(successMessageWishlist)}</p>
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
          {t('preorder')}
        </div>
      )}
    </div>
  );
}
