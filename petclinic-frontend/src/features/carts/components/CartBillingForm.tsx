import { useState, useEffect, ChangeEvent, FormEvent } from 'react';
import { useUser } from '@/context/UserContext';
import { getCustomer } from '@/features/customers/api/getCustomer';
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
  { code: 'AB', name: 'Alberta' },
  { code: 'BC', name: 'British Columbia' },
  { code: 'MB', name: 'Manitoba' },
  { code: 'NB', name: 'New Brunswick' },
  { code: 'NL', name: 'Newfoundland and Labrador' },
  { code: 'NS', name: 'Nova Scotia' },
  { code: 'ON', name: 'Ontario' },
  { code: 'PE', name: 'Prince Edward Island' },
  { code: 'QC', name: 'Quebec' },
  { code: 'SK', name: 'Saskatchewan' },
  { code: 'NT', name: 'Northwest Territories' },
  { code: 'NU', name: 'Nunavut' },
  { code: 'YT', name: 'Yukon' },
];

const getProvinceCode = (province?: string): string => {
  const normalized = province?.trim().toLowerCase();

  if (!normalized) return '';

  return (
    provinces.find(
      option =>
        option.code.toLowerCase() === normalized ||
        option.name.toLowerCase() === normalized
    )?.code ?? ''
  );
};

const CartBillingForm: React.FC<CartBillingFormProps> = ({
  // eslint-disable-next-line react/prop-types
  isOpen,
  // eslint-disable-next-line react/prop-types
  onClose,
  // eslint-disable-next-line react/prop-types
  onSubmit,
}) => {
  const { user } = useUser();
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

  useEffect(() => {
    if (!isOpen) return;

    setBilling(previous => ({
      ...previous,
      email: previous.email || user.email || '',
    }));

    if (!user.userId) return;

    let ignoreResponse = false;

    const loadCustomerInformation = async (): Promise<void> => {
      try {
        const response = await getCustomer(user.userId);

        if (ignoreResponse) return;

        const customer = response.data;
        const fullName = [customer.firstName, customer.lastName]
          .filter(Boolean)
          .join(' ');

        setBilling(previous => ({
          ...previous,
          fullName: previous.fullName || fullName,
          phoneNumber: previous.phoneNumber || customer.telephone || '',
          address: previous.address || customer.address || '',
          city: previous.city || customer.city || '',
          province: previous.province || getProvinceCode(customer.province),
        }));
      } catch (error) {
        console.error('Could not pre-fill checkout information:', error);
      }
    };

    void loadCustomerInformation();

    return () => {
      ignoreResponse = true;
    };
  }, [isOpen, user.email, user.userId]);

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
              type="tel"
              name="phoneNumber"
              placeholder="Telephone"
              value={billing.phoneNumber}
              onChange={handleChange}
              autoComplete="tel"
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
              {provinces.map(province => (
                <option key={province.code} value={province.code}>
                  {province.name}
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
