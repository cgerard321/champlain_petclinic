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
        <h4>Delete Review</h4>
        <p>
          Are you sure you want to delete your review? This action cannot be
          undone.
        </p>
        <div className="delete-modal-buttons">
          <Button className="cancel-btn" onClick={onClose}>
            Cancel
          </Button>
          <Button variant="danger" className="delete-btn" onClick={onConfirm}>
            Delete
          </Button>
        </div>
      </Modal.Body>
    </Modal>
  );
};

export default DeleteReviewModal;
