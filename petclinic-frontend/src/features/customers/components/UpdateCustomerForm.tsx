import * as React from 'react';
import { FormEvent, useEffect, useState } from 'react';
import { getOwner } from '../api/getOwner';
import { updateOwner } from '../api/updateOwner';
import { OwnerRequestModel } from '@/features/customers/models/OwnerRequestModel.ts';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel.ts';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { useUser } from '@/context/UserContext';
import './UpdateCustomerForm.css';

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
        const ownerData: OwnerResponseModel = response.data;
        setOwner({
          firstName: ownerData.firstName,
          lastName: ownerData.lastName,
          address: ownerData.address,
          city: ownerData.city,
          province: ownerData.province,
          telephone: ownerData.telephone,
        });
      } catch (error) {
        console.error('Error fetching owner data:', error);
      }
    };

    fetchOwnerData().catch(error =>
        console.error('Error in fetchOwnerData:', error)
    );
  }, [user.userId]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { name, value } = e.target;
    setOwner({ ...owner, [name]: value });
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

  const handleSubmit = async (
      event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate()) return;

    try {
      const response = await updateOwner(user.userId, owner);
      if (response.status === 200) {
        navigate(AppRoutePaths.Home);
      } else {
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
      <div className="update-customer-form">
        <h1>Edit Profile</h1>
        <form onSubmit={handleSubmit}>
          <label>First Name: </label>
          <input
              type="text"
              name="firstName"
              value={owner.firstName}
              onChange={handleChange}
          />
          {errors.firstName && <span className="error">{errors.firstName}</span>}
          <br />
          <label>Last Name: </label>
          <input
              type="text"
              name="lastName"
              value={owner.lastName}
              onChange={handleChange}
          />
          {errors.lastName && <span className="error">{errors.lastName}</span>}
          <br />
          <label>Address: </label>
          <input
              type="text"
              name="address"
              value={owner.address}
              onChange={handleChange}
          />
          {errors.address && <span className="error">{errors.address}</span>}
          <br />
          <label>City: </label>
          <input
              type="text"
              name="city"
              value={owner.city}
              onChange={handleChange}
          />
          {errors.city && <span className="error">{errors.city}</span>}
          <br />
          <label>Province: </label>
          <input
              type="text"
              name="province"
              value={owner.province}
              onChange={handleChange}
          />
          {errors.province && <span className="error">{errors.province}</span>}
          <br />
          <label>Telephone: </label>
          <input
              type="text"
              name="telephone"
              value={owner.telephone}
              onChange={handleChange}
          />
          {errors.telephone && <span className="error">{errors.telephone}</span>}
          <br />
          <button type="submit">Update</button>
        </form>
      </div>
  );
};

export default UpdateCustomerForm;