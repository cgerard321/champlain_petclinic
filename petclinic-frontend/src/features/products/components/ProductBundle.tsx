/* eslint-disable react/prop-types */
import { useEffect, useState } from 'react';
import { ProductBundleModel } from '@/features/products/models/ProductModels/ProductBundleModel';
import { getProductByProductId } from '@/features/products/api/getProductByProductId';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import { useNavigate } from 'react-router-dom';
import './ProductBundle.css';
import ImageContainer from './ImageContainer';

interface ProductBundleProps {
  bundle: ProductBundleModel;
}

const ProductBundle: React.FC<ProductBundleProps> = ({ bundle }) => {
  const [products, setProducts] = useState<ProductModel[]>([]);
  const [bundleStatus, setBundleStatus] = useState<
    'available' | 'unavailable' | 'hidden'
  >('available');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProducts = async (): Promise<void> => {
      try {
        const productPromises = bundle.productIds.map(id =>
          getProductByProductId(id)
        );
        const productList = await Promise.all(productPromises);

        setProducts(productList);

        if (productList.some(product => product.isUnlisted)) {
          setBundleStatus('hidden');
        } else if (productList.length !== bundle.productIds.length) {
          setBundleStatus('unavailable');
        } else {
          setBundleStatus('available');
        }
      } catch (error) {
        console.error('Failed to fetch products for bundle:', error);
        setBundleStatus('unavailable');
      }
    };

    fetchProducts();
  }, [bundle.productIds]);

  if (bundleStatus === 'hidden') {
    return null;
  }

  if (bundleStatus === 'unavailable') {
    return (
      <div className="product-bundle-card">
        <h3 className="bundle-title">{bundle.bundleName}</h3>
        <h1>Bundle Unavailable</h1>
        <p>Bundle temporarily unavailable due to missing Products</p>
      </div>
    );
  }

  return (
    <div className="product-bundle-card">
      <div className="deal-stamp">DEAL</div>
      <h3 className="bundle-title">{bundle.bundleName}</h3>
      <p>{bundle.bundleDescription}</p>
      <div className="product-bundle-products">
        {products.map(product => (
          <div
            key={product.productId}
            className="product-bundle-item"
            style={{ cursor: 'pointer' }}
            onClick={() => navigate(`/products/${product.productId}`)}
            tabIndex={0}
            role="button"
            onKeyDown={e => {
              if (e.key === 'Enter' || e.key === ' ') {
                navigate(`/products/${product.productId}`);
              }
            }}
          >
            <ImageContainer imageId={product.imageId} />
            <div className="product-details">
              <p>{product.productName}</p>
              <p>Price: ${product.productSalePrice.toFixed(2)}</p>
            </div>
          </div>
        ))}
      </div>
      <p>
        Original Total Price:{' '}
        <span className="original-price">
          ${bundle.originalTotalPrice.toFixed(2)}
        </span>
      </p>
      <p>
        Bundle Price:{' '}
        <span className="bundle-price">${bundle.bundlePrice.toFixed(2)}</span>
      </p>
      <button
        className="add-bundle-to-cart-button"
        aria-label={`Add ${bundle.bundleName} to Cart`}
      >
        Add Bundle to Cart
      </button>
    </div>
  );
};

export default ProductBundle;
