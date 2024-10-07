import { JSX, useEffect, useState } from 'react';
import { Alert, Button } from 'react-bootstrap';
import { RatingModel } from '../models/ProductModels/RatingModel';

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
    <div>
      {wasSubmitted === false ? (
        <>
          {isError && <Alert variant="warning">{isError}</Alert>}
          <textarea
            className="review-box"
            placeholder="Leave your review here.."
            onChange={e => setReviewText(e.target.value)}
            value={reviewText}
          ></textarea>
          <Button
            onClick={() => {
              updateFunc(reviewText);
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
          <p>You left a review on this product:</p>
          <p>{reviewText}</p>
          <Button onClick={() => setSubmitted(false)}>Edit</Button>
        </>
      )}
    </div>
  );
}

export default ReviewBox;
