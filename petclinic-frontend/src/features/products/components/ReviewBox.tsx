import { JSX, useEffect, useState } from 'react';
import { Form, Alert, ToggleButton } from 'react-bootstrap';
import Stack from 'react-bootstrap/Stack';
import { RatingModel } from '../models/ProductModels/RatingModel';
import './ReviewBox.css';
import { MarkdownReviewBody } from '@/features/products/components/MarkdownReviewBody.tsx';

function ReviewBox({
  updateFunc,
  rating,
}: {
  updateFunc: (newReview: string) => void;
  rating: RatingModel;
}): JSX.Element {
  const [reviewText, setReviewText] = useState<string>(rating.review);
  const [isError, setError] = useState<string | null>(null);
  const [previewToggle, setPreviewToggle] = useState<boolean>(true);

  useEffect(() => {
    setReviewText(rating.review);
  }, [rating.review]);
  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleLocalChange = (text: string) => {
    if (text.length > 2000) {
      setError('Review cannot exceed 2000 characters!');
    } else {
      setError(null);
      updateFunc(text); // propagate changes to parent
    }
    setReviewText(text);
  };

  return (
    <div className="reviewbox-container">
      <ToggleButton
        id="preview-toggle"
        type="checkbox"
        variant={previewToggle ? 'primary' : 'secondary'}
        checked={previewToggle}
        value="1"
        onChange={e => setPreviewToggle(e.target.checked)}
      >
        Preview
      </ToggleButton>
      {isError && <Alert variant="warning">{isError}</Alert>}
      <div>
        <Stack direction="vertical" gap={3}>
          <Form.Control
            as="textarea"
            className="review-box"
            placeholder="Leave your review here..."
            value={reviewText}
            onChange={e => handleLocalChange(e.target.value)}
          />
          {previewToggle && (
            <div className="reviewbox-preview-body">
              <MarkdownReviewBody>
                {reviewText != ''
                  ? reviewText
                  : '**Your preview will appear here**, __markdown is supported__'}
              </MarkdownReviewBody>
            </div>
          )}
        </Stack>
      </div>
    </div>
  );
}

export default ReviewBox;
