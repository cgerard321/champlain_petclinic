import { useEffect, useState, FC } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getOwner } from '../api/getOwner';
import { updateOwner } from '../api/updateOwner';
import { OwnerRequestModel } from '../models/OwnerRequestModel';
import { OwnerResponseModel } from '../models/OwnerResponseModel';
import './UpdateCustomerForm.css';

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
      if (!ownerId) {
        console.error('Owner id is undefined');
        return;
      }

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
      if (!ownerId) {
        console.error('Owner id is undefined');
        return;
      }
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
    <div className="update-customer-form">
      <h1>Edit Customer</h1>
      <form onSubmit={handleSubmit}>
        <label>First Name: </label>
        <input
          type="text"
          name="firstName"
          value={formData.firstName}
          onChange={handleChange}
        />
        {errors.firstName && <span className="error">{errors.firstName}</span>}
        <br />
        <label>Last Name: </label>
        <input
          type="text"
          name="lastName"
          value={formData.lastName}
          onChange={handleChange}
        />
        {errors.lastName && <span className="error">{errors.lastName}</span>}
        <br />
        <label>Address: </label>
        <input
          type="text"
          name="address"
          value={formData.address}
          onChange={handleChange}
        />
        {errors.address && <span className="error">{errors.address}</span>}
        <br />
        <label>City: </label>
        <input
          type="text"
          name="city"
          value={formData.city}
          onChange={handleChange}
        />
        {errors.city && <span className="error">{errors.city}</span>}
        <br />
        <label>Province: </label>
        <select
          name="province"
          value={formData.province}
          onChange={handleChange}
        >
          <option value="">Select Province</option>
          {provincesOfCanada.map(province => (
            <option key={province} value={province}>
              {province}
            </option>
          ))}
        </select>
        {errors.province && <span className="error">{errors.province}</span>}
        <br />
        <label>Telephone: </label>
        <input
          type="text"
          name="telephone"
          value={formData.telephone}
          onChange={handleChange}
        />
        {errors.telephone && <span className="error">{errors.telephone}</span>}
        <br />
        <button type="submit">Update</button>
      </form>

      <button id="back-button" onClick={handleBack}>
        Back
      </button>

      {isModalOpen && (
        <div className="admin-update-customer-modal-overlay">
          <div className="admin-update-customer-modal">
            <h2>Success!</h2>
            <p>Customer has been successfully updated.</p>
            <button onClick={closeModal}>Close</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminUpdateCustomerForm;
