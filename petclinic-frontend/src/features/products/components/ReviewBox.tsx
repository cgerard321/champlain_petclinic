import { JSX, useEffect, useState } from 'react';
import { Form, Alert } from 'react-bootstrap';
import { RatingModel } from '../models/ProductModels/RatingModel';
import './ReviewBox.css';

function ReviewBox({
  updateFunc,
  rating,
}: {
  updateFunc: (newReview: string) => void;
  rating: RatingModel;
}): JSX.Element {
  const [reviewText, setReviewText] = useState<string>(rating.review);
  const [isError, setError] = useState<string | null>(null);

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
      {isError && <Alert variant="warning">{isError}</Alert>}
      <Form.Control
        as="textarea"
        className="review-box"
        placeholder="Leave your review here..."
        value={reviewText}
        onChange={e => handleLocalChange(e.target.value)} // now used
      />
    </div>
  );
}

export default ReviewBox;
