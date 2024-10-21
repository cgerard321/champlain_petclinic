/* eslint-disable @typescript-eslint/explicit-function-return-type */

import { useState } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { useNavigate } from 'react-router-dom';
import './FormPromo.css';
import { PromoApi } from '@/features/promos/api/PromoApi.tsx';
import { PromoCodeRequestModel } from '@/features/promos/models/PromoCodeRequestModel.tsx';

export default function AddPromo(): JSX.Element {
  const [name, setName] = useState<string>('');
  const [code, setCode] = useState<string>('');
  const [discount, setDiscount] = useState<string>(''); // Almacenamos como string para permitir entrada libre
  const [expirationDate, setExpirationDate] = useState<Date | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleAddPromo = async () => {
    if (!name || !code || !discount || !expirationDate) {
      setError('All fields are required');
      setSuccessMessage(null);
      return;
    }

    const parsedDiscount = parseFloat(discount);
    if (isNaN(parsedDiscount) || parsedDiscount <= 0) {
      setError('Discount must be a valid number greater than 0');
      setSuccessMessage(null);
      return;
    }

    const formattedExpirationDate = new Date(expirationDate);
    formattedExpirationDate.setHours(23, 59, 59, 999);
    const formattedExpirationDateString = formattedExpirationDate
      .toISOString()
      .replace('.999Z', '');

    const newPromo: PromoCodeRequestModel = {
      name,
      code,
      discount: parsedDiscount,
      expirationDate: formattedExpirationDateString,
    };

    try {
      await PromoApi.addPromo(newPromo); // Usar el método de la clase API
      setSuccessMessage('Promo added successfully!');
      setError(null);

      setName('');
      setCode('');
      setDiscount('');
      setExpirationDate(null);
    } catch (err) {
      console.error('Error adding promo:', err);
      setError('Failed to add promo. Please try again.');
      setSuccessMessage(null);
    }
  };

  const handleCancel = () => {
    navigate('/promos');
  };

  return (
    <div className="promo-form">
      <h3>Add New Promo</h3>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      {successMessage && <p style={{ color: 'green' }}>{successMessage}</p>}

      <div className="form-group">
        <label htmlFor="promo-name">Name:</label>
        <input
          type="text"
          id="promo-name"
          value={name}
          onChange={e => setName(e.target.value)}
          placeholder="Promo Name"
        />
      </div>

      <div className="form-group">
        <label htmlFor="promo-code">Code:</label>
        <input
          type="text"
          id="promo-code"
          value={code}
          onChange={e => setCode(e.target.value)}
          placeholder="Promo Code"
        />
      </div>

      <div className="form-group">
        <label htmlFor="promo-discount">Discount (%):</label>
        <input
          type="text"
          id="promo-discount"
          value={discount}
          onChange={e => setDiscount(e.target.value)} // Se almacena como string
          placeholder="Discount Percentage"
        />
      </div>

      <div className="form-group">
        <label htmlFor="expiration-date">Expiration Date:</label>
        <DatePicker
          selected={expirationDate}
          onChange={(date: Date | null) => setExpirationDate(date)}
          dateFormat="yyyy-MM-dd"
          placeholderText="Select Expiration Date"
        />
      </div>

      <div className="form-actions">
        <button className="add-button" onClick={handleAddPromo}>
          Add Promo
        </button>
        <button className="cancel-button" onClick={handleCancel}>
          Cancel
        </button>
      </div>
    </div>
  );
}
