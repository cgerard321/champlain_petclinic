import { useState, ChangeEvent, FormEvent } from 'react';
import { useTranslation } from 'react-i18next';
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
  const { t } = useTranslation('carts');
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
      setError(t('billing.cardInvalid'));
      setLoading(false);
      return;
    }
    if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(billing.expiry)) {
      setError(t('billing.expiryInvalid'));
      setLoading(false);
      return;
    }
    if (!/^\d{3,4}$/.test(billing.cvv)) {
      setError(t('billing.cvvInvalid'));
      setLoading(false);
      return;
    }

    setLoading(false);
    setShowConfirm(true);
  };

  const handleConfirm = (): void => {
    setShowConfirm(false);
    setSuccess(t('billing.success'));
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
          aria-label={t('billing.close')}
        >
          ✕
        </button>
        <h2>{t('billing.title')}</h2>

        {error && <div className="error">{error}</div>}
        {success && <div className="success">{success}</div>}

        <form onSubmit={handleSubmit} className="billing-form">
          <div className="main-fields">
            <input
              type="text"
              name="fullName"
              placeholder={t('billing.fullName')}
              value={billing.fullName}
              onChange={handleChange}
              required
            />
            <input
              type="email"
              name="email"
              placeholder={t('billing.email')}
              value={billing.email}
              onChange={handleChange}
              required
            />
            <input
              type="text"
              name="address"
              placeholder={t('billing.address')}
              value={billing.address}
              onChange={handleChange}
              required
            />
            <input
              type="text"
              name="city"
              placeholder={t('billing.city')}
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
              <option value="">{t('billing.selectProvince')}</option>
              {provinces.map(prov => (
                <option key={prov} value={prov}>
                  {prov}
                </option>
              ))}
            </select>
            <input
              type="text"
              name="postalCode"
              placeholder={t('billing.postalCode')}
              value={billing.postalCode}
              onChange={handleChange}
              required
            />
            <input
              type="text"
              name="cardNumber"
              placeholder={t('billing.cardNumber')}
              value={billing.cardNumber}
              onChange={handleChange}
              required
            />
            <div className="form-row">
              <div className="form-field">
                <input
                  type="text"
                  name="expiry"
                  placeholder={t('billing.expiryPlaceholder')}
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
            {loading ? t('billing.processing') : t('billing.submit')}
          </button>
        </form>
      </div>

      {showConfirm && (
        <div className="confirm-modal-backdrop">
          <div className="confirm-modal-content cart-panel cart-panel--padded">
            <h2>{t('checkoutModal.title')}</h2>
            <p>{t('checkoutModal.message')}</p>
            <div className="confirm-modal-buttons">
              <button
                className="cart-button cart-button--brand cart-button--tall"
                onClick={handleConfirm}
              >
                {t('buttons.yes')}
              </button>
              <button
                className="cart-button cart-button--danger cart-button--tall"
                onClick={handleCancel}
              >
                {t('buttons.no')}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default CartBillingForm;
