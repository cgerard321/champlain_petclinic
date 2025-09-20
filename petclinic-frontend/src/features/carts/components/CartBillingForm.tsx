import { useState } from 'react';
import './CartBillingForm.css';

export interface CartBillingFormProps {
  onSubmit: () => void;
}

interface BillingInfo {
  fullName: string;
  email: string;
  phoneNumber: string;
  address: string;
  city: string;
  province: string;
  postalCode: string;
  cardNumber: string;
  expiry: string;
  cvv: string;
}

const provinces = [
  'AB',
  'BC',
  'MB',
  'NB',
  'NL',
  'NS',
  'ON',
  'PE',
  'QC',
  'SK',
  'NT',
  'NU',
  'YT',
];
// eslint-disable-next-line react/prop-types
const CartBillingForm: React.FC<CartBillingFormProps> = ({ onSubmit }) => {
  const [billing, setBilling] = useState<BillingInfo>({
    fullName: '',
    email: '',
    phoneNumber: '',
    address: '',
    city: '',
    province: '',
    postalCode: '',
    cardNumber: '',
    expiry: '',
    cvv: '',
  });

  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  //for Inputs
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { name, value } = e.target;
    setBilling(prev => ({ ...prev, [name]: value }));
  };
  //For Select
  const handleSelectChange = (
    e: React.ChangeEvent<HTMLSelectElement>
  ): void => {
    const { name, value } = e.target;
    setBilling(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: React.FormEvent): void => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);

    // Credit Card Number Validation
    if (!/^\d{16}$/.test(billing.cardNumber.replace(/\s+/g, ''))) {
      setError('Credit card number must be 16 digits.');
      setLoading(false);
      return;
    }
    // Credit Card Expiration Date Validation
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(billing.expiry)) {
      setError('Expiration must be in MM/YY format.');
      setLoading(false);
      return;
    }
    // CVV Validation
    if (!/^\d{3,4}$/.test(billing.cvv)) {
      setError('CVV must be 3 or 4 digits.');
      setLoading(false);
      return;
    }

    //simulate a checkout success after 1s
    setTimeout(() => {
      setSuccess(
        'Checkout successful! Your order is being processed (mocked, no backend yet)'
      );
      setBilling({
        fullName: '',
        email: '',
        phoneNumber: '',
        address: '',
        city: '',
        province: '',
        postalCode: '',
        cardNumber: '',
        expiry: '',
        cvv: '',
      });
      setLoading(false);
      onSubmit();
    }, 1000);
  };

  return (
    <div className="billing-form-container">
      <h2>Billing Information</h2>
      {error && <div className="error">{error}</div>}
      {success && <div className="success">{success}</div>}

      <form onSubmit={handleSubmit} className="billing-form">
        <input
          type="text"
          name="fullName"
          placeholder="Full Name"
          value={billing.fullName}
          onChange={handleChange}
          required
        />
        <input
          type="email"
          name="email"
          placeholder="Email"
          value={billing.email}
          onChange={handleChange}
          required
        />
        <input
          type="text"
          name="address"
          placeholder="Address"
          value={billing.address}
          onChange={handleChange}
          required
        />
        <input
          type="text"
          name="city"
          placeholder="City"
          value={billing.city}
          onChange={handleChange}
          required
        />
        <select
          name="province"
          value={billing.province}
          onChange={handleSelectChange}
          required
        >
          <option value="">Select Province</option>
          {provinces.map(prov => (
            <option key={prov} value={prov}>
              {prov}
            </option>
          ))}
        </select>

        <input
          type="text"
          name="postalCode"
          placeholder="Postal Code"
          value={billing.postalCode}
          onChange={handleChange}
          required
        />
        <input
          type="text"
          name="cardNumber"
          placeholder="Card Number"
          value={billing.cardNumber}
          onChange={handleChange}
          required
        />
        <input
          type="text"
          name="expiry"
          placeholder="MM/YY"
          value={billing.expiry}
          onChange={handleChange}
          required
        />
        <input
          type="text"
          name="cvv"
          placeholder="CVV"
          value={billing.cvv}
          onChange={handleChange}
          required
        />

        <button type="submit" disabled={loading}>
          {loading ? 'Processing...' : 'Submit Payment'}
        </button>
      </form>
    </div>
  );
};

export default CartBillingForm;
