import * as React from 'react';
import styles from './InvProForm.module.css';

interface ConfirmationModalProps {
  show: boolean;
  message: string;
  onConfirm: () => void;
  onCancel: () => void;
}

const ConfirmationModal: React.FC<ConfirmationModalProps> = ({
  show,
  message,
  onConfirm,
  onCancel,
}) => {
  if (!show) return null;

  return (
    <div className={styles.overlay}>
      <div className={styles['form-container']}>
        <h2>Delete this product?</h2>
        <p>{message}</p>
        <div className={styles['modal-actions']}>
          <button
            className={`${styles.btn} ${styles['btn-danger']}`}
            onClick={onConfirm}
            style={{ backgroundColor: 'red', color: 'white' }}
          >
            Delete
          </button>
          <button
            className={`${styles.btn} ${styles['btn-secondary']}`}
            onClick={onCancel}
            style={{ backgroundColor: 'black', color: 'white' }}
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmationModal;
