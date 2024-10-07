/* eslint-disable @typescript-eslint/explicit-function-return-type */
import { useEffect, useState } from 'react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { useNavigate, useParams } from 'react-router-dom';
import './FormPromo.css';

interface PromoCodeRequestModel {
  name: string;
  code: string;
  discount: number;
  expirationDate: string;
}

export default function UpdatePromo(): JSX.Element {
  const [name, setName] = useState<string>('');
  const [code, setCode] = useState<string>('');
  const [discount, setDiscount] = useState<string>(''); // Almacenamos como string para permitir entrada libre
  const [expirationDate, setExpirationDate] = useState<Date | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const { promoId } = useParams<{ promoId: string }>();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchPromo = async () => {
      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/promos/${promoId}`,
          {
            method: 'GET',
            headers: {
              Accept: 'application/json',
            },
            credentials: 'include',
          }
        );
        if (!response.ok) {
          throw new Error(`Error: ${response.status} ${response.statusText}`);
        }

        const promo = await response.json();
        setName(promo.name);
        setCode(promo.code);
        setDiscount(promo.discount.toString());
        const promoExpirationDate = new Date(
          promo.expirationDate.split('T')[0]
        );
        setExpirationDate(promoExpirationDate);
      } catch (err) {
        setError('Failed to fetch promo data.');
        console.error(err);
      }
    };

    fetchPromo();
  }, [promoId]);

  const handleUpdatePromo = async () => {
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

    const updatedPromo: PromoCodeRequestModel = {
      name,
      code,
      discount: parsedDiscount,
      expirationDate: formattedExpirationDateString,
    };

    try {
      const response = await fetch(
        `http://localhost:8080/api/v2/gateway/promos/${promoId}`,
        {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            Accept: 'application/json',
          },
          body: JSON.stringify(updatedPromo),
          credentials: 'include',
        }
      );

      if (!response.ok) {
        throw new Error(
          `Failed to update promo: ${response.status} ${response.statusText}`
        );
      }

      setSuccessMessage('Promo updated successfully!');
      setError(null);
    } catch (err) {
      console.error('Error updating promo:', err);
      setError('Failed to update promo. Please try again.');
      setSuccessMessage(null);
    }
  };

  const handleCancel = () => {
    navigate('/promos');
  };

  return (
    <div className="promo-form">
      <h3>Update Promo</h3>
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
          onChange={e => setDiscount(e.target.value)}
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
        <button className="update-button" onClick={handleUpdatePromo}>
          Update Promo
        </button>
        <button className="cancel-button" onClick={handleCancel}>
          Cancel
        </button>
      </div>
    </div>
  );
}
