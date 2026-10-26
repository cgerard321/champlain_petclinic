import { useTranslation } from 'react-i18next';
import {
  localizedProduct,
  formatProductPrice,
} from '@/features/products/utils/localizedProduct';
import {
  emptyProductModel,
  ProductModel,
} from '@/features/products/models/ProductModels/ProductModel';
import { NavBar } from '@/layouts/AppNavBar';
import { JSX, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getProduct } from '../api/getProduct';
import { addProductToRecentlyViewed } from '../utils/recentlyViewed';
import './ProductDetails.css';
import StarRating from './StarRating';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { AxiosError } from 'axios';
import ImageContainer from './ImageContainer';
import { Button } from 'react-bootstrap';
import {
  IsAdmin,
  IsInventoryManager,
  IsReceptionist,
  IsVet,
  useUser,
} from '@/context/UserContext';
import RecentlyViewedProducts from '@/features/products/components/RecentlyViewedProducts';
import { useAddToCart } from '@/features/carts/api/addToCartFromProducts';
import { useAddToWishlist } from '@/features/carts/api/addToWishlistFromProducts';
import { FaCheck, FaHeart, FaTimes } from 'react-icons/fa';
import { ReviewsList } from '@/features/products/components/ReviewsList.tsx';

export default function ProductDetails(): JSX.Element {
  const { t, i18n } = useTranslation('products');
  const language = i18n.resolvedLanguage || i18n.language || 'en';
  const isAdmin = IsAdmin();
  const { user, isAuthenticated } = useUser();
  const isInventoryManager = IsInventoryManager();
  const isVet = IsVet();
  const isReceptionist = IsReceptionist();
  const isStaff = isAdmin || isInventoryManager || isVet || isReceptionist;
  const [reviewCount, setReviewCount] = useState(0);

  const navigate = useNavigate();
  const { productId } = useParams();
  const { addToCart } = useAddToCart();
  //------------------------------------------------------
  const { addToWishlist } = useAddToWishlist();
  const [quantity, setQuantity] = useState(1);
  const handleMinus = (): void => {
    if (quantity > 1) setQuantity(quantity - 1);
  };
  const handlePlus = (): void => {
    setQuantity(quantity + 1);
  };

  const [successMessageWishlist, setSuccessMessageWishlist] = useState<
    string | null
  >(null);

  const handleAddToWishlist = async (): Promise<void> => {
    if (!isAuthenticated) {
      navigate(AppRoutePaths.Login);
      return;
    }
    const isSuccess = await addToWishlist(currentProduct.productId, 1);
    if (isSuccess) {
      setSuccessMessageWishlist('wishSuccess');

      // Clear the message after 3 seconds
      setTimeout(() => setSuccessMessageWishlist(null), 3000);
    }
  };

  const [currentProduct, setCurrentProduct] =
    useState<ProductModel>(emptyProductModel);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const getProductTypeLabel = (productType: string): string => {
    return productType
      ? t(`types.${productType.toUpperCase()}`, { defaultValue: productType })
      : t('unknown');
  };
  const getDeliveryTypeLabel = (deliveryType: string): string => {
    if (deliveryType === 'DELIVERY') return t('standardDelivery');
    if (deliveryType === 'PICKUP') return t('pickup');
    if (deliveryType === 'DELIVERY_AND_PICKUP') return t('deliveryPickup');
    if (deliveryType === 'NO_DELIVERY_OPTION') return t('noDelivery');
    return t('unknownDelivery');
  };

  const fetchProduct = async (): Promise<void> => {
    if (!productId) return;
    getProduct(productId)
      .then(res => {
        setCurrentProduct(res);
        if (!res.isUnlisted) {
          addProductToRecentlyViewed(res, user.userId);
        }

        setIsLoading(false);
      })
      .catch((err: AxiosError) => {
        switch (err.status) {
          case 404:
          case 422:
            navigate(AppRoutePaths.PageNotFound);
            break;
          default:
            navigate(AppRoutePaths.InternalServerError);
            console.error('Failed to fetch product', err);
            break;
        }
      });
  };
  useEffect(() => {
    fetchProduct();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [productId, isAuthenticated]);

  const isUnlisted = currentProduct.isUnlisted;

  const handleAddToCartClick = async (): Promise<void> => {
    if (!isAuthenticated) {
      navigate(AppRoutePaths.Login);
      return;
    }
    if (!productId) return;
    if (isStaff) return;
    const ok = await addToCart(String(productId), quantity);
    alert(ok ? t('addedItems', { count: quantity }) : t('cartFailed'));
  };

  return (
    <>
      <NavBar />
      {isLoading ? (
        <div>
          <p>{t('loadingDetails')}</p>
        </div>
      ) : (
        <div
          className={
            isUnlisted && !isAdmin && !isInventoryManager ? 'no-grid' : ''
          }
        >
          <div className="product-container">
            {isUnlisted && !isAdmin && !isInventoryManager ? (
              <div className="product-unavailable">
                <h2>{t('unavailable')}</h2>
                <h3>{t('unlisted')}</h3>
              </div>
            ) : (
              <>
                <div className="productimage-container">
                  <ImageContainer
                    image={currentProduct.image}
                    imageId={currentProduct.imageId}
                  />
                </div>
                <div className="productdetails-container">
                  <div
                    className="productadmin-container"
                    style={{
                      visibility: `${
                        isAdmin || isInventoryManager ? 'visible' : 'hidden'
                      }`,
                    }}
                  ></div>

                  <h2>
                    {localizedProduct(currentProduct, language).name}
                    {!isInventoryManager && !isVet && !isReceptionist && (
                      <button
                        onClick={handleAddToWishlist}
                        className="wishlist-btn"
                      >
                        <FaHeart className="wishlist-icon" /> {t('wishlist')}
                      </button>
                    )}
                  </h2>
                  {successMessageWishlist && (
                    <p className="success-message">
                      {t(successMessageWishlist)}
                    </p>
                  )}

                  <div className="avgrating-container">
                    <StarRating
                      currentRating={currentProduct.averageRating}
                      viewOnly={true}
                    />
                    <h3>{currentProduct.averageRating} </h3>
                    <div className="review-nums">
                      {t('reviewCount', { count: reviewCount || 0 })}
                    </div>
                  </div>
                  <div className="line"></div>

                  <div className="details-type">
                    {t('type')}
                    <span className="box-details">
                      {getProductTypeLabel(currentProduct.productType)}
                    </span>
                    {t('deliveryType')}
                    <span className="box-details">
                      {getDeliveryTypeLabel(currentProduct.deliveryType)}
                    </span>
                  </div>

                  <h3 className="prod-price">
                    {formatProductPrice(
                      currentProduct.productSalePrice,
                      language
                    )}
                  </h3>

                  <div className="stock-details">
                    <div className="inStock">
                      {currentProduct.productQuantity >= 10 && (
                        <p>
                          {' '}
                          <FaCheck />
                          {t('inStock')}
                        </p>
                      )}
                    </div>
                    <div className="outOfStock">
                      {currentProduct.productQuantity === 0 && (
                        <p>
                          {' '}
                          <FaTimes />
                          {t('outStock')}
                        </p>
                      )}
                    </div>
                    <div className="lowStock">
                      {currentProduct.productQuantity > 0 &&
                        currentProduct.productQuantity < 10 && (
                          <p>
                            {' '}
                            <FaCheck />
                            {t('fewLeft')}
                          </p>
                        )}
                    </div>
                  </div>
                  <h3 className="prod-desc-title">{t('about')}</h3>
                  <p className="prod-description">
                    {localizedProduct(currentProduct, language).description}
                  </p>
                  <div className=" cart-box">
                    <div className=" quantity-selector">
                      <button onClick={handleMinus} className="qty-btn">
                        {'-'}
                      </button>
                      <input className="qty-input" value={quantity} readOnly />
                      <button onClick={handlePlus} className="qty-btn">
                        {'+'}
                      </button>
                    </div>

                    {/* Single, final Add to Cart block */}
                    {!isInventoryManager && !isVet && !isReceptionist && (
                      <div className="cartactions-container">
                        <Button
                          onClick={handleAddToCartClick}
                          disabled={isStaff}
                          aria-disabled={isStaff}
                        >
                          {t('addCart')}
                        </Button>
                      </div>
                    )}
                  </div>
                </div>
                <div className="product-review-separator"></div>
                <ReviewsList
                  productId={productId}
                  isAuthenticated={isAuthenticated}
                  isStaff={isStaff}
                  onReviewModification={async () => {
                    if (!productId) return;
                    const resRefresh = await getProduct(productId);
                    setCurrentProduct(resRefresh);
                  }}
                  onModifyReviews={setReviewCount}
                  currentProduct={currentProduct}
                />
              </>
            )}
          </div>
        </div>
      )}
      <div>
        <RecentlyViewedProducts />
      </div>
    </>
  );
}
