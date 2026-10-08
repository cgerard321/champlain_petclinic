import { useTranslation } from 'react-i18next';
import {
  localizedProduct,
  formatProductPrice,
} from '@/features/products/utils/localizedProduct';
import {
  ProductModel,
  emptyProductModel,
} from '@/features/products/models/ProductModels/ProductModel';
import { NavBar } from '@/layouts/AppNavBar';
import { useState, useEffect, JSX } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { updateUserRating } from '../api/updateUserRating';
import { getProduct } from '../api/getProduct';
import { deleteUserRating } from '../api/deleteUserRating';
import './ProductDetails.css';
import defaultProfile from '@/assets/Customers/defaultProfilePicture.png';
import StarRating from './StarRating';
import { RatingModel } from '../models/ProductModels/RatingModel';
import { getUserRatingsForProduct } from '../api/getUserRatingsForProduct';
import { getUserRating } from '../api/getUserRating';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { AxiosError } from 'axios';
import ImageContainer from './ImageContainer';
import { Button } from 'react-bootstrap';
import {
  IsAdmin,
  useUser,
  IsInventoryManager,
  IsVet,
  IsReceptionist,
} from '@/context/UserContext';
import RecentlyViewedProducts from '@/features/products/components/RecentlyViewedProducts';
import { useAddToCart } from '@/features/carts/api/addToCartFromProducts';
import { useAddToWishlist } from '@/features/carts/api/addToWishlistFromProducts';
import { FaHeart, FaCheck, FaTimes, FaPen, FaTrash } from 'react-icons/fa';
import WriteReviewModal from './WriteReviewModal';
import EditReviewModal from './EditReviewModal';
import DeleteReviewModal from './DeleteReviewModal';

export default function ProductDetails(): JSX.Element {
  const { t, i18n } = useTranslation('products');
  const language = i18n.resolvedLanguage || i18n.language || 'en';
  const isAdmin = IsAdmin();
  const { isAuthenticated } = useUser();
  const isInventoryManager = IsInventoryManager();
  const isVet = IsVet();
  const isReceptionist = IsReceptionist();
  const isStaff = isAdmin || isInventoryManager || isVet || isReceptionist;

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
  const [currentUserRating, setUserRating] = useState<RatingModel>({
    rating: 0,
    review: '',
  });
  const [productReviews, setProductReviews] = useState<RatingModel[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  // const navigateToEditProduct = (): void => {
  //   if (!currentProduct || !productId) return;
  //   navigate(generatePath(AppRoutePaths.EditProduct, { productId }), {
  //     state: { product: currentProduct },
  //   });
  // };

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

  const fetchRatings = async (): Promise<void> => {
    if (!productId) return;
    try {
      const reviews = await getUserRatingsForProduct(productId);
      setProductReviews(reviews.filter((r: RatingModel) => r.review !== ''));
    } catch (err) {
      console.error('Failed to fetch product ratings', err);
    }
  };

  const fetchRating = async (): Promise<void> => {
    if (!productId) return;
    try {
      const rating = await getUserRating(productId);
      setUserRating(rating);
    } catch (err) {
      console.error('Failed to fetch current rating', err);
    }
  };

  const deleteRating = async (): Promise<void> => {
    if (!productId) return;
    try {
      await deleteUserRating(productId);
      setUserRating({ rating: 0, review: '' });
      const resRefresh = await getProduct(productId);
      setCurrentProduct(resRefresh);
    } catch (err) {
      console.error('Could not delete data', err);
    }
  };

  // const handleDeleteProduct = async (): Promise<void> => {
  //   if (!productId) return;
  //   try {
  //     await deleteProduct(productId);
  //     navigate(AppRoutePaths.Products);
  //   } catch (error) {
  //     console.error('Failed to delete product:', error);
  //   }
  // };

  const updateRating = async (
    newRating: number,
    newReview: string | null
  ): Promise<void> => {
    if (!productId) return;
    if (newRating === 0) {
      await deleteRating();
    } else {
      try {
        const resUpdate = await updateUserRating(
          productId,
          newRating,
          newReview
        );
        setUserRating(resUpdate);
        const resRefresh = await getProduct(productId);
        setCurrentProduct(resRefresh);
      } catch (err) {
        console.error('Could not update/fetch product ratings', err);
      }
    }
    fetchRatings();
  };

  useEffect(() => {
    fetchProduct();
    fetchRatings();
    if (isAuthenticated) {
      fetchRating();
    } else {
      setUserRating({ rating: 0, review: '' });
    }
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

  const handleEditReview = (): void => {
    setShowEditModal(true);
  };

  const handleDeleteReview = (): void => {
    setShowDeleteModal(true);
  };

  const confirmDeleteReview = async (): Promise<void> => {
    if (!productId) return;
    try {
      await deleteUserRating(productId);
      setUserRating({ rating: 0, review: '' });
      const resRefresh = await getProduct(productId);
      setCurrentProduct(resRefresh);
      await fetchRatings();
      setShowDeleteModal(false);
    } catch (err) {
      console.error('Could not delete review', err);
    }
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
                  >
                    {/* <Button variant="warning" onClick={navigateToEditProduct}>
                      Edit
                    </Button>
                    {productId && (
                      <PatchListingStatusButton productId={productId} />
                    )}
                    <Button variant="danger" onClick={handleDeleteProduct}>{t('delete')}</Button> */}
                  </div>

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
                      {t('reviewCount', { count: productReviews.length })}
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
                <div className="review-section-container">
                  <div className="reviewproduct-container">
                    <h2>{t('customerReview')}</h2>

                    <div className="rating-summary-container">
                      <div className="rating-left">
                        <div className="rating-display">
                          <div className="rating-display-row">
                            <span className="rating-number">
                              {currentProduct.averageRating.toFixed(1)}
                            </span>
                            <div className="rating-stars-beside">
                              <StarRating
                                currentRating={currentProduct.averageRating}
                                viewOnly={true}
                              />
                            </div>
                          </div>
                          <p className="review-count">
                            {t('basedOn', { count: productReviews.length })}
                          </p>
                        </div>
                      </div>

                      <div className="rating-breakdown">
                        {[5, 4, 3, 2, 1].map(star => {
                          const count = productReviews.filter(
                            r => Math.floor(r.rating) === star
                          ).length;
                          const percentage =
                            productReviews.length > 0
                              ? (count / productReviews.length) * 100
                              : 0;
                          return (
                            <div key={star} className="rating-bar-row">
                              <span className="star-label">
                                {t('stars', { count: star })}
                              </span>
                              <div className="rating-bar">
                                <div
                                  className="rating-bar-fill"
                                  style={{ width: `${percentage}%` }}
                                ></div>
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>

                    <div className="rating-section-separator"></div>

                    <div className="review-action-row">
                      <h3 className="reviews-heading">
                        {t('reviewsHeading', { count: productReviews.length })}
                      </h3>
                      <Button
                        variant="primary"
                        onClick={() => {
                          if (!isAuthenticated) {
                            navigate(AppRoutePaths.Login);
                            return;
                          }
                          setShowReviewModal(true);
                        }}
                        disabled={isStaff || currentUserRating.rating > 0}
                      >
                        {t('writeReview')}
                      </Button>
                    </div>

                    <div className="rating-section-separator"></div>

                    <WriteReviewModal
                      show={showReviewModal}
                      onClose={() => setShowReviewModal(false)}
                      currentUserRating={currentUserRating}
                      updateRating={updateRating}
                    />

                    <EditReviewModal
                      show={showEditModal}
                      onClose={() => setShowEditModal(false)}
                      currentUserRating={currentUserRating}
                      updateRating={updateRating}
                    />

                    <DeleteReviewModal
                      show={showDeleteModal}
                      onClose={() => setShowDeleteModal(false)}
                      onConfirm={confirmDeleteReview}
                    />
                  </div>

                  <div className="reviewsforproduct-container">
                    {productReviews.length > 0 ? (
                      productReviews.map(
                        (rating: RatingModel, index: number) => (
                          <div
                            key={rating.customerId || index}
                            className="reviewbox"
                          >
                            <div className="product-review-author">
                              <img
                                src={rating.reviewerPhoto || defaultProfile}
                                alt={t('photoAlt', {
                                  name:
                                    rating.reviewerUsername || t('customer'),
                                })}
                                onError={event => {
                                  event.currentTarget.onerror = null;
                                  event.currentTarget.src = defaultProfile;
                                }}
                              />
                              <span>
                                {rating.reviewerUsername || t('customer')}
                              </span>
                            </div>
                            {isAuthenticated &&
                              currentUserRating.rating > 0 &&
                              !!currentUserRating.customerId &&
                              currentUserRating.customerId ===
                                rating.customerId &&
                              !isStaff && (
                                <div className="review-card-actions">
                                  <button
                                    className="review-card-edit-btn"
                                    onClick={handleEditReview}
                                    title={t('editReviewTitle')}
                                  >
                                    <FaPen />
                                  </button>
                                  <button
                                    className="review-card-delete-btn"
                                    onClick={handleDeleteReview}
                                    title={t('deleteReviewTitle')}
                                  >
                                    <FaTrash />
                                  </button>
                                </div>
                              )}
                            <div className="starcontainer">
                              {Array.from({ length: 5 }, (_, k) => (
                                <svg
                                  xmlns="http://www.w3.org/2000/svg"
                                  width="24"
                                  height="24"
                                  viewBox="0 0 24 24"
                                  fill="none"
                                  stroke="currentColor"
                                  strokeWidth="2"
                                  strokeLinecap="round"
                                  strokeLinejoin="round"
                                  key={k}
                                  className={`star-static ${
                                    k < rating.rating ? 'star-shown' : ''
                                  }`}
                                >
                                  <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
                                </svg>
                              ))}
                            </div>
                            <p>{rating.review}</p>
                          </div>
                        )
                      )
                    ) : (
                      <p>{t('noReviews')}</p>
                    )}
                  </div>
                </div>
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
