import { useTranslation } from 'react-i18next';
import { JSX, useState } from 'react';
import { RatingModel } from '@/features/products/models/ProductModels/RatingModel.ts';
import defaultProfile from '@/assets/Customers/defaultProfilePicture.png';
import { MarkdownReviewBody } from '@/features/products/components/MarkdownReviewBody.tsx';
import { FaPen, FaTrash } from 'react-icons/fa';
import { Button } from 'react-bootstrap';

interface ReviewProps {
  rating: RatingModel;
  index: number;
  canEdit: boolean;
  onEdit: () => void;
  onDelete: () => void;
  searchQuery?: string;
}

export function Review({
  rating,
  canEdit,
  onEdit,
  onDelete,
  searchQuery,
}: ReviewProps): JSX.Element {
  const { t } = useTranslation('products');
  const [showExpanded, setShowExpanded] = useState(false);
  const [hasOverflow, setHasOverflow] = useState(false);

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
    </div>
  );
}
