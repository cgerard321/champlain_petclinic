import * as React from 'react';
import { useState } from 'react';
import { Button, Modal } from 'react-bootstrap';

import './BasicModal.css';

interface BasicModalProps {
  title?: string;
  confirmText?: string;
  onConfirm?: () => Promise<void>;
  formId?: string;
  validate?: () => boolean;
  showButton: JSX.Element;
  children?: React.ReactNode;
}

const BasicModal: React.FC<BasicModalProps> = ({
  title,
  confirmText = 'Confirm',
  onConfirm,
  formId,
  validate = () => false,
  showButton,
  children,
}) => {
  const [show, setShow] = useState(false);
  const [busy, setBusy] = useState(false);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  // const [progress, setProgress] = useState<number | null>(null);
  const [errorMessage, setErrorMessage] = useState<string>('');

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
    // setProgress(null);
    // setError(null);
  };

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleConfirm = async (): Promise<void> => {
    if (busy) return;

    if (!validate()) return;

    setIsLoading(true);
    // setErrorMessage('');
    // setSuccessMessage('');
    try {
      if (onConfirm) await onConfirm();
    } catch (error) {
      // const apiError = error as ApiError;
      setErrorMessage(`Error updating visit: ${error}`);
    }

    setIsLoading(false);
    // setErrorMessage('');
    // setSuccessMessage('');

    handleClose();
  };

  const renderConfirmButton = (): JSX.Element =>
    onConfirm || formId != null ? (
      <Button
        form={formId}
        type="submit"
        variant="primary"
        onClick={handleConfirm}
        disabled={isLoading}
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
        <div className="basic-modal-footer">
          <Button
            variant="secondary"
            onClick={handleClose}
            disabled={isLoading}
          >
            Close
          </Button>
          {renderConfirmButton()}
        </div>
        {errorMessage && <div className="error">{errorMessage}</div>}
      </Modal>
    </>
  );
};

export default BasicModal;
