import * as React from 'react';
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUser } from '@/context/UserContext';
import { getAllReviews } from './Api/getAllCustomerReviews';
import { ReviewResponseDTO } from './Model/ReviewResponseDTO';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';

const CustomerReviewsList: React.FC = (): JSX.Element => {
  const [reviewList, setReviewList] = useState<ReviewResponseDTO[]>([]);
  const [showConfirmDialog, setShowConfirmDialog] = useState<boolean>(false);
  const navigate = useNavigate();
  const { user } = useUser();

  useEffect(() => {
    const fetchReviewsData = async (): Promise<void> => {
      try {
        const response = await getAllReviews(user.userId);
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
  }, [user.userId]);

  const handleDeleteAllReviews = (confirm: boolean): void => {
    if (confirm) {
      // Logic to delete all reviews (e.g., API call to delete)
      setReviewList([]);
      setShowConfirmDialog(false);
    } else {
      setShowConfirmDialog(false);
    }
  };

  const handleDelete = async (reviewId: number): Promise<void> => {
    const confirmDelete = window.confirm(
      `Are you sure you want to delete review with ID: ${reviewId}?`
    );
    if (confirmDelete) {
      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/visits/owners/${user.userId}/reviews/${reviewId}`,
          {
            method: 'DELETE',
            credentials: 'include',
          }
        );
        if (response.ok) {
          setReviewList(prev =>
            prev.filter(review => review.reviewId !== reviewId)
          );
          alert('Review deleted successfully!');
        } else {
          console.error('Failed to delete the review.');
          alert('Failed to delete the review.');
        }
      } catch (error) {
        console.error('Error deleting review:', error);
        alert('Error deleting review.');
      }
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
                    onClick={() => handleDelete(review.reviewId)}
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
        onClick={() => navigate(AppRoutePaths.CustomerVisits)}
        title="Let a review"
        //CAMBIAR ESTOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO
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

export default CustomerReviewsList;
