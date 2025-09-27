import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
import { getOwner } from '../api/getOwner';
import { updateOwner } from '../api/updateOwner';
import { OwnerRequestModel } from '@/features/customers/models/OwnerRequestModel';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { useUser } from '@/context/UserContext';
import './customers.css';

const UpdateCustomerForm: React.FC = (): JSX.Element => {
  const navigate = useNavigate();
  const { user } = useUser();

  const [owner, setOwner] = useState<OwnerRequestModel>({
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    province: '',
    telephone: '',
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});

  useEffect(() => {
    const fetchOwnerData = async (): Promise<void> => {
      try {
        const response = await getOwner(user.userId);
        const data: OwnerResponseModel = response.data;
        setOwner({
          firstName: data.firstName,
          lastName: data.lastName,
          address: data.address,
          city: data.city,
          province: data.province,
          telephone: data.telephone,
        });
      } catch (err) {
        console.error('Error fetching owner data:', err);
      }
    };
    fetchOwnerData().catch(err =>
      console.error('Error in fetchOwnerData:', err)
    );
  }, [user.userId]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, value } = e.target;
    setOwner(prev => ({ ...prev, [name]: value }));
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!owner.firstName) newErrors.firstName = 'First name is required';
    if (!owner.lastName) newErrors.lastName = 'Last name is required';
    if (!owner.address) newErrors.address = 'Address is required';
    if (!owner.city) newErrors.city = 'City is required';
    if (!owner.province) newErrors.province = 'Province is required';
    if (!owner.telephone) newErrors.telephone = 'Telephone is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>): Promise<void> => {
    e.preventDefault();
    if (!validate()) return;
    try {
      const response = await updateOwner(user.userId, owner);
      if (response.status === 200) {
        navigate(AppRoutePaths.Home);
      }
    } catch (err) {
      console.error('Error updating owner:', err);
    }
  };

  return (
    <div className="update-customer-form-container">
      <div className="update-customer-form">
        <h1>Edit Profile</h1>
        <form onSubmit={handleSubmit}>
          <div className="update-customer-fields">
            <div className="form-group">
              <label>First Name</label>
              <input
                type="text"
                name="firstName"
                value={owner.firstName}
                onChange={handleChange}
                className={errors.firstName ? 'error-input' : ''}
              />
              {errors.firstName && (
                <span className="error-message">{errors.firstName}</span>
              )}
            </div>

            <div className="form-group">
              <label>Last Name</label>
              <input
                type="text"
                name="lastName"
                value={owner.lastName}
                onChange={handleChange}
                className={errors.lastName ? 'error-input' : ''}
              />
              {errors.lastName && (
                <span className="error-message">{errors.lastName}</span>
              )}
            </div>

            <div className="form-group">
              <label>Address</label>
              <input
                type="text"
                name="address"
                value={owner.address}
                onChange={handleChange}
                className={errors.address ? 'error-input' : ''}
              />
              {errors.address && (
                <span className="error-message">{errors.address}</span>
              )}
            </div>

            <div className="form-group">
              <label>City</label>
              <input
                type="text"
                name="city"
                value={owner.city}
                onChange={handleChange}
                className={errors.city ? 'error-input' : ''}
              />
              {errors.city && (
                <span className="error-message">{errors.city}</span>
              )}
            </div>

            <div className="form-group">
              <label>Province</label>
              <input
                type="text"
                name="province"
                value={owner.province}
                onChange={handleChange}
                className={errors.province ? 'error-input' : ''}
              />
              {errors.province && (
                <span className="error-message">{errors.province}</span>
              )}
            </div>

            <div className="form-group">
              <label>Telephone</label>
              <input
                type="text"
                name="telephone"
                value={owner.telephone}
                onChange={handleChange}
                className={errors.telephone ? 'error-input' : ''}
              />
              {errors.telephone && (
                <span className="error-message">{errors.telephone}</span>
              )}
            </div>
          </div>

          <div className="update-customer-actions">
            <button type="submit" className="primary-button">
              Update Profile
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default UpdateCustomerForm;
