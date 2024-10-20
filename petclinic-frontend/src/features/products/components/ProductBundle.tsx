import { useEffect, useState } from 'react';
import { ProductBundleModel } from '@/features/products/models/ProductModels/ProductBundleModel';
import { getProductByProductId } from '@/features/products/api/getProductByProductId';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import './ProductBundle.css';
import ImageContainer from './ImageContainer';

interface ProductBundleProps {
  bundle: ProductBundleModel;
}

export default function ProductBundle({
  bundle,
}: ProductBundleProps): React.ReactElement {
  const [products, setProducts] = useState<(ProductModel | null)[]>([]);
  const [isBundleAvailable, setIsBundleAvailable] = useState(true);

  useEffect(() => {
    const fetchProducts = async (): Promise<void> => {
      const productPromises = bundle.productIds.map(async id => {
        try {
          return await getProductByProductId(id);
        } catch (error) {
          console.error(`Failed to fetch product with id ${id}:`, error);
          return null;
        }
      });
      const productList = await Promise.all(productPromises);
      setProducts(productList);
      setIsBundleAvailable(productList.every(product => product !== null));
    };
    fetchProducts();
  }, [bundle.productIds]);

  if (!isBundleAvailable) {
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
        {products.map(
          product =>
            product && (
              <div key={product.productId} className="product-bundle-item">
                <ImageContainer imageId={product.imageId} />
                <div className="product-details">
                  <p>{product.productName}</p>
                  <p>Price: ${product.productSalePrice.toFixed(2)}</p>
                </div>
              </div>
            )
        )}
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
    </div>
  );
}
