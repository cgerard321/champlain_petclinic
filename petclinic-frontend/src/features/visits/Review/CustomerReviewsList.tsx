import * as React from 'react';
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { IsCustomer, useUser } from '@/context/UserContext';
import { getAllReviews } from './Api/getAllReviews';
import { ReviewResponseDTO } from './Model/ReviewResponseDTO';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { deleteReview } from './Api/deleteReview';
import AddingReview from './reviewComponents/AddingReview';
import EditingReview from './reviewComponents/EditingReview';
import '../VisitListTable.css';
import StarRating from '@/features/products/components/StarRating';
import BasicModal from '@/shared/components/BasicModal';
import SvgIcon from '@/shared/components/SvgIcon';
import { useTranslation } from 'react-i18next';
import { getReviewEligibility } from './Api/getReviewEligibility';

const CustomerReviewsList: React.FC = (): JSX.Element => {
  const { t } = useTranslation('visits');
  const [reviewList, setReviewList] = useState<ReviewResponseDTO[]>([]);

  const [selectedRating, setSelectedRating] = useState<number | null>(null);

  const filteredReviews =
    selectedRating === null
      ? reviewList
      : reviewList.filter(review => review.rating === selectedRating);

  const navigate = useNavigate();
  const { user } = useUser();
  const isOwner = IsCustomer();
  const [eligibilityStatus, setEligibilityStatus] = useState<
    'loading' | 'eligible' | 'ineligible' | 'error'
  >('loading');
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [showSuccessMessage, setShowSuccessMessage] = useState<boolean>(false);

  useEffect(() => {
    if (!isOwner || !user?.userId) return;
    let active = true;
    setEligibilityStatus('loading');
    getReviewEligibility()
      .then(({ eligible }) => {
        if (active) {
          setEligibilityStatus(eligible ? 'eligible' : 'ineligible');
        }
      })
      .catch(error => {
        console.error('Failed to check review eligibility:', error);
        if (active) {
          setEligibilityStatus('error');
        }
      });
    return () => {
      active = false;
    };
  }, [isOwner, user?.userId]);

  useEffect(() => {
    const fetchReviewsData = async (): Promise<void> => {
      try {
        const response = await getAllReviews();
        if (Array.isArray(response)) {
          setReviewList(response);
        } else {
          console.error('Fetched data is not an array:', response);
        }
      } catch (error) {
        console.error('Error fetching reviews:', error);
      }
    };

    if (user?.userId) {
      fetchReviewsData().catch(error =>
        console.error('Error in fetchReviewsData:', error)
      );
    }
    fetchReviewsData().catch(error =>
      console.error('Error in fetchReviewsData:', error)
    );
    // No reviewerName fallback — ownership is determined by ownerId from the backend
  }, [user.userId]);

  const handleDelete = async (reviewId: number): Promise<void> => {
    try {
      await deleteReview(reviewId.toString());
      setReviewList(prev =>
        prev.filter(review => review.reviewId !== reviewId)
      );

      // show a short success message (page-level)
      setSuccessMessage('Review successfully deleted.');
      setShowSuccessMessage(true);
      setTimeout(() => {
        setShowSuccessMessage(false);
        setSuccessMessage(null);
      }, 2500);
    } catch (error) {
      console.error('Error deleting review:', error);
    }
  };

  return (
    <div className="page-container">
      <div className="visit-table-section">
        <h1>Reviews</h1>
        <button
          className="btn btn-warning"
          onClick={() => navigate(AppRoutePaths.CustomerVisits)}
          title="Return to visits"
        >
          Return to visits
        </button>
        {isOwner && (
          <div>
            <AddingReview canReview={eligibilityStatus === 'eligible'} />
            {eligibilityStatus !== 'eligible' && (
              <p
                className="text-muted mt-2 mb-3"
                role="status"
                aria-live="polite"
              >
                {eligibilityStatus === 'ineligible'
                  ? t('reviews.completedAppointmentRequired')
                  : eligibilityStatus === 'error'
                    ? t('reviews.eligibilityUnavailable')
                    : t('reviews.checkingEligibility')}
              </p>
            )}
          </div>
        )}{' '}
        {showSuccessMessage && successMessage && (
          <div
            className="visit-success-message"
            role="status"
            aria-live="polite"
            style={{ marginBottom: '0.75rem' }}
          >
            {successMessage}
          </div>
        )}
        <div className="d-flex align-items-center gap-2 my-3">
          <label htmlFor="customer-review-rating-filter">
            {t('reviews.filterByRating')}
          </label>
          <select
            id="customer-review-rating-filter"
            className="form-select w-auto"
            value={selectedRating ?? 'all'}
            onChange={event =>
              setSelectedRating(
                event.target.value === 'all' ? null : Number(event.target.value)
              )
            }
          >
            <option value="all">{t('reviews.allRatings')}</option>
            {[1, 2, 3, 4, 5].map(rating => (
              <option key={rating} value={rating}>
                {t('reviews.stars', { count: rating })}
              </option>
            ))}
          </select>
        </div>
        <table className="reviews-table">
          <thead>
            <tr>
              <th>Reviewer Name</th>
              <th>Review</th>
              <th>Rating</th>
              <th>Date Submitted</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {filteredReviews.length > 0 ? (
              filteredReviews.map(review => {
                const isOwner = user && review.ownerId === user.userId;

                return (
                  <tr key={review.reviewId}>
                    <td>{review.reviewerName}</td>
                    <td>{review.review}</td>
                    <td>
                      <StarRating
                        currentRating={review.rating}
                        viewOnly={true}
                      />
                    </td>
                    <td>
                      {new Date(review.dateSubmitted).toLocaleDateString()}
                    </td>
                    <td>
                      {isOwner ? (
                        <>
                          <EditingReview
                            reviewId={review.reviewId.toString()}
                            reviewData={review}
                          />
                          <BasicModal
                            title="Delete review"
                            confirmText="Delete"
                            onConfirm={async () =>
                              await handleDelete(review.reviewId)
                            }
                            showButton={
                              <a title="Delete">
                                <SvgIcon id="trash" className="icon-visits" />
                              </a>
                            }
                          >
                            <p>Are you sure you want to delete your review?</p>
                          </BasicModal>
                        </>
                      ) : null}
                    </td>
                  </tr>
                );
              })
            ) : (
              <tr>
                <td colSpan={5} className="text-center">
                  {selectedRating === null
                    ? t('reviews.noReviews')
                    : t('reviews.noMatchingReviews')}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default CustomerReviewsList;
