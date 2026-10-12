import {
  emptyProductModel,
  ProductModel,
} from '@/features/products/models/ProductModels/ProductModel';
import { NavBar } from '@/layouts/AppNavBar';
import { JSX, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getProduct } from '../api/getProduct';
import './ProductDetails.css';
import StarRating from './StarRating';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { AxiosError } from 'axios';
import ImageContainer from './ImageContainer';
import { Button } from 'react-bootstrap';
import {
  IsAdmin,
  IsInventoryManager,
  IsReceptionist,
  IsVet,
  useUser,
} from '@/context/UserContext';
import RecentlyViewedProducts from '@/features/products/components/RecentlyViewedProducts';
import { useAddToCart } from '@/features/carts/api/addToCartFromProducts';
import { useAddToWishlist } from '@/features/carts/api/addToWishlistFromProducts';
import { FaCheck, FaHeart, FaTimes } from 'react-icons/fa';
import { ReviewsList } from '@/features/products/components/ReviewsList.tsx';

export default function ProductDetails(): JSX.Element {
  const isAdmin = IsAdmin();
  const { isAuthenticated } = useUser();
  const isInventoryManager = IsInventoryManager();
  const isVet = IsVet();
  const isReceptionist = IsReceptionist();
  const isStaff = isAdmin || isInventoryManager || isVet || isReceptionist;
  const [reviewCount, setReviewCount] = useState(0);

  const navigate = useNavigate();
  const { productId } = useParams();
  const { addToCart } = useAddToCart();
  //------------------------------------------------------
  const { addToWishlist } = useAddToWishlist();
  const [quantity, setQuantity] = useState(1);
  const handleMinus = (): void => {
    if (quantity > 1) setQuantity(quantity - 1);
  };
  const handlePlus = (): void => {
    setQuantity(quantity + 1);
  };

  const [successMessageWishlist, setSuccessMessageWishlist] = useState<
    string | null
  >(null);

  const handleAddToWishlist = async (): Promise<void> => {
    if (!isAuthenticated) {
      navigate(AppRoutePaths.Login);
      return;
    }
    const isSuccess = await addToWishlist(currentProduct.productId, 1);
    if (isSuccess) {
      setSuccessMessageWishlist('Product added to wishlist successfully!');

      // Clear the message after 3 seconds
      setTimeout(() => setSuccessMessageWishlist(null), 3000);
    }
  };

  const [currentProduct, setCurrentProduct] =
    useState<ProductModel>(emptyProductModel);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const getProductTypeLabel = (productType: string): string => {
    return productType
      ? productType.charAt(0).toUpperCase() + productType.slice(1).toLowerCase()
      : 'Unknown';
  };
  const getDeliveryTypeLabel = (deliveryType: string): string => {
    if (deliveryType === 'DELIVERY') return 'Standard Delivery';
    if (deliveryType === 'PICKUP') return 'Pickup';
    if (deliveryType === 'DELIVERY_AND_PICKUP') return 'Delivery & Pickup';
    if (deliveryType === 'NO_DELIVERY_OPTION') return 'No delivery option';
    return 'Unknown Delivery Type';
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
  useEffect(() => {
    fetchProduct();
  }, [productId, isAuthenticated]);

  const isUnlisted = currentProduct.isUnlisted;

  const handleAddToCartClick = async (): Promise<void> => {
    if (!isAuthenticated) {
      navigate(AppRoutePaths.Login);
      return;
    }
    if (!productId) return;
    if (isStaff) return;
    const ok = await addToCart(String(productId), quantity);
    alert(
      ok
        ? `Added ${quantity} item${quantity > 1 ? 's' : ''} to cart`
        : "Couldn't add to cart. Please try again."
    );
  };

  return (
    <>
      <NavBar />
      {isLoading ? (
        <div>
          <p>Is Loading..</p>
        </div>
      ) : (
        <div
          className={
            isUnlisted && !isAdmin && !isInventoryManager ? 'no-grid' : ''
          }
        >
          <div className="product-container">
            {isUnlisted && !isAdmin && !isInventoryManager ? (
              <div className="product-unavailable">
                <h2>Item Unavailable</h2>
                <h3>This item has been unlisted. Check back later! </h3>
              </div>
            ) : (
              <>
                <div className="productimage-container">
                  <ImageContainer
                    image={currentProduct.image}
                    imageId={currentProduct.imageId}
                  />
                </div>
                <div className="productdetails-container">
                  <div
                    className="productadmin-container"
                    style={{
                      visibility: `${
                        isAdmin || isInventoryManager ? 'visible' : 'hidden'
                      }`,
                    }}
                  ></div>

                  <h2>
                    {currentProduct.productName}
                    {!isInventoryManager && !isVet && !isReceptionist && (
                      <button
                        onClick={handleAddToWishlist}
                        className="wishlist-btn"
                      >
                        <FaHeart className="wishlist-icon" /> Wishlist{' '}
                      </button>
                    )}
                  </h2>
                  {successMessageWishlist && (
                    <p className="success-message">{successMessageWishlist}</p>
                  )}

                  <div className="avgrating-container">
                    <StarRating
                      currentRating={currentProduct.averageRating}
                      viewOnly={true}
                    />
                    <h3>{currentProduct.averageRating} </h3>
                    <div className="review-nums">
                      {reviewCount || 0} reviews{' '}
                    </div>
                  </div>
                  <div className="line"></div>

                  <div className="details-type">
                    Type:
                    <span className="box-details">
                      {getProductTypeLabel(currentProduct.productType)}
                    </span>
                    Delivery Type:
                    <span className="box-details">
                      {getDeliveryTypeLabel(currentProduct.deliveryType)}
                    </span>
                  </div>

                  <h3 className="prod-price">
                    {currentProduct.productSalePrice}$
                  </h3>

                  <div className="stock-details">
                    <div className="inStock">
                      {currentProduct.productQuantity >= 10 && (
                        <p>
                          {' '}
                          <FaCheck /> In Stock - Available for Pickup
                        </p>
                      )}
                    </div>
                    <div className="outOfStock">
                      {currentProduct.productQuantity === 0 && (
                        <p>
                          {' '}
                          <FaTimes /> Out of Stock
                        </p>
                      )}
                    </div>
                    <div className="lowStock">
                      {currentProduct.productQuantity > 0 &&
                        currentProduct.productQuantity < 10 && (
                          <p>
                            {' '}
                            <FaCheck /> Only a few left!
                          </p>
                        )}
                    </div>
                  </div>
                  <h3 className="prod-desc-title">About this Product</h3>
                  <p className="prod-description">
                    {currentProduct.productDescription}
                  </p>
                  <div className=" cart-box">
                    <div className=" quantity-selector">
                      <button onClick={handleMinus} className="qty-btn">
                        {'-'}
                      </button>
                      <input className="qty-input" value={quantity} readOnly />
                      <button onClick={handlePlus} className="qty-btn">
                        {'+'}
                      </button>
                    </div>

                    {/* Single, final Add to Cart block */}
                    {!isInventoryManager && !isVet && !isReceptionist && (
                      <div className="cartactions-container">
                        <Button
                          onClick={handleAddToCartClick}
                          disabled={isStaff}
                          aria-disabled={isStaff}
                        >
                          Add to Cart
                        </Button>
                      </div>
                    )}
                  </div>
                </div>
                <div className="product-review-separator"></div>
                <ReviewsList
                  productId={productId}
                  isAuthenticated={isAuthenticated}
                  isStaff={isStaff}
                  onReviewModification={async () => {
                    if (!productId) return;
                    const resRefresh = await getProduct(productId);
                    setCurrentProduct(resRefresh);
                  }}
                  onModifyReviews={setReviewCount}
                  currentProduct={currentProduct}
                />
              </>
            )}
          </div>
        </div>
      )}
      <div>
        <RecentlyViewedProducts />
      </div>
    </>
  );
}
