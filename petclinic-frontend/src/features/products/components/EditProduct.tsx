import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import { useState, JSX } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { updateProduct } from '../api/updateProduct';
import './EditProduct.css';

export default function EditProduct(): JSX.Element {
  const navigate = useNavigate();
  const location = useLocation();
  const { product } = location.state as { product: ProductModel };

  const [productData, setProductData] = useState<ProductModel>(product);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    setProductData({ ...productData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    try {
      await updateProduct(productData.productId, productData);
      navigate(`/products`, {
        state: { product: productData },
      });
      alert('Product updated successfully!');
    } catch (error) {
      console.error('Error updating product:', error);
      alert('Failed to update the product. Please try again.');
    }
  };

  return (
    <div className="edit-product-container">
      <h2 className="form-title">Edit Product</h2>
      <form className="edit-product-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="productName" className="form-label">
            Product Name:
          </label>
          <input
            id="productName"
            name="productName"
            className="form-input"
            value={productData.productName}
            onChange={handleInputChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="productDescription" className="form-label">
            Product Description:
          </label>
          <input
            id="productDescription"
            name="productDescription"
            className="form-input"
            value={productData.productDescription}
            onChange={handleInputChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="productSalePrice" className="form-label">
            Product Sale Price:
          </label>
          <input
            id="productSalePrice"
            name="productSalePrice"
            type="number"
            step="0.01"
            className="form-input"
            value={productData.productSalePrice}
            onChange={handleInputChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="productType" className="form-label">
            Product Type:
          </label>
          <input
            id="productType"
            name="productType"
            className="form-input"
            value={productData.productType}
            onChange={handleInputChange}
            required
          />
        </div>

        <button type="submit" className="submit-button">
          Update Product
        </button>
      </form>
    </div>
  );
}
