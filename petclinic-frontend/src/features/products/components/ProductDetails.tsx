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
  const isAdmin = IsAdmin();
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
    if (productType === 'ACCESSORY') return 'Accessory';
    if (productType === 'FOOD') return 'Food';
    if (productType === 'MEDICATION') return 'Medication';
    if (productType === 'EQUIPMENT') return 'Equipment';
    return 'Unknown Product Type';
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
    fetchProduct();
    fetchRatings();
    fetchRating();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [productId]);

  const isUnlisted = currentProduct.isUnlisted;

  const handleAddToCartClick = async (): Promise<void> => {
    if (!productId) return;
    if (isStaff) return;
    const ok = await addToCart(String(productId), quantity);
    alert(
      ok
        ? `Added ${quantity} item${quantity > 1 ? 's' : ''} to cart`
        : "Couldn't add to cart. Please try again."
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
                  <ImageContainer imageId={currentProduct.imageId} />
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
                          return (
                            <div key={star} className="rating-bar-row">
                              <span className="star-label">{star} stars</span>
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
                        Reviews ({productReviews.length})
                      </h3>
                      <Button
                        variant="primary"
                        onClick={() => setShowReviewModal(true)}
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
                    {productReviews.length > 0 ? (
                      productReviews.map(
                        (rating: RatingModel, index: number) => (
                          <div key={index} className="reviewbox">
                            {currentUserRating.rating > 0 &&
                              currentUserRating.review === rating.review &&
                              !isStaff && (
                                <div className="review-card-actions">
                                  <button
                                    className="review-card-edit-btn"
                                    onClick={handleEditReview}
                                    title="Edit your review"
                                  >
                                    <FaPen />
                                  </button>
                                  <button
                                    className="review-card-delete-btn"
                                    onClick={handleDeleteReview}
                                    title="Delete your review"
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
                      <p>This item does not have any reviews yet!</p>
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
