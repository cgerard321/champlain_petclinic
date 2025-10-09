import { useState, FormEvent } from 'react';
import { payBill } from '@/features/bills/api/payBill.tsx';
import './PaymentForm.css';

interface PaymentFormProps {
  billId: string;
  customerId: string;
  billAmount: number;
  baseAmount?: number;
  interestAmount?: number;
  onPaymentSuccess: () => void;
  onCancel: () => void;
}

interface PaymentFormData {
  cardNumber: string;
  cvv: string;
  expirationDate: string;
}

const PaymentForm = ({
  billId,
  customerId,
  billAmount,
  baseAmount,
  interestAmount,
  onPaymentSuccess,
  onCancel,
}: PaymentFormProps): JSX.Element => {
  const finalInterestAmount = interestAmount || 0;
  const actualBillAmount = billAmount || baseAmount || 0;

  const [formData, setFormData] = useState<PaymentFormData>({
    cardNumber: '',
    cvv: '',
    expirationDate: '',
  });
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [isProcessing, setIsProcessing] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { name, value } = e.target;
    let formattedValue = value;
    if (name === 'cardNumber') {
      formattedValue = value.replace(/\D/g, '').slice(0, 16);
    } else if (name === 'cvv') {
      formattedValue = value.replace(/\D/g, '').slice(0, 3);
    } else if (name === 'expirationDate') {
      formattedValue = value.replace(/\D/g, '');
      if (formattedValue.length >= 2) {
        formattedValue =
          formattedValue.slice(0, 2) + '/' + formattedValue.slice(2, 4);
      }
    }

    setFormData({
      ...formData,
      [name]: formattedValue,
    });

    if (errors[name]) {
      setErrors({
        ...errors,
        [name]: '',
      });
    }
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};

    if (!formData.cardNumber) {
      newErrors.cardNumber = 'Card number is required';
    } else if (formData.cardNumber.length !== 16) {
      newErrors.cardNumber = 'Card number must be 16 digits';
    }

    if (!formData.cvv) {
      newErrors.cvv = 'CVV is required';
    } else if (formData.cvv.length !== 3) {
      newErrors.cvv = 'CVV must be 3 digits';
    }

    if (!formData.expirationDate) {
      newErrors.expirationDate = 'Expiration date is required';
    } else {
      const expirationDatePattern = /^(0[1-9]|1[0-2])\/\d{2}$/;
      if (!expirationDatePattern.test(formData.expirationDate)) {
        newErrors.expirationDate = 'Invalid format. Use MM/YY';
      } else {
        const [expMonth, expYear] = formData.expirationDate.split('/');
        const currentDate = new Date();
        const currentYear = currentDate.getFullYear();
        const currentCentury = Math.floor(currentYear / 100) * 100;

        // Handle 2-digit year by assuming it's in the current or next century
        const expYearNum = Number(expYear);
        const expiryYearFull =
          expYearNum < 50
            ? currentCentury + 100 + expYearNum // 00-49 -> 2100-2149
            : currentCentury + expYearNum; // 50-99 -> 2050-2099

        // Create expiry date using the last day of the expiry month
        const expiryDate = new Date(expiryYearFull, Number(expMonth), 0); // Day 0 gives last day of previous month
        const currentMonthEnd = new Date(
          currentDate.getFullYear(),
          currentDate.getMonth() + 1,
          0
        );

        if (expiryDate < currentMonthEnd) {
          newErrors.expirationDate = 'Card is expired';
        }
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    if (!validate()) {
      return;
    }

    setIsProcessing(true);

    try {
      await payBill(customerId, billId, {
        cardNumber: formData.cardNumber,
        cvv: formData.cvv,
        expirationDate: formData.expirationDate,
      });

      onPaymentSuccess();
    } catch (error) {
      console.error('Payment error:', error);
      setErrors({
        general: 'Payment failed. Please try again.',
      });
    } finally {
      setIsProcessing(false);
    }
  };

  return (
    <div className="payment-modal-overlay">
      <div className="payment-modal">
        <div className="payment-header">
          <h2>Pay Bill</h2>
          <button className="close-button" onClick={onCancel} type="button">
            ×
          </button>
        </div>
        <div className="payment-details">
          <p>
            <strong>Bill ID:</strong> {billId}
          </p>
          {baseAmount && finalInterestAmount > 0 ? (
            <div className="payment-breakdown">
              <p>
                <strong>Base Amount:</strong> ${baseAmount.toFixed(2)}
              </p>
              <p>
                <strong>Interest:</strong> ${finalInterestAmount.toFixed(2)}
              </p>
              <hr className="breakdown-divider" />
              <p className="total-amount">
                <strong>Total Amount:</strong> ${actualBillAmount.toFixed(2)}
              </p>
            </div>
          ) : (
            <p>
              <strong>Amount:</strong> ${actualBillAmount.toFixed(2)}
            </p>
          )}
        </div>

        <form onSubmit={handleSubmit} className="payment-form">
          {errors.general && (
            <div className="error-message general-error">{errors.general}</div>
          )}

          <div className="form-group">
            <label htmlFor="cardNumber">Card Number</label>
            <input
              type="text"
              id="cardNumber"
              name="cardNumber"
              value={formData.cardNumber}
              onChange={handleChange}
              placeholder="1234 5678 9012 3456"
              maxLength={16}
              disabled={isProcessing}
            />
            {errors.cardNumber && (
              <span className="error-message">{errors.cardNumber}</span>
            )}
          </div>

          <div className="form-row">
            <div className="form-group">
              <label htmlFor="expirationDate">Expiration Date</label>
              <input
                type="text"
                id="expirationDate"
                name="expirationDate"
                value={formData.expirationDate}
                onChange={handleChange}
                placeholder="MM/YY"
                maxLength={5}
                disabled={isProcessing}
              />
              {errors.expirationDate && (
                <span className="error-message">{errors.expirationDate}</span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="cvv">CVV</label>
              <input
                type="text"
                id="cvv"
                name="cvv"
                value={formData.cvv}
                onChange={handleChange}
                placeholder="123"
                maxLength={3}
                disabled={isProcessing}
              />
              {errors.cvv && (
                <span className="error-message">{errors.cvv}</span>
              )}
            </div>
          </div>

          <div className="form-actions">
            <button
              type="button"
              className="cancel-button"
              onClick={onCancel}
              disabled={isProcessing}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="pay-button"
              disabled={isProcessing}
            >
              {isProcessing
                ? 'Processing...'
                : `Pay $${actualBillAmount.toFixed(2)}`}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default PaymentForm;
