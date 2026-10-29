import { useTranslation } from 'react-i18next';
import { JSX, useState } from 'react';
import { RatingModel } from '@/features/products/models/ProductModels/RatingModel.ts';
import defaultProfile from '@/assets/Customers/defaultProfilePicture.png';
import { MarkdownReviewBody } from '@/features/products/components/MarkdownReviewBody.tsx';
import { FaPen, FaThumbsUp, FaTrash } from 'react-icons/fa';
import { Button } from 'react-bootstrap';
import StarRating from '@/features/products/components/StarRating.tsx';

interface ReviewProps {
  rating: RatingModel;
  index: number;
  canEdit: boolean;
  onEdit: () => void;
  onDelete: () => void;
  canLike: boolean;
  onLike: () => Promise<void>;
  searchQuery?: string;
}

export function Review({
  rating,
  canEdit,
  onEdit,
  onDelete,
  canLike,
  onLike,
  searchQuery,
}: ReviewProps): JSX.Element {
  const { t } = useTranslation('products');
  const [showExpanded, setShowExpanded] = useState(false);
  const [hasOverflow, setHasOverflow] = useState(false);
  const [isLiking, setIsLiking] = useState(false);
  const [likeError, setLikeError] = useState('');

  const handleLike = async (): Promise<void> => {
    if (isLiking) return;
    setIsLiking(true);
    setLikeError('');
    try {
      await onLike();
    } catch (error) {
      console.error('Could not like review', error);
      setLikeError('Could not save your like. Please try again.');
    } finally {
      setIsLiking(false);
    }
  };

  const categoryRatings = [
    {
      label: t('qualityOfProduct'),
      rating: rating.qualityRating,
    },
    {
      label: t('valueOfProduct'),
      rating: rating.valueRating,
    },
    {
      label: t('petSatisfaction'),
      rating: rating.petSatisfactionRating,
    },
    {
      label: t('ingredients'),
      rating: rating.ingredientsRating,
    },
  ].filter(
    (category): category is { label: string; rating: number } =>
      typeof category.rating === 'number'
  );

  return (
    <div className="reviewbox">
      <div className="product-review-author">
        <img
          src={rating.reviewerPhoto || defaultProfile}
          alt={t('photoAlt', {
            name: rating.reviewerUsername || t('customer'),
          })}
          onError={event => {
            event.currentTarget.onerror = null;
            event.currentTarget.src = defaultProfile;
          }}
        />
        <span>{rating.reviewerUsername || t('customer')}</span>
      </div>
      {canEdit && (
        <div className="review-card-actions">
          <button
            className="review-card-edit-btn"
            onClick={onEdit}
            title={t('editReviewTitle')}
          >
            <FaPen />
          </button>
          <button
            className="review-card-delete-btn"
            onClick={onDelete}
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
            className={`star-static ${k < rating.rating ? 'star-shown' : ''}`}
          >
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
          </svg>
        ))}
      </div>
      {categoryRatings.length > 0 && (
        <div className="review-category-ratings">
          {categoryRatings.map(category => (
            <div className="review-category-rating" key={category.label}>
              <span className="review-category-label">{category.label}</span>

              <StarRating currentRating={category.rating} viewOnly={true} />

              <span className="review-category-value">{category.rating}/5</span>
            </div>
          ))}
        </div>
      )}
      <MarkdownReviewBody
        setHasOverflow={b => {
          if (!hasOverflow && b) setHasOverflow(b);
        }}
        unlimitedSize={showExpanded}
        searchQuery={searchQuery}
      >
        {rating.review}
      </MarkdownReviewBody>
      {hasOverflow && (
        <Button
          variant="secondary"
          onClick={() => setShowExpanded(!showExpanded)}
        >
          {showExpanded ? t('collapseReview') : t('expandReview')}
        </Button>
      )}
      <div className="review-helpful-row">
        <button
          type="button"
          className={`review-like-button ${
            rating.likedByCurrentUser ? 'review-like-button-selected' : ''
          }`}
          onClick={handleLike}
          disabled={isLiking || !canLike}
          aria-busy={isLiking}
          title={canLike ? undefined : "You can't like your own review"}
          aria-pressed={rating.likedByCurrentUser ?? false}
          aria-label={`Mark this review as helpful. ${rating.likeCount ?? 0} likes`}
        >
          <FaThumbsUp aria-hidden="true" />
          <span>
            {isLiking
              ? 'Saving...'
              : rating.likedByCurrentUser
                ? 'Unlike'
                : 'Helpful'}
          </span>
          <span>({rating.likeCount ?? 0})</span>
        </button>
        {likeError && <span className="review-like-error">{likeError}</span>}
      </div>
    </div>
  );
}
