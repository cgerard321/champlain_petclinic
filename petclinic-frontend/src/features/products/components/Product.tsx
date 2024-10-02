import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import { JSX, useEffect, useState } from 'react';
import { getUserRating } from '../api/getUserRating';
import StarRating from './StarRating';
import { updateUserRating } from '../api/updateUserRating';
import { getProduct } from '../api/getProduct';
import { deleteUserRating } from '../api/deleteUserRating';
// import { getProductByProductId } from '@/features/products/api/getProductByProductId.tsx';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';

function Product({ product }: { product: ProductModel }): JSX.Element {
  const [currentUserRating, setUserRating] = useState<number>(0);
  const [currentProduct, setCurrentProduct] = useState<ProductModel>(product);
  const [selectedProduct, setSelectedProduct] = useState<ProductModel | null>(
    null
  );

  const navigate = useNavigate(); // Get the navigate function
  const handleButtonClick = (): void => {
    // Navigate to ProductDetails and pass the product data
    navigate(AppRoutePaths.ProductDetails, { state: { product } });
  };

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

  // const handleProductClick = async (productId: string): Promise<void> => {
  //   try {
  //     const product = await getProductByProductId(productId);
  //     setSelectedProduct(product);
  //   } catch (error) {
  //     console.error('Failed to fetch product details:', error);
  //   }
  // };

  const handleBackToList = (): void => {
    setSelectedProduct(null);
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

  return (
    <div className="card" key={product.productId}>
      <h2
        onClick={handleButtonClick}
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

export default Product;
