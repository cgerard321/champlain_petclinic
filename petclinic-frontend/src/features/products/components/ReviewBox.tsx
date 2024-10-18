import { JSX, useEffect, useState } from 'react';
import { Alert, Button, Form } from 'react-bootstrap';
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
  const [wasSubmitted, setSubmitted] = useState<boolean>(rating.review !== '');
  const [isError, setError] = useState<string | null>();

  useEffect(() => {
    setSubmitted(rating.review !== '');
    setReviewText(rating.review);
  }, [rating.review]);

  return (
    <div className="reviewbox-container">
      {wasSubmitted === false ? (
        <>
          {isError && <Alert variant="warning">{isError}</Alert>}
          <Form.Control
            as="textarea"
            rows={3}
            className="review-box"
            placeholder="Leave your review here.."
            onChange={e => setReviewText(e.target.value)}
            value={reviewText}
          ></Form.Control>
          <Button
            onClick={() => {
              updateFunc(reviewText);
              if (rating.review.length > 2000) {
                setError('Review cannot exceed 2000 characters!');
              }
              if (rating.rating !== 0) {
                setSubmitted(true);
                setError(null);
              } else {
                setError('Rating must be set (1-5)!');
              }
            }}
          >
            Submit
          </Button>
        </>
      ) : (
        <>
          <h5>You previously left a review on this product:</h5>
          <p className="past-review">{reviewText}</p>
          <Button onClick={() => setSubmitted(false)}>Edit</Button>
        </>
      )}
    </div>
  );
}

export default ReviewBox;
