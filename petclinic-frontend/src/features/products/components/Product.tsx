import { JSX, useEffect, useState } from 'react';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import { getProductByProductId } from '@/features/products/api/getProductByProductId.tsx';
import ImageContainer from './ImageContainer';
import { changeProductQuantity } from '../api/changeProductQuantity';
import { generatePath, useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import './Product.css';
import { useAddToCart } from '@/features/carts/api/addToCartFromProducts.ts';
import {
  IsInventoryManager,
  IsVet,
  IsReceptionist,
  useUser,
} from '@/context/UserContext';
import { useAddToWishlist } from '@/features/carts/api/addToWishlistFromProducts';

import type { Role } from '@/shared/models/Role';

export default function Product({
  product,
}: {
  product: ProductModel;
}): JSX.Element {
  const isInventoryManager = IsInventoryManager();
  const isVet = IsVet();
  const isReceptionist = IsReceptionist();

  const { user } = useUser();

  // ---- Rôles utilisateur (simple et lisible) ----
  const roleNames = new Set<string>();
  const rolesSet = user?.roles as Set<Role> | undefined;

  if (rolesSet) {
    for (const role of rolesSet) {
      roleNames.add(role.name);
    }
  }

  const isAdmin = roleNames.has('ADMIN');
  const isStaff = isAdmin || isInventoryManager || isVet || isReceptionist;

  const [currentProduct, setCurrentProduct] = useState<ProductModel>(product);
  const [selectedProduct, setSelectedProduct] = useState<ProductModel | null>(
    null
  );
  const [selectedProductForQuantity, setSelectedProductForQuantity] =
    useState<ProductModel | null>(null);
  const [quantity, setQuantity] = useState<number>(0);
  const [, setTooLong] = useState<boolean>(false); //tooLong

  const navigate = useNavigate();
  const { addToCart } = useAddToCart();
  const { addToWishlist } = useAddToWishlist();
  const [successMessageCart, setSuccessMessageCart] = useState<string | null>(
    null
  );
  const [successMessageWishlist, setSuccessMessageWishlist] = useState<
    string | null
  >(null);

  const handleProductTitleClick = (): void => {
    navigate(
      generatePath(AppRoutePaths.ProductDetails, {
        productId: product.productId,
      })
    );
  };
  const getDeliveryTypeLabel = (deliveryType: string): string => {
    if (deliveryType === 'DELIVERY') {
      return 'Delivery';
    } else if (deliveryType === 'PICKUP') {
      return 'Pickup';
    } else if (deliveryType === 'DELIVERY_AND_PICKUP') {
      return 'Delivery & Pickup';
    } else if (deliveryType === 'NO_DELIVERY_OPTION') {
      return 'No delivery option';
    }
    return 'Unknown Delivery Type';
  };

  useEffect(() => {
    if (product.productDescription.length > 100) {
      setTooLong(true);
    } else {
      setTooLong(false);
    }
  }, [product.productDescription]);

  const handleProductClickForProductQuantity = async (
    productId: string
  ): Promise<void> => {
    try {
      const product = await getProductByProductId(productId);
      setSelectedProductForQuantity(product);
      setQuantity(product.productQuantity);
    } catch (error) {
      console.error('Failed to fetch product details:', error);
    }
  };

  const handleQuantitySubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    if (selectedProductForQuantity) {
      try {
        await changeProductQuantity(
          selectedProductForQuantity.productId,
          quantity
        );
        const updatedProduct = await getProductByProductId(
          selectedProductForQuantity.productId
        );
        setCurrentProduct(updatedProduct);
        setSelectedProductForQuantity(null); // Close the quantity update form
      } catch (error) {
        console.error('Failed to update product quantity:', error);
      }
    }
  };

  const handleBackToList = (): void => {
    setSelectedProduct(null);
    setSelectedProductForQuantity(null);
  };

  const handleAddToCart = async (): Promise<void> => {
    const isSuccess = await addToCart(currentProduct.productId);
    if (isSuccess) {
      setSuccessMessageCart('Product added to cart successfully!');
      setTimeout(() => setSuccessMessageCart(null), 3000);
    }
  };

  const handleAddToWishlist = async (): Promise<void> => {
    const isSuccess = await addToWishlist(currentProduct.productId, 1);
    if (isSuccess) {
      setSuccessMessageWishlist('Product added to wishlist successfully!');
      setTimeout(() => setSuccessMessageWishlist(null), 3000);
    }
  };

  if (selectedProduct) {
    return (
      <div>
        <h1>{selectedProduct.productName}</h1>
        <p>{selectedProduct.productDescription}</p>
        <p>Price: ${selectedProduct.productSalePrice.toFixed(2)}</p>

        <div className="deliveryType-container">
          <p>{getDeliveryTypeLabel(currentProduct.deliveryType)}</p>
        </div>

        <button onClick={handleBackToList}>Back to Catalog</button>
      </div>
    );
  }

  if (selectedProductForQuantity) {
    return (
      <div>
        <h3>Change Item Quantity</h3>
        <h2>{selectedProductForQuantity.productName}</h2>
        <form onSubmit={handleQuantitySubmit}>
          <label>
            Quantity:
            <input
              type="number"
              value={quantity}
              onChange={e => setQuantity(parseInt(e.target.value))}
              placeholder="Enter new quantity"
            />
          </label>
          <button type="submit">Update Quantity</button>
        </form>
        <button onClick={handleBackToList}>Back to Items</button>
      </div>
    );
  }

  const cartDisabled = isStaff || currentProduct.productQuantity === 0;
  const wishlistDisabled = isStaff;

  return (
    <div
      className={`card ${
        currentProduct.productQuantity === 0
          ? 'out-of-stock'
          : currentProduct.productQuantity < 10
            ? 'low-quantity'
            : ''
      }`}
      key={currentProduct.productId}
    >
      <ImageContainer imageId={currentProduct.imageId} />
      <span
        onClick={() =>
          handleProductClickForProductQuantity(currentProduct.productId)
        }
        className="product-title"
      ></span>

      <h2 onClick={handleProductTitleClick} className="product-title">
        {currentProduct.productName}
      </h2>

      <p>Price: ${currentProduct.productSalePrice.toFixed(2)}</p>

      <div className="deliveryType-container">
        <p>{getDeliveryTypeLabel(currentProduct.deliveryType)}</p>
      </div>

      <button
        onClick={cartDisabled ? undefined : handleAddToCart}
        disabled={cartDisabled}
        className={`add-to-cart-btn ${cartDisabled ? 'disabled' : ''}`}
        title={
          isStaff
            ? 'Shopping is disabled for staff/admin accounts.'
            : currentProduct.productQuantity === 0
              ? 'Out of stock'
              : 'Add to cart'
        }
      >
        {currentProduct.productQuantity === 0 ? 'Out of Stock' : 'Add to Cart'}
      </button>
      {successMessageCart && (
        <p className="success-message">{successMessageCart}</p>
      )}

      <button
        onClick={wishlistDisabled ? undefined : handleAddToWishlist}
        disabled={wishlistDisabled}
        className={`add-to-wishlist-btn ${wishlistDisabled ? 'disabled' : ''}`}
        title={
          isStaff
            ? 'Shopping is disabled for staff/admin accounts.'
            : 'Add to wishlist'
        }
      >
        Add to Wishlist
      </button>

      {successMessageWishlist && (
        <p className="success-message">{successMessageWishlist}</p>
      )}

      {currentProduct.productStatus === 'PRE_ORDER' && (
        <div
          style={{
            position: 'absolute',
            top: '10px',
            right: '10px',
            backgroundColor: '#FFD700',
            padding: '5px 10px',
            borderRadius: '5px',
            fontWeight: 'bold',
            color: '#333',
            zIndex: 1,
            boxShadow: '0 2px 4px rgba(0,0,0,0.2)',
          }}
        >
          PRE-ORDER
        </div>
      )}
    </div>
  );
}
