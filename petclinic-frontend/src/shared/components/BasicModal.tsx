import * as React from 'react';
import { useState } from 'react';
import { Button, Modal } from 'react-bootstrap';

import './BasicModal.css';

interface BasicModalProps {
  title?: string; //The header of the modal
  confirmText?: string; //The text shown on the confirmation button
  onConfirm?: () => Promise<void>; //The function to be handled upon pressing the confirmation button
  refreshPageOnConfirm?: boolean; //If true, refreshes the page upon pressing the confirm button
  formId?: string; //The id of the form inside of the modal
  validate?: () => boolean; //What needs to be true before it handles confirmation
  showButton: JSX.Element; //The button that shows the modal
  errorMessage?: string;
  children?: React.ReactNode; //The body of the modal
}

const BasicModal: React.FC<BasicModalProps> = ({
  title,
  confirmText = 'Confirm',
  onConfirm,
  refreshPageOnConfirm,
  formId,
  validate = () => true,
  showButton,
  errorMessage,
  children,
}) => {
  const [show, setShow] = useState(false);
  const [busy, setBusy] = useState(false);
  // const [errorMessage, setErrorMessage] = useState<string>('');

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
    try {
      if (onConfirm) await onConfirm();
      if (refreshPageOnConfirm)
        setTimeout(() => {
          if (errorMessage) {
            window.location.reload();
          }
        }, 1000);
      handleClose;
    } catch (error) {
      // setErrorMessage(`Error updating visit: ${error}`);
    } finally {
      setBusy(false);
    }
  };

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
      <a onClick={handleShow}>{showButton}</a>

      <Modal
        show={show}
        onHide={handleClose}
        backdrop={busy ? 'static' : true}
        size="xl"
      >
        <h2 className="mx-auto">{title}</h2>
        <div className="basic-modal-body">{children}</div>
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
