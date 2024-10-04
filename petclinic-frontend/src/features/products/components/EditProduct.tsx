import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import { useState, useEffect, JSX } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useUser } from '@/context/UserContext';
import { updateProduct } from '../api/updateProduct';

export default function EditProduct(): JSX.Element {
  const { user } = useUser();
  const navigate = useNavigate();
  const location = useLocation();
  const { product } = location.state as { product: ProductModel };

  const [productData, setProductData] = useState<ProductModel>(product);

  useEffect(() => {
    const isAdmin = user?.roles?.some(role => role.name === 'ADMIN');
    if (!isAdmin) {
      navigate('/unauthorized');
    }
  }, [user, navigate]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setProductData({ ...productData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    try {
      await updateProduct(productData.productId, productData);
      navigate(`/products/${productData.productId}`, {
        state: { product: productData },
      });
    } catch (error) {
      console.error('Error updating product:', error);
      alert('Failed to update the product. Please try again.');
    }
  };

  return (
    <div>
      <h2>Edit Product</h2>
      <form onSubmit={handleSubmit}>
        <label>
          Product Name:
          <input
            name="productName"
            value={productData.productName}
            onChange={handleInputChange}
            required
          />
        </label>
        <label>
          Product Description:
          <input
            name="productDescription"
            value={productData.productDescription}
            onChange={handleInputChange}
            required
          />
        </label>
        <label>
          Product Sale Price:
          <input
            name="productSalePrice"
            type="number"
            step="0.01"
            value={productData.productSalePrice}
            onChange={handleInputChange}
            required
          />
        </label>
        <label>
          Product Type:
          <input
            name="productType"
            value={productData.productType}
            onChange={handleInputChange}
            required
          />
        </label>
        <button type="submit">Update Product</button>
      </form>
    </div>
  );
}
