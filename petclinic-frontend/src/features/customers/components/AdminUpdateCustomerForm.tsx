import { useEffect, useState, FC } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getOwner, updateOwner } from '@/features/customers/api/updateOwner.ts';
import { OwnerRequestModel } from '../models/OwnerRequestModel';
import { OwnerResponseModel } from '../models/OwnerResponseModel';
import './UpdateCustomerForm.css';

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

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
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
    if (!formData.telephone) newErrors.telephone = 'Telephone is required';
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
      navigate(`/customers/${ownerId}`);
    } catch (error) {
      console.error('Error updating owner:', error);
    }
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
        <input
          type="text"
          name="province"
          value={formData.province}
          onChange={handleChange}
        />
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
    </div>
  );
};

export default AdminUpdateCustomerForm;
