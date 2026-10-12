import {
  emptyProductModel,
  ProductModel,
} from '@/features/products/models/ProductModels/ProductModel';
import { NavBar } from '@/layouts/AppNavBar';
import { JSX, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { updateUserRating } from '../api/updateUserRating';
import { getProduct } from '../api/getProduct';
import { deleteUserRating } from '../api/deleteUserRating';
import './ProductDetails.css';
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
  IsInventoryManager,
  IsReceptionist,
  IsVet,
  useUser,
} from '@/context/UserContext';
import RecentlyViewedProducts from '@/features/products/components/RecentlyViewedProducts';
import { useAddToCart } from '@/features/carts/api/addToCartFromProducts';
import { useAddToWishlist } from '@/features/carts/api/addToWishlistFromProducts';
import { FaCheck, FaHeart, FaTimes } from 'react-icons/fa';
import WriteReviewModal from './WriteReviewModal';
import EditReviewModal from './EditReviewModal';
import DeleteReviewModal from './DeleteReviewModal';
import { Review } from '@/features/products/components/Review.tsx';

export default function ProductDetails(): JSX.Element {
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
      setSuccessMessageWishlist('Product added to wishlist successfully!');

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
  const [selectedRating, setSelectedRating] = useState<number | null>(null);
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
      ? productType.charAt(0).toUpperCase() + productType.slice(1).toLowerCase()
      : 'Unknown';
  };
  const getDeliveryTypeLabel = (deliveryType: string): string => {
    if (deliveryType === 'DELIVERY') return 'Standard Delivery';
    if (deliveryType === 'PICKUP') return 'Pickup';
    if (deliveryType === 'DELIVERY_AND_PICKUP') return 'Delivery & Pickup';
    if (deliveryType === 'NO_DELIVERY_OPTION') return 'No delivery option';
    return 'Unknown Delivery Type';
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
    setSelectedRating(null);
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
    alert(
      ok
        ? `Added ${quantity} item${quantity > 1 ? 's' : ''} to cart`
        : "Couldn't add to cart. Please try again."
    );
  };
  const handleRatingFilter = (rating: number): void => {
    setSelectedRating(currentRating =>
      currentRating === rating ? null : rating
    );
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

  const filteredProductReviews =
    selectedRating === null
      ? productReviews
      : productReviews.filter(
          review => Math.floor(review.rating) === selectedRating
        );

  return (
    <>
      <NavBar />
      {isLoading ? (
        <div>
          <p>Is Loading..</p>
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
                <h2>Item Unavailable</h2>
                <h3>This item has been unlisted. Check back later! </h3>
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
                    <Button variant="danger" onClick={handleDeleteProduct}>
                      Delete
                    </Button> */}
                  </div>

                  <h2>
                    {currentProduct.productName}
                    {!isInventoryManager && !isVet && !isReceptionist && (
                      <button
                        onClick={handleAddToWishlist}
                        className="wishlist-btn"
                      >
                        <FaHeart className="wishlist-icon" /> Wishlist{' '}
                      </button>
                    )}
                  </h2>
                  {successMessageWishlist && (
                    <p className="success-message">{successMessageWishlist}</p>
                  )}

                  <div className="avgrating-container">
                    <StarRating
                      currentRating={currentProduct.averageRating}
                      viewOnly={true}
                    />
                    <h3>{currentProduct.averageRating} </h3>
                    <div className="review-nums">
                      {productReviews.length || 0} reviews{' '}
                    </div>
                  </div>
                  <div className="line"></div>

                  <div className="details-type">
                    Type:
                    <span className="box-details">
                      {getProductTypeLabel(currentProduct.productType)}
                    </span>
                    Delivery Type:
                    <span className="box-details">
                      {getDeliveryTypeLabel(currentProduct.deliveryType)}
                    </span>
                  </div>

                  <h3 className="prod-price">
                    {currentProduct.productSalePrice}$
                  </h3>

                  <div className="stock-details">
                    <div className="inStock">
                      {currentProduct.productQuantity >= 10 && (
                        <p>
                          {' '}
                          <FaCheck /> In Stock - Available for Pickup
                        </p>
                      )}
                    </div>
                    <div className="outOfStock">
                      {currentProduct.productQuantity === 0 && (
                        <p>
                          {' '}
                          <FaTimes /> Out of Stock
                        </p>
                      )}
                    </div>
                    <div className="lowStock">
                      {currentProduct.productQuantity > 0 &&
                        currentProduct.productQuantity < 10 && (
                          <p>
                            {' '}
                            <FaCheck /> Only a few left!
                          </p>
                        )}
                    </div>
                  </div>
                  <h3 className="prod-desc-title">About this Product</h3>
                  <p className="prod-description">
                    {currentProduct.productDescription}
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
                          Add to Cart
                        </Button>
                      </div>
                    )}
                  </div>
                </div>
                <div className="product-review-separator"></div>
                <div className="review-section-container">
                  <div className="reviewproduct-container">
                    <h2>Customer Review</h2>

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
                            Based on {productReviews.length} review
                            {productReviews.length !== 1 ? 's' : ''}
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

                          const ratingLabel = star === 1 ? 'star' : 'stars';
                          const reviewLabel =
                            count === 1 ? 'review' : 'reviews';

                          return (
                            <button
                              key={star}
                              type="button"
                              className={`rating-bar-row ${
                                selectedRating === star
                                  ? 'rating-bar-row-selected'
                                  : ''
                              }`}
                              onClick={() => handleRatingFilter(star)}
                              aria-pressed={selectedRating === star}
                              aria-label={`${star} ${ratingLabel}, ${count} ${reviewLabel}`}
                            >
                              <span className="star-label">
                                {star} {ratingLabel}
                              </span>
                              <span className="rating-bar">
                                <span
                                  className="rating-bar-fill"
                                  style={{ width: `${percentage}%` }}
                                ></span>
                              </span>

                              <span className="rating-category-count">
                                {count}
                              </span>
                            </button>
                          );
                        })}
                      </div>
                    </div>

                    <div className="rating-section-separator"></div>

                    <div className="review-action-row">
                      <div className="reviews-heading-container">
                        <h3 className="reviews-heading">
                          Reviews ({filteredProductReviews.length}
                          {selectedRating !== null
                            ? ` of ${productReviews.length}`
                            : ''}
                          )
                        </h3>
                        {selectedRating !== null && (
                          <button
                            type="button"
                            className="clear-rating-filter"
                            onClick={() => setSelectedRating(null)}
                          >
                            Clear {selectedRating}-star filter
                          </button>
                        )}
                      </div>
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
                        Write a Review
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
                    {filteredProductReviews.length > 0 ? (
                      filteredProductReviews.map(
                        (rating: RatingModel, index: number) => (
                          <Review
                            key={rating.customerId || index}
                            rating={rating}
                            index={index}
                            canEdit={
                              isAuthenticated &&
                              currentUserRating.rating > 0 &&
                              !!currentUserRating.customerId &&
                              currentUserRating.customerId ===
                                rating.customerId &&
                              !isStaff
                            }
                            onEdit={handleEditReview}
                            onDelete={handleDeleteReview}
                          />
                        )
                      )
                    ) : (
                      <p role="status">
                        {selectedRating !== null
                          ? `This item does not have any ${selectedRating}-star reviews.`
                          : 'This item does not have any reviews yet!'}
                      </p>
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
