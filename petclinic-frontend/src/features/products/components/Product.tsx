import { JSX, useEffect, useState } from 'react';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import { getUserRating } from '../api/getUserRating';
import StarRating from './StarRating';
import { updateUserRating } from '../api/updateUserRating';
import { getProduct } from '../api/getProduct';
import { deleteUserRating } from '../api/deleteUserRating';
import { getProductByProductId } from '@/features/products/api/getProductByProductId.tsx';
import { changeProductQuantity } from '../api/changeProductQuantity';

export default function Product({
  product,
}: {
  product: ProductModel;
}): JSX.Element {
  const [currentUserRating, setUserRating] = useState<number>(0);
  const [currentProduct, setCurrentProduct] = useState<ProductModel>(product);
  const [selectedProduct, setSelectedProduct] = useState<ProductModel | null>(
    null
  );
  const [selectedProductForQuantity, setSelectedProductForQuantity] =
    useState<ProductModel | null>(null);
  const [quantity, setQuantity] = useState<number>(0);

  useEffect(() => {
    fetchRating();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

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
      setUserRating(0);
      const resRefresh = await getProduct(product.productId);
      setCurrentProduct(resRefresh);
    } catch (err) {
      console.error('Could not delete data', err);
    }
  };

  const updateRating = async (newRating: number): Promise<void> => {
    if (newRating == 0) return deleteRating();
    try {
      const resUpdate = await updateUserRating(product.productId, newRating);
      setUserRating(resUpdate);
      const resRefresh = await getProduct(product.productId);
      setCurrentProduct(resRefresh);
    } catch (err) {
      console.error('Could not update/fetch product ratings', err);
    }
  };

  const handleProductClick = async (productId: string): Promise<void> => {
    try {
      const product = await getProductByProductId(productId);
      setSelectedProduct(product);
    } catch (error) {
      console.error('Failed to fetch product details:', error);
    }
  };

  const handleProductClickForProductQuantity = async (
    productId: string
  ): Promise<void> => {
    try {
      const product = await getProductByProductId(productId);
      setSelectedProductForQuantity(product);
      setQuantity(product.productQuantity); // Set initial quantity to current product quantity
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

  if (selectedProduct) {
    return (
      <div>
        <h1>{selectedProduct.productName}</h1>
        <p>{selectedProduct.productDescription}</p>
        <p>Price: ${selectedProduct.productSalePrice.toFixed(2)}</p>
        <button onClick={handleBackToList}>Back to Products</button>
      </div>
    );
  }

  if (selectedProductForQuantity) {
    return (
      <div>
        <h1>Change Product Quantity</h1>
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
        <button onClick={handleBackToList}>Back to Products</button>
      </div>
    );
  }

  return (
    <div
      className={`card ${product.productQuantity < 10 ? 'low-quantity' : ''}`}
      key={product.productId}
    >
      <span
        onClick={() => handleProductClickForProductQuantity(product.productId)}
        style={{ cursor: 'pointer', color: 'blue', fontWeight: 'bold' }}
      >
        +
      </span>
      <h2
        onClick={() => handleProductClick(product.productId)}
        style={{
          cursor: 'pointer',
          color: 'blue',
          textDecoration: 'underline',
        }}
      >
        {currentProduct.productName}
      </h2>

      <p>{currentProduct.productDescription}</p>
      <p>Price: ${currentProduct.productSalePrice.toFixed(2)}</p>
      <p>Rating: {currentProduct.averageRating}</p>
      <p>Your Rating:</p>
      <StarRating
        currentRating={currentUserRating}
        updateRating={updateRating}
      />
    </div>
  );
}
