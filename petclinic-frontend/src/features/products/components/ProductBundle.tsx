/* eslint-disable react/prop-types */
import { useTranslation } from 'react-i18next';
import {
  localizedProduct,
  formatProductPrice,
} from '@/features/products/utils/localizedProduct';
import { useEffect, useState } from 'react';
import { ProductBundleModel } from '@/features/products/models/ProductModels/ProductBundleModel';
import { getProduct } from '@/features/products/api/getProduct';
import { getProductByProductId } from '@/features/products/api/getProductByProductId';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import { useNavigate, generatePath } from 'react-router-dom';
import './ProductBundle.css';
import ImageContainer from './ImageContainer';
import { useUser } from '@/context/UserContext';
import { addProductToRecentlyViewed } from '@/features/products/utils/recentlyViewed';
import { AppRoutePaths } from '@/shared/models/path.routes';

interface ProductBundleProps {
  bundle: ProductBundleModel;
}

const ProductBundle: React.FC<ProductBundleProps> = ({ bundle }) => {
  const { t, i18n } = useTranslation('products');
  const language = i18n.resolvedLanguage || i18n.language || 'en';
  const bundleName = t(`bundleNames.${bundle.bundleName}`, {
    defaultValue: bundle.bundleName,
  });
  const bundleDescription = t(
    `bundleDescriptions.${bundle.bundleDescription}`,
    {
      defaultValue: bundle.bundleDescription,
    }
  );
  const [products, setProducts] = useState<ProductModel[]>([]);
  const [bundleStatus, setBundleStatus] = useState<
    'available' | 'unavailable' | 'hidden'
  >('available');

  const navigate = useNavigate();
  const { user, isAuthenticated } = useUser();
  useEffect(() => {
    const fetchProducts = async (): Promise<void> => {
      try {
        const productPromises = bundle.productIds.map(id =>
          isAuthenticated ? getProductByProductId(id) : getProduct(id)
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
  }, [bundle.productIds, isAuthenticated]);

  if (bundleStatus === 'hidden') {
    return null;
  }

  if (bundleStatus === 'unavailable') {
    return (
      <div className="product-bundle-card">
        <h3 className="bundle-title">{bundleName}</h3>
        <h1>{t('bundleUnavailable')}</h1>
        <p>{t('bundleMissing')}</p>
      </div>
    );
  }

  return (
    <div className="product-bundle-card">
      <div className="deal-stamp">{t('deal')}</div>
      <h3 className="bundle-title">{bundleName}</h3>
      <p>{bundleDescription}</p>
      <div className="product-bundle-products">
        {products.map(product => (
          <div
            key={product.productId}
            className="product-bundle-item"
            style={{ cursor: 'pointer' }}
            onClick={() => {
              addProductToRecentlyViewed(product, user?.userId); //
              navigate(
                generatePath(AppRoutePaths.ProductDetails, {
                  // uses project route mapping
                  productId: product.productId,
                })
              );
            }}
            tabIndex={0}
            role="button"
            onKeyDown={e => {
              if (e.key === 'Enter' || e.key === ' ') {
                addProductToRecentlyViewed(product, user?.userId); //mirror for keyboard users
                navigate(
                  generatePath(AppRoutePaths.ProductDetails, {
                    productId: product.productId,
                    //navigate(`/products/${product.productId}`);
                  })
                );
              }
            }}
          >
            <ImageContainer image={product.image} imageId={product.imageId} />
            <div className="product-details">
              <p>{localizedProduct(product, language).name}</p>
              <p>
                {t('price')}{' '}
                {formatProductPrice(product.productSalePrice, language)}
              </p>
            </div>
          </div>
        ))}
      </div>
      <p>
        {t('originalPrice')}{' '}
        <span className="original-price">
          {formatProductPrice(bundle.originalTotalPrice, language)}
        </span>
      </p>
      <p>
        {t('bundlePrice')}{' '}
        <span className="bundle-price">
          {formatProductPrice(bundle.bundlePrice, language)}
        </span>
      </p>
      {isAuthenticated && (
        <button
          className="add-bundle-to-cart-button"
          aria-label={t('bundleCart', { name: bundleName })}
        >
          {t('addBundle')}
        </button>
      )}
    </div>
  );
};

export default ProductBundle;
