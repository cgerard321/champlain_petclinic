import React, { FormEvent, useState, useEffect } from 'react';
import { updateOwner, getOwner } from '@/features/customers/api/updateOwner.ts';
import { OwnerRequestModel } from '@/features/customers/models/OwnerRequestModel.ts';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel.ts';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { useUser } from '@/context/UserContext';
import './ProfileEdit.css';

export default function ProfileEdit(): JSX.Element {
  const navigate = useNavigate();
  const { user } = useUser();
  const [owner, setOwner] = useState<OwnerRequestModel>({
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    province: '',
    telephone: ''
  });
  const [errors, setErrors] = useState<{ [key: string]: string }>({});

  useEffect(() => {
    const fetchOwnerData = async () => {
      try {
        console.log('Fetching owner data for userId:', user.userId);
        const response = await getOwner(user.userId);
        console.log('Owner data fetched:', response.data);
        const ownerData: OwnerResponseModel = response.data;
        setOwner({
          firstName: ownerData.firstName,
          lastName: ownerData.lastName,
          address: ownerData.address,
          city: ownerData.city,
          province: ownerData.province,
          telephone: ownerData.telephone
        });
        console.log('Owner state updated:', owner);
      } catch (error) {
        console.error('Error fetching owner data:', error);
      }
    };

    fetchOwnerData().catch(error => console.error('Error in fetchOwnerData:', error));
  }, [user.userId]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setOwner({ ...owner, [name]: value });
    console.log('Owner state updated on change:', owner);
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!owner.firstName) newErrors.firstName = 'Your first name is required';
    if (!owner.lastName) newErrors.lastName = 'Your last name is required';
    if (!owner.address) newErrors.address = 'Your address is required';
    if (!owner.city) newErrors.city = 'Your city is required';
    if (!owner.province) newErrors.province = 'Your province is required';
    if (!owner.telephone) newErrors.telephone = 'Your telephone number is required';
    setErrors(newErrors);
    console.log('Validation errors:', newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    event.preventDefault();
    if (!validate()) return;

    try {
      console.log('Submitting owner data:', owner);
      const response = await updateOwner(user.userId, owner);
      if (response.status === 200) {
        console.log('Profile updated successfully');
        navigate(AppRoutePaths.Default);
      } else {
        console.error('Failed to update profile');
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div className="profile-edit">
      <h1>Edit Profile Information</h1>
      <form onSubmit={handleSubmit}>
        <label>First Name: </label>
        <input type="text" name="firstName" value={owner.firstName} onChange={handleChange} />
        {errors.firstName && <span className="error">{errors.firstName}</span>}
        <br />
        <label>Last Name: </label>
        <input type="text" name="lastName" value={owner.lastName} onChange={handleChange} />
        {errors.lastName && <span className="error">{errors.lastName}</span>}
        <br />
        <label>Address: </label>
        <input type="text" name="address" value={owner.address} onChange={handleChange} />
        {errors.address && <span className="error">{errors.address}</span>}
        <br />
        <label>City: </label>
        <input type="text" name="city" value={owner.city} onChange={handleChange} />
        {errors.city && <span className="error">{errors.city}</span>}
        <br />
        <label>Province: </label>
        <input type="text" name="province" value={owner.province} onChange={handleChange} />
        {errors.province && <span className="error">{errors.province}</span>}
        <br />
        <label>Telephone: </label>
        <input type="text" name="telephone" value={owner.telephone} onChange={handleChange} />
        {errors.telephone && <span className="error">{errors.telephone}</span>}
        <br />
        <button type="submit">Update</button>
      </form>
    </div>
  );
}
