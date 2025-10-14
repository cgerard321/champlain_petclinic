import { useState, ChangeEvent, FormEvent } from 'react';
import './cart-shared.css';
import './CartBillingForm.css';
export interface BillingInfo {
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
export interface CartBillingFormProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (billing: BillingInfo) => void;
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

const CartBillingForm: React.FC<CartBillingFormProps> = ({
  // eslint-disable-next-line react/prop-types
  isOpen,
  // eslint-disable-next-line react/prop-types
  onClose,
  // eslint-disable-next-line react/prop-types
  onSubmit,
}) => {
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

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showConfirm, setShowConfirm] = useState(false);

  if (!isOpen) return null;

  const handleChange = (e: ChangeEvent<HTMLInputElement>): void => {
    const { name, value } = e.target;
    setBilling(prev => ({ ...prev, [name]: value }));
  };

  const handleSelectChange = (e: ChangeEvent<HTMLSelectElement>): void => {
    const { name, value } = e.target;
    setBilling(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: FormEvent<HTMLFormElement>): void => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(null);

    const rawCardNumber = billing.cardNumber.replace(/\s+/g, '');
    if (!/^\d{16}$/.test(rawCardNumber)) {
      setError('Credit card number must be 16 digits.');
      setLoading(false);
      return;
    }
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(billing.expiry)) {
      setError('Expiration must be in MM/YY format.');
      setLoading(false);
      return;
    }
    if (!/^\d{3,4}$/.test(billing.cvv)) {
      setError('CVV must be 3 or 4 digits.');
      setLoading(false);
      return;
    }

    setLoading(false);
    setShowConfirm(true);
  };

  const handleConfirm = (): void => {
    setShowConfirm(false);
    setSuccess('Checkout successful! (mocked, no backend yet)');
    onSubmit(billing);
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
    onClose();
  };

  const handleCancel = (): void => {
    setShowConfirm(false);
  };

  return (
    <div className="cart-billing-modal-backdrop">
      <div className="cart-billing-modal-content cart-panel">
        <button
          className="cart-billing-modal-close"
          onClick={onClose}
          aria-label="Close billing form"
        >
          ✕
        </button>
        <h2>Billing Information</h2>

        {error && <div className="error">{error}</div>}
        {success && <div className="success">{success}</div>}

        <form onSubmit={handleSubmit} className="billing-form">
          <div className="main-fields">
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
            <div className="form-row">
              <div className="form-field">
                <input
                  type="text"
                  name="expiry"
                  placeholder="MM/YY"
                  value={billing.expiry}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="form-field">
                <input
                  type="text"
                  name="cvv"
                  placeholder="CVV"
                  value={billing.cvv}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>
          </div>

          <button
            type="submit"
            className="cart-button cart-button--brand cart-button--block cart-button--tall cart-button--disabled-muted"
            disabled={loading}
          >
            {loading ? 'Processing...' : 'Submit Payment'}
          </button>
        </form>
      </div>

      {showConfirm && (
        <div className="confirm-modal-backdrop">
          <div className="confirm-modal-content cart-panel cart-panel--padded">
            <h2>Confirm Checkout</h2>
            <p>Are you sure you want to checkout?</p>
            <div className="confirm-modal-buttons">
              <button
                className="cart-button cart-button--brand cart-button--tall"
                onClick={handleConfirm}
              >
                Yes
              </button>
              <button
                className="cart-button cart-button--danger cart-button--tall"
                onClick={handleCancel}
              >
                No
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CartBillingForm;
