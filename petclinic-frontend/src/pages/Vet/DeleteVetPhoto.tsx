import * as React from 'react';
import { Button, Modal } from 'react-bootstrap';
import { deleteVetPhoto } from '@/features/veterinarians/api/deleteVetPhoto';

interface DeleteVetPhotoProps {
  vetId: string;
  onPhotoDeleted: () => void;
}

const DeleteVetPhoto: React.FC<DeleteVetPhotoProps> = ({
                                                         vetId,
                                                         onPhotoDeleted,
                                                       }) => {
  const [showConfirmModal, setShowConfirmModal] = React.useState<boolean>(false);
  const [alertState, setAlertState] = React.useState<{
    show: boolean;
    title: string;
    message: string;
    isSuccess: boolean;
  }>({ show: false, title: '', message: '', isSuccess: false });

  const handleConfirmDelete = async (): Promise<void> => {
    setShowConfirmModal(false);
    try {
      await deleteVetPhoto(vetId);
      setAlertState({
        show: true,
        title: 'Success',
        message: 'Photo deleted successfully.',
        isSuccess: true,
      });
    } catch (error) {
      console.error('Error deleting photo:', error);
      setAlertState({
        show: true,
        title: 'Error',
        message: 'Failed to delete photo.',
        isSuccess: false,
      });
    }
  };

  const handleAlertClose = (): void => {
    const wasSuccess = alertState.isSuccess;
    setAlertState({ show: false, title: '', message: '', isSuccess: false });
    if (wasSuccess) {
      onPhotoDeleted();
    }
  };

  return (
      <>
        <Button variant="danger" onClick={() => setShowConfirmModal(true)}>
          Delete Photo
        </Button>

        {/* Confirmation Modal */}
        <Modal show={showConfirmModal} onHide={() => setShowConfirmModal(false)} centered>
          <Modal.Header closeButton>
            <Modal.Title>Confirm Deletion</Modal.Title>
          </Modal.Header>
          <Modal.Body>
            Are you sure you want to delete the vet&apos;s photo?
          </Modal.Body>
          <Modal.Footer>
            <Button variant="secondary" onClick={() => setShowConfirmModal(false)}>
              Cancel
            </Button>
            <Button variant="danger" onClick={handleConfirmDelete}>
              Delete
            </Button>
          </Modal.Footer>
        </Modal>

        {/* Alert Result Modal */}
        <Modal show={alertState.show} onHide={handleAlertClose} centered>
          <Modal.Header closeButton>
            <Modal.Title>{alertState.title}</Modal.Title>
          </Modal.Header>
          <Modal.Body>{alertState.message}</Modal.Body>
          <Modal.Footer>
            <Button variant={alertState.isSuccess ? 'primary' : 'secondary'} onClick={handleAlertClose}>
              OK
            </Button>
          </Modal.Footer>
        </Modal>
      </>
  );
};

export default DeleteVetPhoto;