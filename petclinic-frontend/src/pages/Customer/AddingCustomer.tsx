import * as React from 'react';
import { FormEvent, useState } from 'react';
import { addCustomer } from '@/features/customers/api/addCustomer.ts';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import '@/features/customers/components/UpdateCustomerForm.css';
import { CustomerModel } from '@/features/customers/models/CustomerModel.ts';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import './AddingCustomer.css';

const AddingCustomer: React.FC = (): JSX.Element => {
  const navigate = useNavigate();
  const [customer, setCustomer] = useState<CustomerModel>({
    customerId: '',
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
    setCustomer({ ...customer, [name]: value });
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!customer.firstName) newErrors.firstName = 'First name is required';
    if (!customer.lastName) newErrors.lastName = 'Last name is required';
    if (!customer.address) newErrors.address = 'Address is required';
    if (!customer.city) newErrors.city = 'City is required';
    if (!customer.province) newErrors.province = 'Province is required';
    if (!customer.telephone) newErrors.telephone = 'Telephone is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate()) return;

    try {
      const response = await addCustomer(customer);
      if (response.status === 201) {
        navigate(AppRoutePaths.Home);
      } else {
        console.error('Failed to add customer');
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div>
      <NavBar />
      <div className="add-customer-form">
        <h1>Add Customer</h1>
        <form onSubmit={handleSubmit}>
          <label>First Name: </label>
          <input
            type="text"
            name="firstName"
            value={customer.firstName}
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
            value={customer.lastName}
            onChange={handleChange}
          />
          {errors.lastName && <span className="error">{errors.lastName}</span>}
          <br />
          <label>Address: </label>
          <input
            type="text"
            name="address"
            value={customer.address}
            onChange={handleChange}
          />
          {errors.address && <span className="error">{errors.address}</span>}
          <br />
          <label>City: </label>
          <input
            type="text"
            name="city"
            value={customer.city}
            onChange={handleChange}
          />
          {errors.city && <span className="error">{errors.city}</span>}
          <br />
          <label>Province: </label>
          <input
            type="text"
            name="province"
            value={customer.province}
            onChange={handleChange}
          />
          {errors.province && <span className="error">{errors.province}</span>}
          <br />
          <label>Telephone: </label>
          <input
            type="text"
            name="telephone"
            value={customer.telephone}
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
