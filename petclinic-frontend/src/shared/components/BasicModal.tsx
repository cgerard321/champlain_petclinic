import * as React from 'react';
import { useState } from 'react';
import { Button, Modal } from 'react-bootstrap';

import './BasicModal.css';

interface BasicModalProps {
  title?: string; //The header of the modal
  confirmText?: string; //The text shown on the confirmation button
  onConfirm?: () => Promise<void>; //The function to be handled upon pressing the confirmation button
  formId?: string; //The id of the form inside of the modal
  validate?: () => boolean; //What needs to be true before it handles confirmation
  showButton: JSX.Element; //The button that shows the modal
  errorMessage?: string; //The error message that will show at the bottom of the modal
  loading?: boolean; //Displays a basic "Loading..."
  children?: React.ReactNode; //The body of the modal
}

const BasicModal: React.FC<BasicModalProps> = ({
  title,
  confirmText = 'Confirm',
  onConfirm,
  formId,
  validate = () => true,
  showButton,
  errorMessage = null,
  loading = false,
  children,
}) => {
  const [show, setShow] = useState(false);
  const [busy, setBusy] = useState(false);

  // Show modal
  const handleShow = (): void => setShow(true);
  // Close modal
  const handleClose = (): void => {
    if (busy) return;
    setShow(false);
    resetForm();
  };

  const resetForm = (): void => {
    setBusy(false);
    // setErrorMessage('');
  };

  const handleConfirm = async (): Promise<void> => {
    if (busy) return;

    if (!validate()) return;

    setBusy(true);

    if (onConfirm) await onConfirm();

    handleClose();
    setBusy(false);
  };

  // const handleSubmitForm = async (): Promise<void> => {
  //   if (busy) return;

  //   if (!validate()) return;

  //   setIsLoading(true);
  //   try {
  //     if (onConfirm) await onConfirm();
  //     setTimeout(() => {
  //       window.location.reload();
  //     }, 100);
  //   } catch (error) {
  //     setErrorMessage(`Error updating visit: ${error}`);
  //   } finally {
  //     setIsLoading(false);
  //   }
  // };

  const renderConfirmButton = (): JSX.Element =>
    onConfirm || formId != null ? (
      <Button
        form={formId}
        type="submit"
        variant="primary"
        onClick={handleConfirm}
        disabled={busy}
      >
        {confirmText}
      </Button>
    ) : (
      <></>
    );

  return (
    <>
      {React.cloneElement(showButton, { onClick: handleShow })}

      <Modal
        show={show}
        onHide={handleClose}
        backdrop={busy ? 'static' : true}
        size="xl"
        className="basic-modal"
      >
        <h2 className="mx-auto">{title}</h2>

        <div className="basic-modal-body">
          {loading ? 'Loading...' : children}
        </div>
        {errorMessage && <div className="error">{errorMessage}</div>}
        <div className="basic-modal-footer">
          <Button variant="secondary" onClick={handleClose} disabled={busy}>
            Close
          </Button>
          {renderConfirmButton()}
        </div>
      </Modal>
    </>
  );
};

export default BasicModal;
