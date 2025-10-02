import { FC } from 'react';
import './CartBillingForm.css';

interface ConfirmCheckoutModalProps {
  isOpen: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}

const ConfirmCheckoutModal: FC<ConfirmCheckoutModalProps> = ({
  isOpen,
  onConfirm,
  onCancel,
}) => {
  if (!isOpen) return null;

  return (
    <div className="confirm-modal-backdrop">
      <div className="confirm-modal-content">
        <h2>Confirm Checkout</h2>
        <p>Are you sure you want to checkout?</p>
        <div className="confirm-modal-buttons">
          <button className="confirm" onClick={onConfirm}>
            Yes
          </button>
          <button className="cancel" onClick={onCancel}>
            No
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmCheckoutModal;
