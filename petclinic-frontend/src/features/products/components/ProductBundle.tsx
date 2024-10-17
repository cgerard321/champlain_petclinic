import { useEffect, useState } from 'react';
import { ProductBundleModel } from '@/features/products/models/ProductModels/ProductBundleModel';
import { getProductByProductId } from '@/features/products/api/getProductByProductId';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import './ProductBundle.css';

/* eslint-disable react/prop-types */
interface ProductBundleProps {
  bundle: ProductBundleModel;
}

const ProductBundle: React.FC<ProductBundleProps> = ({ bundle }) => {
  const [products, setProducts] = useState<ProductModel[]>([]);
  useEffect(() => {
    const fetchProducts = async (): Promise<void> => {
      const productPromises = bundle.productIds.map(id =>
        getProductByProductId(id)
      );
      const productList = await Promise.all(productPromises);
      setProducts(productList);
    };
    fetchProducts();
  }, [bundle.productIds]);
  return (
    <div className="product-bundle-card">
      <h3>{bundle.bundleName}</h3>
      <p>{bundle.bundleDescription}</p>
      <div className="product-bundle-products">
        {products.map(product => (
          <div key={product.productId} className="product-bundle-item">
            <p>{product.productName}</p>
            <p>Price: ${product.productSalePrice.toFixed(2)}</p>
          </div>
        ))}
      </div>
      <p>Original Total Price: ${bundle.originalTotalPrice.toFixed(2)}</p>
      <p>Bundle Price: ${bundle.bundlePrice.toFixed(2)}</p>
    </div>
  );
};
export default ProductBundle;
