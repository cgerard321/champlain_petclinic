import { useTranslation } from 'react-i18next';
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
import { AverageReview } from './AverageReview';
import Stack from 'react-bootstrap/Stack';
import { likeReview } from '@/features/products/api/likeReview.ts';

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
  const { t } = useTranslation('products');
  const [productRatings, setProductRatings] = useState<RatingModel[]>([]);
  const [ratingsStatus, setRatingsStatus] = useState<
    'loading' | 'ready' | 'error'
  >('loading');
  const productReviews = productRatings.filter(rating => rating.review !== '');
  const [currentUserReview, setCurrentUserReview] = useState<RatingModel>({
    rating: 0,
    review: '',
  });
  const [showReviewModal, setShowReviewModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const navigate = useNavigate();

  const [selectedRating, setSelectedRating] = useState<number | null>(null);
  const ratingFilteredReviews =
    selectedRating === null
      ? productReviews
      : productReviews.filter(r => Math.floor(r.rating) === selectedRating);

  const [searchQuery, setSearchQuery] = useState('');
  const filteredReviews = ratingFilteredReviews.filter(r =>
    r.review.toLowerCase().includes(searchQuery.toLowerCase())
  );

  useEffect(() => {
    onModifyReviews(productReviews.length);
  }, [onModifyReviews, productReviews.length]);

  const fetchReviews = async (): Promise<void> => {
    if (!productId) return;
    setRatingsStatus('loading');
    try {
      let reviews: RatingModel[];
      try {
        reviews = await getUserRatingsForProduct(productId, isAuthenticated);
      } catch (err) {
        if (!isAuthenticated) throw err;
        console.warn(
          'Personalized ratings unavailable, using public list',
          err
        );
        reviews = await getUserRatingsForProduct(productId);
      }
      setProductRatings(reviews);
      setRatingsStatus('ready');
    } catch (err) {
      setRatingsStatus('error');
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
        throw err;
      }
    }
    await fetchReviews();
  };

  useEffect(() => {
    setSelectedRating(null);
    fetchReviews();
    if (isAuthenticated) {
      fetchCurrentUserReview();
    } else {
      setCurrentUserReview({ rating: 0, review: '' });
    }
    setSearchQuery('');
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [productId, isAuthenticated]);

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

  const handleLikeReview = async (review: RatingModel): Promise<void> => {
    if (!productId || !review.customerId) return;
    if (!isAuthenticated) {
      navigate(AppRoutePaths.Login);
      return;
    }

    const result = await likeReview(productId, review.customerId);
    setProductRatings(currentReviews =>
      currentReviews.map(currentReview =>
        currentReview.customerId === review.customerId
          ? {
              ...currentReview,
              likeCount: result.likeCount,
              likedByCurrentUser: result.likedByCurrentUser,
            }
          : currentReview
      )
    );
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
        <h2>{t('customerReview')}</h2>
        <AverageReview ratings={productRatings} status={ratingsStatus} />

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
                <button
                  key={star}
                  type="button"
                  className={`rating-bar-row ${
                    selectedRating === star ? 'rating-bar-row-selected' : ''
                  }`}
                  onClick={() => handleRatingFilter(star)}
                  aria-pressed={selectedRating === star}
                  aria-label={`${t('stars', { count: star })}, ${t('reviewCount', { count })}`}
                >
                  <span className="star-label">
                    {t('stars', { count: star })}
                  </span>
                  <span className="rating-bar">
                    <span
                      className="rating-bar-fill"
                      style={{ width: `${percentage}%` }}
                    ></span>
                  </span>

                  <span className="rating-category-count">{count}</span>
                </button>
              );
            })}
          </div>
        </div>

        <div className="rating-section-separator"></div>

        <div className="review-action-row">
          <div className="reviews-heading-container">
            <h3 className="reviews-heading">
              {selectedRating !== null
                ? t('filteredReviewsHeading', {
                    count: ratingFilteredReviews.length,
                    total: productReviews.length,
                  })
                : t('reviewsHeading', { count: ratingFilteredReviews.length })}
            </h3>
            {selectedRating !== null && (
              <button
                type="button"
                className="clear-rating-filter"
                onClick={() => setSelectedRating(null)}
              >
                {t('clearRatingFilter', { count: selectedRating })}
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
            disabled={isStaff || currentUserReview.rating > 0}
          >
            {t('writeReview')}
          </Button>
        </div>

        {productReviews.length > 0 && (
          <div>
            <Stack direction="horizontal">
              <input
                type="text"
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value.trimStart())}
                placeholder={t('searchReviews')}
                className="search-input"
              />
              <Button
                onClick={() => setSearchQuery('')}
                aria-label={t('clearReviewSearch')}
              >
                <MdClear />
              </Button>
            </Stack>
            {filteredReviews.length > 0 && searchQuery !== '' && (
              <span className="reviews-search-result-count">
                {t('reviewSearchCount', {
                  count: filteredReviews.length,
                  total: ratingFilteredReviews.length,
                })}
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
                canLike={
                  !isAuthenticated ||
                  !currentUserReview.customerId ||
                  currentUserReview.customerId !== rating.customerId
                }
                onLike={() => handleLikeReview(rating)}
                searchQuery={searchQuery}
              />
            </Reveal>
          ))
        ) : productReviews.length === 0 ? (
          <p role="status">{t('noReviews')}</p>
        ) : ratingFilteredReviews.length === 0 ? (
          <p role="status">
            {t('noReviewsWithRating', { count: selectedRating })}
          </p>
        ) : (
          <p>{t('noReviewResults')}</p>
        )}
      </div>
    </div>
  );
}
