import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel';
import { JSX, useEffect, useState } from 'react';
import { getUserRating } from '../api/getUserRating';
import StarRating from './StarRating';
import { updateUserRating } from '../api/updateUserRating';
import { getProduct } from '../api/getProduct';
import { deleteUserRating } from '../api/deleteUserRating';

function Product({ product }: { product: ProductModel }): JSX.Element {
  const [currentUserRating, setUserRating] = useState<number>(0);
  const [currentProduct, setCurrentProduct] = useState<ProductModel>(product);

  const fetchRating = async (): Promise<void> => {
    {
      try {
        const rating = await getUserRating(product.productId);
        setUserRating(rating);
      } catch (err) {
        console.error('Failed to fetch current rating', err);
      }
    }
  };

  const deleteRating = async (): Promise<void> => {
    try {
      await deleteUserRating(product.productId);
      setUserRating(0);
      getProduct(product.productId).then(resRefresh => {
        setCurrentProduct(resRefresh);
      });
    } catch (err) {
      console.error('Could not delete data', err);
    }
  };

  const updateRating = async (newRating: number): Promise<void> => {
    if (newRating == 0) return deleteRating();
    try {
      updateUserRating(product.productId, newRating).then(resUpdate => {
        setUserRating(resUpdate);
        getProduct(product.productId).then(resRefresh => {
          setCurrentProduct(resRefresh);
        });
      });
    } catch (err) {
      console.error('Could not update/fetch product ratings', err);
    }
  };

  useEffect(() => {
    fetchRating();
  }, []);

  return (
    <div className="card" key={product.productId}>
      <h2>{currentProduct.productName}</h2>
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

export default Product;
