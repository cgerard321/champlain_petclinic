import { useTranslation } from 'react-i18next';
import { Modal, Button } from 'react-bootstrap';
import './DeleteReviewModal.css';

interface DeleteReviewModalProps {
  show: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

const DeleteReviewModal = ({
  show,
  onClose,
  onConfirm,
  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
}: DeleteReviewModalProps) => {
  const { t } = useTranslation('products');
  return (
    <Modal
      show={show}
      onHide={onClose}
      centered
      dialogClassName="delete-modal-dialog"
    >
      <button className="delete-modal-close-btn" onClick={onClose}>
        ×
      </button>
      <Modal.Body className="delete-modal-body">
        <h4>{t('deleteReview')}</h4>
        <p>{t('deleteConfirm')}</p>
        <div className="delete-modal-buttons">
          <Button className="cancel-btn" onClick={onClose}>
            {t('cancel')}
          </Button>
          <Button variant="danger" className="delete-btn" onClick={onConfirm}>
            {t('delete')}
          </Button>
        </div>
      </Modal.Body>
    </Modal>
  );
};

export default DeleteReviewModal;
