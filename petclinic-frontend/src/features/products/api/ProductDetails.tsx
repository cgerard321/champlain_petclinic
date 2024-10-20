import {
  ProductModel,
  emptyProductModel,
} from '@/features/products/models/ProductModels/ProductModel.ts';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import { useState, useEffect, JSX } from 'react';
import { generatePath, useNavigate, useParams } from 'react-router-dom';
import { updateUserRating } from './updateUserRating';
import { getProduct } from './getProduct';
import { deleteUserRating } from './deleteUserRating';
import './ProductDetails.css';
import StarRating from '../components/StarRating';
import ReviewBox from '../components/ReviewBox';
import { RatingModel } from '../models/ProductModels/RatingModel';
import { getUserRatingsForProduct } from './getUserRatingsForProduct';
import { getUserRating } from './getUserRating';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { AxiosError } from 'axios';
import ImageContainer from '../components/ImageContainer';
import { Button } from 'react-bootstrap';
import { IsAdmin, IsInventoryManager } from '@/context/UserContext';
import PatchListingStatusButton from '../components/PatchListingStatusButton';

export default function ProductDetails(): JSX.Element {
  const isAdmin = IsAdmin();
  const isInventoryManager = IsInventoryManager();
  const navigate = useNavigate();
  const { productId } = useParams();
  const [currentProduct, setCurrentProduct] =
    useState<ProductModel>(emptyProductModel);
  const [currentUserRating, setUserRating] = useState<RatingModel>({
    rating: 0,
    review: '',
  });
  const [productReviews, setProductReviews] = useState<RatingModel[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const navigateToEditProduct = (): void => {
    if (!currentProduct || !productId) return;
    navigate(
      generatePath(AppRoutePaths.EditProduct, {
        productId: productId,
      }),
      {
        state: { product: currentProduct },
      }
    );
  };

  const fetchProduct = async (): Promise<void> => {
    if (!productId) return;
    getProduct(productId)
      .then(res => {
        setCurrentProduct(res);
        setIsLoading(false);
      })
      .catch((err: AxiosError) => {
        switch (err.status) {
          case 404:
          case 422:
            navigate(AppRoutePaths.PageNotFound);
            break;
          default:
            navigate(AppRoutePaths.InternalServerError);
            console.error('Failed to fetch product', err);
            break;
        }
      });
  };

  const fetchRatings = async (): Promise<void> => {
    if (!productId) return;
    try {
      const reviews = await getUserRatingsForProduct(productId);
      setProductReviews(reviews.filter((r: RatingModel) => r.review !== ''));
    } catch (err) {
      console.error('Failed to fetch product ratings', err);
    }
  };

  const fetchRating = async (): Promise<void> => {
    if (!productId) return;
    try {
      const rating = await getUserRating(productId);
      setUserRating(rating);
    } catch (err) {
      console.error('Failed to fetch current rating', err);
    }
  };

  const deleteRating = async (): Promise<void> => {
    if (!productId) return;
    try {
      await deleteUserRating(productId);
      setUserRating({ rating: 0, review: '' });
      const resRefresh = await getProduct(productId);
      setCurrentProduct(resRefresh);
    } catch (err) {
      console.error('Could not delete data', err);
    }
  };

  const updateRating = async (
    newRating: number,
    newReview: string | null
  ): Promise<void> => {
    if (!productId) return;
    // We await because we want to avoid race condition with fetchRatings in backend
    if (newRating == 0) await deleteRating();
    else {
      try {
        const resUpdate = await updateUserRating(
          productId,
          newRating,
          newReview
        );
        setUserRating(resUpdate);
        const resRefresh = await getProduct(productId);
        setCurrentProduct(resRefresh);
      } catch (err) {
        console.error('Could not update/fetch product ratings', err);
      }
    }
    fetchRatings();
  };

  useEffect(() => {
    fetchProduct();
    fetchRatings();
    fetchRating();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const isUnlisted = currentProduct.isUnlisted;

  return (
    <>
      <NavBar />
      {isLoading ? (
        <div>
          <p>Is Loading..</p>
        </div>
      ) : (
        <div className="product-container">
          {isUnlisted && !isAdmin && !isInventoryManager ? (
            <div className="product-unavailable">
              <h2>Product Unavailable</h2>
              <h3>This product has been unlisted. Check back later!😊</h3>
          <div className="productimage-container">
            <ImageContainer imageId={currentProduct.imageId} />
          </div>
          <div className="productdetails-container">
            <div
              className="productadmin-container"
              style={{ visibility: `${isAdmin ? 'visible' : 'hidden'}` }}
            >
              <Button variant="warning" onClick={navigateToEditProduct}>
                Edit
              </Button>
              <Button
                variant="danger"
                onClick={() =>
                  alert('This feature has not yet been implemented')
                }
              >
                Delete
              </Button>
            </div>
            <h2>{currentProduct.productName}</h2>
            <h3>{currentProduct.productSalePrice}$</h3>
            <div className="avgrating-container">
              <StarRating
                currentRating={currentProduct.averageRating}
                viewOnly={true}
              />
              <h3>{currentProduct.averageRating} / 5</h3>
            </div>
            {/* <div className="quantityalert-container">
              {currentProduct.status}
            </div> */}
            <div className="cartactions-container">
              <Button
                onClick={() =>
                  alert('This feature has not yet been implemented')
                }
              >
                Add to Cart
              </Button>
            </div>
            <p>Type: {currentProduct.productType}</p>
            <h3>Description</h3>
            <p>{currentProduct.productDescription}</p>
          </div>
          <div className="review-section-container">
            <div className="reviewproduct-container">
              <h2>Review</h2>
              <p>Leave a rating:</p>
              <StarRating
                currentRating={currentUserRating.rating}
                viewOnly={false}
                updateRating={updateRating}
              />
              <ReviewBox
                updateFunc={(newReview: string) =>
                  updateRating(currentUserRating.rating, newReview)
                }
                rating={currentUserRating}
              />
            </div>
            <br></br>
            <h3>Users feedback</h3>
            <div className="reviewsforproduct-container">
              {productReviews.length > 0 ? (
                productReviews?.map((rating: RatingModel, index: number) => (
                  <div key={index} className="reviewbox">
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
                    <p>{rating.review}</p>
                  </div>
                ))
              ) : (
                <p>This product does not have any reviews yet!</p>
              )}
            </div>
          ) : (
            <>
              <div className="productimage-container">
                <ImageContainer imageId={currentProduct.imageId} />
              </div>
              <div className="productdetails-container">
                <div
                  className="productadmin-container"
                  style={{
                    visibility: `${isAdmin || isInventoryManager ? 'visible' : 'hidden'}`,
                  }}
                >
                  <Button variant="warning" onClick={navigateToEditProduct}>
                    Edit
                  </Button>
                  {productId && (
                    <PatchListingStatusButton productId={productId} />
                  )}
                  <Button
                    variant="danger"
                    onClick={() =>
                      alert('This feature has not yet been implemented')
                    }
                  >
                    Delete
                  </Button>
                </div>
                <h2>{currentProduct.productName}</h2>
                <h3>{currentProduct.productSalePrice}$</h3>
                <div className="avgrating-container">
                  <StarRating
                    currentRating={currentProduct.averageRating}
                    viewOnly={true}
                  />
                  <h3>{currentProduct.averageRating} / 5</h3>
                </div>
                <div className="quantityalert-container">
                  {currentProduct.status}
                </div>
                <div className="cartactions-container">
                  <Button
                    onClick={() =>
                      alert('This feature has not yet been implemented')
                    }
                  >
                    Add to Cart
                  </Button>
                </div>
                <p>Type: {currentProduct.productType}</p>
                <h3>Description</h3>
                <p>{currentProduct.productDescription}</p>
              </div>
              <div className="review-section-container">
                <div className="reviewproduct-container">
                  <h2>Review</h2>
                  <p>Leave a rating:</p>
                  <StarRating
                    currentRating={currentUserRating.rating}
                    viewOnly={false}
                    updateRating={updateRating}
                  />
                  <ReviewBox
                    updateFunc={(newReview: string) =>
                      updateRating(currentUserRating.rating, newReview)
                    }
                    rating={currentUserRating}
                  />
                </div>
                <br></br>
                <h3>Users feedback</h3>
                <div className="reviewsforproduct-container">
                  {productReviews.length > 0 ? (
                    productReviews?.map(
                      (rating: RatingModel, index: number) => (
                        <div key={index} className="reviewbox">
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
                          <p>{rating.review}</p>
                        </div>
                      )
                    )
                  ) : (
                    <p>This product does not have any reviews yet!</p>
                  )}
                </div>
              </div>
            </>
          )}
        </div>
      )}
    </>
  );
}
