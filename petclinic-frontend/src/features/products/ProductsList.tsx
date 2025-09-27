import { useState, useEffect, JSX } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import './ProductList.css';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import Product from './components/Product';
import AddProduct from './components/AddProduct';
import { addProduct } from '@/features/products/api/addProduct';
import { useUser } from '@/context/UserContext';
import './components/Sidebar.css';
//import { getProductsByType } from '@/features/products/api/getProductsByType.ts';
import { addImage } from './api/addImage';
import { ImageModel } from './models/ProductModels/ImageModel';
import './components/StarRating.css';
import { getAllProductBundles } from './api/getAllProductBundles';
import { ProductBundleModel } from './models/ProductModels/ProductBundleModel';
import ProductBundle from './components/ProductBundle';

export default function ProductList({
  view,
  filters,
}: {
  view: 'catalog' | 'extras';
  filters?: {
    minPrice?: number;
    maxPrice?: number;
    ratingSort?: string;
    minStars?: number;
    maxStars?: number;
    deliveryType?: string;
    productType?: string;
  };
}): JSX.Element {
  const [productList, setProductList] = useState<ProductModel[]>([]);
  const [bundleList, setBundleList] = useState<ProductBundleModel[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const { user } = useUser();
  const [isRightRole, setIsRightRole] = useState<boolean>(false);
  const [recentlyClickedProducts, setRecentlyClickedProducts] = useState<
    ProductModel[]
  >([]);
  const fetchProducts = async (): Promise<void> => {
    setIsLoading(true);
    try {
      const list = await getAllProducts(
        filters?.minPrice,
        filters?.maxPrice,
        filters?.minStars,
        filters?.maxStars,
        filters?.ratingSort ?? 'default',
        filters?.deliveryType ?? '',
        filters?.productType ?? ''
      );

      const filteredList = list.filter(product => !product.isUnlisted);
      setProductList(filteredList);
    } catch (err) {
      console.error('Error fetching products:', err);
      setProductList([]);
    } finally {
      setIsLoading(false);
    }
    // Fetch product bundles
    try {
      const bundles = await getAllProductBundles();
      setBundleList(bundles);
    } catch (err) {
      console.error('Error fetching product bundles:', err);
    }
  };
  useEffect(() => {
    fetchProducts();
    const savedProducts = localStorage.getItem(
      `recentlyClickedProducts_${user.userId}`
    );
    if (savedProducts) {
      setRecentlyClickedProducts(JSON.parse(savedProducts));
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]);

  useEffect(() => {
    const hasRightRole =
      user?.roles !== undefined &&
      Array.from(user.roles).some(role => role.name === 'ADMIN');
    setIsRightRole(hasRightRole);
  }, [user]);

  const handleAddImage = async (formData: FormData): Promise<ImageModel> => {
    try {
      const createdImage = await addImage(formData);
      await fetchProducts();
      return createdImage;
    } catch (error) {
      console.error('Error adding image:', error);
      throw error;
    }
  };

  const handleAddProduct = async (
    product: ProductModel
  ): Promise<ProductModel> => {
    try {
      const savedProduct = await addProduct(product);
      await fetchProducts();
      return savedProduct;
    } catch (error) {
      throw error;
    }
  };

  const handleProductClick = (product: ProductModel): void => {
    setRecentlyClickedProducts(listOfProducts => {
      const updatedProducts = listOfProducts.filter(
        currentProduct => currentProduct.productId !== product.productId
      );

      updatedProducts.unshift(product);
      if (updatedProducts.length > 5) updatedProducts.pop();
      localStorage.setItem(
        `recentlyClickedProducts_${user.userId}`,
        JSON.stringify(updatedProducts)
      );
      return updatedProducts;
    });
  };

  const RecentlyViewedProducts = (): JSX.Element => (
    <div className="recently-viewed-container">
      <h2 className="section-header">Recently Seen</h2>
      <div className="recently-viewed-flex">
        {recentlyClickedProducts.length > 0 ? (
          recentlyClickedProducts
            .filter(product => !product.isUnlisted)
            .map(product => (
              <Product key={product.productId} product={product} />
            ))
        ) : (
          <p>No Recently Seen Items.</p>
        )}
      </div>
    </div>
  );

  return (
    <div className="product-list-container">
      {isRightRole && (
        <AddProduct addProduct={handleAddProduct} addImage={handleAddImage} />
      )}

      <div className="main-content">
        {view == 'catalog' && (
          <div className="list-container">
            <h2 className="section-header">Catalog</h2>
            <div className="grid">
              {isLoading ? (
                <p>Loading items...</p>
              ) : productList.length > 0 ? (
                productList.map(product => (
                  <div
                    key={product.productId}
                    onClick={() => handleProductClick(product)}
                  >
                    <Product product={product} />
                  </div>
                ))
              ) : (
                <p>No items found</p>
              )}
            </div>
          </div>
        )}

        {view == 'extras' && (
          <>
            <div className="list-container">
              <h2 className="section-header">Bundles</h2>
              <div className="grid product-bundles-grid">
                {bundleList.length > 0 ? (
                  bundleList.map((bundle: ProductBundleModel) => (
                    <ProductBundle key={bundle.bundleId} bundle={bundle} />
                  ))
                ) : (
                  <p>No Bundles Available</p>
                )}
              </div>
            </div>
            <div>
              <hr />
            </div>
            <RecentlyViewedProducts />
          </>
        )}
      </div>
    </div>
  );
}
