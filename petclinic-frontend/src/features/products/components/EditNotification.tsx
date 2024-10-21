import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import { useState, JSX, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import './EditProduct.css';
import { updateNotification } from '../api/updateNotification';
import { getNotification } from '../api/getNotification';

export default function EditNotification(): JSX.Element {
  const navigate = useNavigate();
  const location = useLocation();
  const { product } = location.state as { product: ProductModel };

  const notifTypes = ['PRICE', 'QUANTITY'];

  const [email, setEmail] = useState<string>('');
  const [selectedTypes, setTypes] = useState<string[]>([]);

  const handleCheckboxChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ): void => {
    const { value, checked } = event.target;
    if (checked) {
      setTypes(prev => [...prev, value]);
    } else {
      setTypes(prev => prev.filter(type => type !== value));
    }
  };

  const fetchNotif = async (): Promise<void> => {
    try {
      const notif = await getNotification(product.productId);
      setEmail(notif.email);
      setTypes(notif.notificationType);
    } catch (err) {
      console.error('Failed to fetch product ratings', err);
    }
  };

  const handleSubmit = async (e: React.FormEvent): Promise<void> => {
    e.preventDefault();
    try {
      await updateNotification(product.productId, email, selectedTypes);
      navigate(`/products/${product.productId}`);
      alert('Product notification updated successfully!');
    } catch (error) {
      console.error('Error updating notification:', error);
      alert(
        'Failed to update the notification subscription. Please try again.'
      );
    }
  };

  useEffect(() => {
    fetchNotif();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div className="edit-product-container">
      <h2 className="form-title">Edit Product</h2>
      <form className="edit-product-form" onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="emailBox" className="form-label">
            E-Mail:
          </label>
          <input
            id="emailBox"
            name="emailBox"
            className="form-input"
            value={email}
            onChange={event => {
              setEmail(event.target.value);
            }}
            required
          />
        </div>

        <div className="form-group">
          <label className="form-label">Notification Type:</label>
          {notifTypes.map(type => (
            <div key={type}>
              <input
                type="checkbox"
                id={type}
                value={type}
                checked={selectedTypes.includes(type)}
                onChange={handleCheckboxChange}
              />
              <label htmlFor={type} className="form-label">
                {type}
              </label>
            </div>
          ))}
        </div>

        <button type="submit" className="submit-button">
          Update Product
        </button>
      </form>
    </div>
  );
}
