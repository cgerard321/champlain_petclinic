import * as React from 'react';
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllReviews } from './Api/getAllReviews';
import { ReviewResponseDTO } from './Model/ReviewResponseDTO';

const ReviewsList: React.FC = (): JSX.Element => {
  const [reviewList, setReviewList] = useState<ReviewResponseDTO[]>([]);
  const [showConfirmDialog, setShowConfirmDialog] = useState<boolean>(false);
  const navigate = useNavigate();

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

    fetchReviewsData().catch(error =>
      console.error('Error in fetchReviewsData:', error)
    );
  }, []);

  const handleDeleteAllReviews = (confirm: boolean): void => {
    if (confirm) {
      // Logic to delete all reviews (e.g., API call to delete)
      setReviewList([]);
      setShowConfirmDialog(false);
    } else {
      setShowConfirmDialog(false);
    }
  };

  return (
    <div className="reviews-container">
      <h1>Reviews</h1>
      <table className="reviews-table">
        <thead>
          <tr>
            <th>Reviewer Name</th>
            <th>Review</th>
            <th>Rating</th>
            <th>Date Submitted</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {reviewList.length > 0 ? (
            reviewList.map(review => (
              <tr key={review.reviewId}>
                <td>{review.reviewerName}</td>
                <td>{review.review}</td>
                <td>{review.rating}/5</td>
                <td>{new Date(review.dateSubmitted).toLocaleDateString()}</td>
                <td>
                  <button
                    className="btn btn-warning"
                    onClick={() =>
                      navigate(`/updateReview/${review.reviewId}/edit`)
                    }
                    title="Edit"
                  >
                    Edit
                  </button>
                  <button
                    className="btn btn-danger"
                    onClick={() => {
                      // Add logic to delete a single review
                    }}
                    title="Delete"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan={5} className="text-center">
                No reviews available
              </td>
            </tr>
          )}
        </tbody>
      </table>

      <button
        className="btn btn-warning"
        onClick={() => navigate('/visits')}
        title="Let a review"
      >
        Return to visits
      </button>

      <button
        className="delete-all-reviews-button btn btn-success"
        onClick={() => setShowConfirmDialog(true)}
      >
        Delete All Reviews
      </button>

      {showConfirmDialog && (
        <>
          <div
            className="overlay"
            onClick={() => setShowConfirmDialog(false)}
          ></div>
          <div className="confirm-dialog">
            <p>Are you sure you want to delete all reviews?</p>
            <button
              className="btn-danger mx-1"
              onClick={() => handleDeleteAllReviews(true)}
            >
              Yes
            </button>
            <button
              className="btn-warning mx-1"
              onClick={() => setShowConfirmDialog(false)}
            >
              No
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default ReviewsList;
