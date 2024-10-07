import { ProductModel } from '@/features/products/models/ProductModels/ProductModel.ts';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import { useState, useEffect, JSX } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { updateUserRating } from './updateUserRating';
import { getProduct } from './getProduct';
import { deleteUserRating } from './deleteUserRating';
import './ProductDetails.css';
import StarRating from '../components/StarRating';
import ReviewBox from '../components/ReviewBox';
import { RatingModel } from '../models/ProductModels/RatingModel';
import { getUserRatingsForProduct } from './getUserRatingsForProduct';
import { getUserRating } from './getUserRating';

export default function ProductDetails(): JSX.Element {
  const location = useLocation();
  const navigate = useNavigate();
  const { product, rating } = location.state as {
    product: ProductModel;
    rating: RatingModel;
  };
  const [currentProduct, setCurrentProduct] = useState<ProductModel>(product);
  const [currentUserRating, setUserRating] = useState<RatingModel>({
    rating: rating.rating,
    review: rating.review,
  });
  const [productReviews, setProductReviews] = useState<RatingModel[]>([]);
  const navigateToEditProduct = (): void => {
    navigate(`/products/edit/${product.productId}`, { state: { product } });
  };

  const fetchRatings = async (): Promise<void> => {
    const reviews = await getUserRatingsForProduct(currentProduct.productId);
    setProductReviews(reviews.filter((r: RatingModel) => r.review !== ''));
  };

  const fetchRating = async (): Promise<void> => {
    try {
      const rating = await getUserRating(product.productId);
      setUserRating(rating);
    } catch (err) {
      console.error('Failed to fetch current rating', err);
    }
  };

  const deleteRating = async (): Promise<void> => {
    try {
      await deleteUserRating(product.productId);
      setUserRating({ rating: 0, review: '' });
      const resRefresh = await getProduct(product.productId);
      setCurrentProduct(resRefresh);
    } catch (err) {
      console.error('Could not delete data', err);
    }
  };

  const updateRating = async (
    newRating: number,
    newReview: string | null
  ): Promise<void> => {
    if (newRating == 0) return deleteRating();
    try {
      const resUpdate = await updateUserRating(
        product.productId,
        newRating,
        newReview
      );
      setUserRating(resUpdate);
      const resRefresh = await getProduct(product.productId);
      setCurrentProduct(resRefresh);
    } catch (err) {
      console.error('Could not update/fetch product ratings', err);
    }
  };

  useEffect(() => {
    fetchRatings();
    fetchRating();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const renderProductDescription = (productName: string): JSX.Element => {
    switch (productName) {
      case 'Horse Saddle':
        return (
          <>
            Horse Saddle - Classic Leather Western Saddle Brand: EquiStyle Size:
            Size: 16&quot; seat The Classic Leather Western Saddle combines
            durability and comfort for both horse and rider. Crafted from
            high-quality leather, it features a padded seat and adjustable
            stirrups for a customized fit. Ideal for trail riding and everyday
            use, this saddle ensures a secure and enjoyable ride.
          </>
        );
      case 'Rabbit Hutch':
        return (
          <>
            Rabbit Hutch - Deluxe Wooden Enclosure Brand: Happy Bunnies
            Dimensions: 48&quot; L x 24&quot; W x 36&quot; H<br />
            Our Deluxe Wooden Rabbit Hutch features a spacious design with
            multiple levels, providing a safe and comfortable home for your
            rabbits. Made from weather-resistant wood, it includes a large run
            for exercise and a cozy sleeping area. The easy-access doors ensure
            convenient cleaning and feeding.
          </>
        );
      case 'Dog Food':
        return (
          <>
            Premium Dog Food - Chicken & Brown Rice Brand: Furry Friends 30lbs
            <br />
            Made with real chicken and brown rice, our Premium Dog Food offers
            balanced nutrition for dogs of all ages. Rich in omega fatty acids
            for a shiny coat and digestive health, this formula contains no
            artificial preservatives. Ideal for keeping your furry friend happy
            and energized!
          </>
        );
      case 'Fish Tank Heater':
        return (
          <>
            Fish Tank Heater - Adjustable 300W Submersible Heater AquaSafe
            Power: 300 watts The Adjustable 300W Submersible Heater is perfect
            for maintaining optimal water temperature in aquariums up to 75
            gallons. Featuring an easy-to-read LED display and adjustable
            temperature settings, this heater ensures a stable environment for
            your fish. Its durable design and automatic shut-off function
            provide added safety.
          </>
        );
      case 'Cat Litter':
        return (
          <>
            Cat Litter - Premium Clumping Cat Litter Brand: Purrfect Choice
            Weight: 20 lbs Our Premium Clumping Cat Litter offers exceptional
            odor control and easy cleanup. Made from natural clay, it forms
            tight clumps for effortless scooping and minimizes dust for a
            cleaner home. Safe for cats and the environment, this litter ensures
            your feline friend stays happy and healthy.
          </>
        );
      case 'Flea Collar':
        return (
          <>
            Flea Collar - Advanced Flea and Tick Control Brand: PetGuard
            Adjustable, fits necks up to 22&quot; The Advanced Flea and Tick
            Collar provides long-lasting protection for your pet against fleas
            and ticks for up to 8 months. Made with natural ingredients, it’s
            safe for dogs and cats. The adjustable design ensures a comfortable
            fit, keeping your furry friend protected and pest-free.
          </>
        );
      case 'Bird Cage':
        return (
          <>
            Bird Cage - Spacious Double-Door Aviary Brand: Feather Haven
            Dimensions: 36&quot; L x 24&quot; W x 60&quot; H<br />
            The Spacious Double-Door Aviary is designed for small to
            medium-sized birds, offering ample room for play and exercise.
            Featuring sturdy construction, multiple perches, and easy-access
            doors for cleaning, this cage ensures a safe and comfortable
            environment for your feathered friends.
          </>
        );
      case 'Aquarium Filter':
        return (
          <>
            Aquarium Filter - 50-Gallon Canister Filter Brand: Crystal Clear
            Flow Rate: 300 GPH The 50-Gallon Canister Filter provides powerful
            filtration for aquariums up to 50 gallons. Featuring a multi-stage
            filtration system, it effectively removes debris, toxins, and odors,
            ensuring a clean and healthy environment for your fish. The quiet
            operation and easy setup make it a perfect choice for any aquarium
            enthusiast.
          </>
        );
      default:
        return <>Description not available</>;
    }
  };

  return (
    <>
      <NavBar />
      <h1>{currentProduct.productName}</h1>
      <p>{renderProductDescription(currentProduct.productName)}</p>
      <p>Price: ${currentProduct.productSalePrice.toFixed(2)}</p>
      <p>Average Rating: {currentProduct.averageRating} / 5</p>

      <button className="edit-button" onClick={navigateToEditProduct}>
        Edit Product
      </button>
      <p>Your Rating:</p>
      <StarRating
        currentRating={currentUserRating}
        updateRating={updateRating}
      />
      <ReviewBox
        updateFunc={(newReview: string) =>
          updateRating(currentUserRating.rating, newReview)
        }
        rating={currentUserRating}
      />
      <p>Product Feedback:</p>
      <ul>
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
                    className={`star-static ${k < rating.rating ? 'shown' : ''}`}
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
      </ul>
    </>
  );
}
