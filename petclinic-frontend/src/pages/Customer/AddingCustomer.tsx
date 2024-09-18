import * as React from 'react';
import { FormEvent, useState } from 'react';
import { addOwner } from '@/features/customers/api/addOwner.ts';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import './ProfileEdit';
import { OwnerModel } from '@/features/customers/models/OwnerModel.ts';
import { NavBar } from '@/layouts/AppNavBar.tsx';

const AddingCustomer: React.FC = (): JSX.Element => {
  const navigate = useNavigate();
  const [owner, setOwner] = useState<OwnerModel>({
    ownerId: '',
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    province: '',
    telephone: '',
    pets: [],
  });
  const [errors, setErrors] = useState<{ [key: string]: string }>({});

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
      const response = await addOwner(owner);
      if (response.status === 201) {
        navigate(AppRoutePaths.Home);
      } else {
        console.error('Failed to add owner');
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div>
      <NavBar />
      <div className="profile-edit">
        <h1>Add Customer</h1>
        <form onSubmit={handleSubmit}>
          <label>First Name: </label>
          <input
            type="text"
            name="firstName"
            value={owner.firstName}
            onChange={handleChange}
          />
          {errors.firstName && (
            <span className="error">{errors.firstName}</span>
          )}
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
          {errors.telephone && (
            <span className="error">{errors.telephone}</span>
          )}
          <br />
          <button type="submit">Add</button>
        </form>
      </div>
    </div>
  );
};

export default AddingCustomer;
