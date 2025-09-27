import { useEffect, useState, FC } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getOwner } from '../api/getOwner';
import { updateOwner } from '../api/updateOwner';
import { OwnerRequestModel } from '../models/OwnerRequestModel';
import { OwnerResponseModel } from '../models/OwnerResponseModel';
import './customers.css';

const provincesOfCanada = [
  'Alberta',
  'British Columbia',
  'Manitoba',
  'New Brunswick',
  'Newfoundland and Labrador',
  'Nova Scotia',
  'Ontario',
  'Prince Edward Island',
  'Quebec',
  'Saskatchewan',
  'Northwest Territories',
  'Nunavut',
  'Yukon',
];

const AdminUpdateCustomerForm: FC = () => {
  const { ownerId } = useParams<{ ownerId: string }>();
  const navigate = useNavigate();
  const [formData, setFormData] = useState<OwnerRequestModel>({
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    province: '',
    telephone: '',
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    const fetchOwnerData = async (): Promise<void> => {
      if (!ownerId) return;
      try {
        const response = await getOwner(ownerId);
        const ownerData: OwnerResponseModel = response.data;
        setFormData(ownerData);
      } catch (error) {
        console.error('Error fetching owner data:', error);
      }
    };

    fetchOwnerData().catch(error =>
        console.error('Error in fetchOwnerData:', error)
    );
  }, [ownerId]);

  const handleChange = (
      e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};

    if (!formData.firstName) newErrors.firstName = 'First name is required';
    if (!formData.lastName) newErrors.lastName = 'Last name is required';
    if (!formData.address) newErrors.address = 'Address is required';
    if (!formData.city) newErrors.city = 'City is required';
    if (!formData.province) newErrors.province = 'Province is required';

    const telephoneRegex = /^[0-9]+$/;
    if (!formData.telephone) {
      newErrors.telephone = 'Telephone is required';
    } else if (!telephoneRegex.test(formData.telephone)) {
      newErrors.telephone = 'Telephone must contain only digits';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
      e: React.FormEvent<HTMLFormElement>
  ): Promise<void> => {
    e.preventDefault();
    if (!validate()) return;

    try {
      if (!ownerId) return;
      await updateOwner(ownerId, formData);
      setIsModalOpen(true);
    } catch (error) {
      console.error('Error updating owner:', error);
    }
  };

  const closeModal = (): void => {
    setIsModalOpen(false);
    navigate(`/customers/${ownerId}`);
  };

  const handleBack = (): void => {
    navigate(`/customers/${ownerId}`);
  };

  return (
      <div className="form-container">
        <h1>Edit Customer</h1>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>First Name</label>
            <input
                type="text"
                name="firstName"
                value={formData.firstName}
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
                value={formData.lastName}
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
                value={formData.address}
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
                value={formData.city}
                onChange={handleChange}
                className={errors.city ? 'error-input' : ''}
            />
            {errors.city && (
                <span className="error-message">{errors.city}</span>
            )}
          </div>

          <div className="form-group">
            <label>Province</label>
            <select
                name="province"
                value={formData.province}
                onChange={handleChange}
                className={errors.province ? 'error-input' : ''}
            >
              <option value="">Select Province</option>
              {provincesOfCanada.map(province => (
                  <option key={province} value={province}>
                    {province}
                  </option>
              ))}
            </select>
            {errors.province && (
                <span className="error-message">{errors.province}</span>
            )}
          </div>

          <div className="form-group">
            <label>Telephone</label>
            <input
                type="text"
                name="telephone"
                value={formData.telephone}
                onChange={handleChange}
                className={errors.telephone ? 'error-input' : ''}
            />
            {errors.telephone && (
                <span className="error-message">{errors.telephone}</span>
            )}
          </div>

          <div className="form-group" style={{ textAlign: 'center' }}>
            <button type="submit" className="button-base primary-button">
              Update
            </button>
            <button
                type="button"
                onClick={handleBack}
                className="button-base secondary-button mt-2"
            >
              Back
            </button>
          </div>
        </form>

        {isModalOpen && (
            <div className="modal-overlay">
              <div className="modal-content">
                <div className="modal-header">
                  <h2>Success!</h2>
                  <button className="modal-close" onClick={closeModal}>
                    &times;
                  </button>
                </div>
                <p>Customer has been successfully updated.</p>
                <button
                    onClick={closeModal}
                    className="button-base primary-button mt-4"
                >
                  Close
                </button>
              </div>
            </div>
        )}
      </div>
  );
};

export default AdminUpdateCustomerForm;
