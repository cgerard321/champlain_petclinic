import { JSX, useEffect, useState } from 'react';
import { RatingModel } from '@/features/products/models/ProductModels/RatingModel.ts';
import { getUserRatingsForProduct } from '@/features/products/api/getUserRatingsForProduct.ts';
import { getUserRating } from '@/features/products/api/getUserRating.ts';
import { deleteUserRating } from '@/features/products/api/deleteUserRating.ts';
import { updateUserRating } from '@/features/products/api/updateUserRating.ts';
import { Button } from 'react-bootstrap';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import WriteReviewModal from '@/features/products/components/WriteReviewModal.tsx';
import StarRating from '@/features/products/components/StarRating.tsx';
import EditReviewModal from '@/features/products/components/EditReviewModal.tsx';
import DeleteReviewModal from '@/features/products/components/DeleteReviewModal.tsx';
import { Review } from '@/features/products/components/Review.tsx';
import { useNavigate } from 'react-router-dom';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel.ts';
import { Reveal } from '@/shared/components';
import { MdClear } from 'react-icons/md';
import './ReviewsList.css';
import Stack from 'react-bootstrap/Stack';

interface ReviewListProps {
  productId: string | undefined;
  isAuthenticated: boolean;
  isStaff: boolean;
  onReviewModification: () => Promise<void>;
  onModifyReviews: (reviewCount: number) => void;
  currentProduct: ProductModel;
}

export function ReviewsList({
  productId,
  isAuthenticated,
  isStaff,
  onReviewModification,
  onModifyReviews,
  currentProduct,
}: ReviewListProps): JSX.Element {
  const [productReviews, setProductReviews] = useState<RatingModel[]>([]);
  const [currentUserReview, setCurrentUserReview] = useState<RatingModel>({
    rating: 0,
    review: '',
  });
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const navigate = useNavigate();

  const [searchQuery, setSearchQuery] = useState('');
  const filteredReviews = productReviews.filter(r =>
    r.review.toLowerCase().includes(searchQuery.toLowerCase())
  );

  useEffect(() => {
    onModifyReviews(productReviews.length);
  }, [onModifyReviews, productReviews.length]);

  const fetchReviews = async (): Promise<void> => {
    if (!productId) return;
    try {
      const reviews = await getUserRatingsForProduct(productId);
      setProductReviews(reviews.filter((r: RatingModel) => r.review !== ''));
    } catch (err) {
      console.error('Failed to fetch product ratings', err);
    }
  };

  const fetchCurrentUserReview = async (): Promise<void> => {
    if (!productId) return;
    try {
      const rating = await getUserRating(productId);
      setCurrentUserReview(rating);
    } catch (err) {
      console.error('Failed to fetch current rating', err);
    }
  };

  const deleteReview = async (): Promise<void> => {
    if (!productId) return;
    try {
      await deleteUserRating(productId);
      setCurrentUserReview({ rating: 0, review: '' });
      await onReviewModification();
    } catch (err) {
      console.error('Could not delete data', err);
    }
  };

  const updateReview = async (
    newRating: number,
    newReview: string | null
  ): Promise<void> => {
    if (!productId) return;
    if (newRating === 0) {
      await deleteReview();
    } else {
      try {
        const resUpdate = await updateUserRating(
          productId,
          newRating,
          newReview
        );
        setCurrentUserReview(resUpdate);
        await onReviewModification();
      } catch (err) {
        console.error('Could not update/fetch product ratings', err);
      }
    }
    fetchReviews();
  };

  useEffect(() => {
    fetchReviews();
    if (isAuthenticated) {
      fetchCurrentUserReview();
    } else {
      setCurrentUserReview({ rating: 0, review: '' });
    }
    setSearchQuery('');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [productId, isAuthenticated]);

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
      setCurrentUserReview({ rating: 0, review: '' });
      await onReviewModification();
      await fetchReviews();
      setShowDeleteModal(false);
    } catch (err) {
      console.error('Could not delete review', err);
    }
  };

  return (
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
          <h3 className="reviews-heading">Reviews ({productReviews.length})</h3>
          <Button
            variant="primary"
            onClick={() => {
              if (!isAuthenticated) {
                navigate(AppRoutePaths.Login);
                return;
              }
              setShowReviewModal(true);
            }}
            disabled={isStaff || currentUserReview.rating > 0}
          >
            Write a Review
          </Button>
        </div>

        {productReviews.length > 0 && (
          <div>
            <Stack direction="horizontal">
              <input
                type="text"
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value.trimStart())}
                placeholder="Search for a review..."
                className="search-input"
              />
              <Button
                onClick={() => setSearchQuery('')}
                aria-label="Clear search"
              >
                <MdClear />
              </Button>
            </Stack>
            {filteredReviews.length > 0 && searchQuery !== '' && (
              <span className="reviews-search-result-count">
                Showing {filteredReviews.length} of {productReviews.length}{' '}
                review(s)
              </span>
            )}
          </div>
        )}

        <div className="rating-section-separator"></div>

        <WriteReviewModal
          show={showReviewModal}
          onClose={() => setShowReviewModal(false)}
          currentUserRating={currentUserReview}
          updateRating={updateReview}
        />

        <EditReviewModal
          show={showEditModal}
          onClose={() => setShowEditModal(false)}
          currentUserRating={currentUserReview}
          updateRating={updateReview}
        />

        <DeleteReviewModal
          show={showDeleteModal}
          onClose={() => setShowDeleteModal(false)}
          onConfirm={confirmDeleteReview}
        />
      </div>

      <div className="reviewsforproduct-container">
        {filteredReviews.length > 0 ? (
          filteredReviews.map((rating: RatingModel, index: number) => (
            <Reveal key={rating.customerId || index}>
              <Review
                rating={rating}
                index={index}
                canEdit={
                  isAuthenticated &&
                  currentUserReview.rating > 0 &&
                  !!currentUserReview.customerId &&
                  currentUserReview.customerId === rating.customerId &&
                  !isStaff
                }
                onEdit={handleEditReview}
                onDelete={handleDeleteReview}
                searchQuery={searchQuery}
              />
            </Reveal>
          ))
        ) : productReviews.length === 0 ? (
          <p>This item does not have any reviews yet!</p>
        ) : (
          <p>No results were found</p>
        )}
      </div>
    </div>
  );
}
