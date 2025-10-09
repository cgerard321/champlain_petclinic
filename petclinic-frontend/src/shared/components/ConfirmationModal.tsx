import * as React from 'react';
import { useState } from 'react';
import { Button, Modal } from 'react-bootstrap';

interface ConfirmationModalProps {
  title: string;
  showButton: JSX.Element;
  onConfirm: () => Promise<void>;
  children?: React.ReactNode;
}

const ConfirmationModal: React.FC<ConfirmationModalProps> = ({
  title,
  onConfirm,
  showButton,
  children,
}) => {
  const [show, setShow] = useState(false);
  const [busy, setBusy] = useState(false);
  // const [progress, setProgress] = useState<number | null>(null);
  // const [error, setError] = useState<string | null>(null);

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
    onConfirm();
    handleClose();
  };

  return (
    <>
      {/* <Button variant="primary" onClick={handleShow}>
        {title}
      </Button> */}
      <a onClick={handleShow}>{showButton}</a>

      <Modal show={show} onHide={handleClose} backdrop={busy ? 'static' : true}>
        <Modal.Header className="d-flex justify-content-between align-items-center">
          <Modal.Title className="mx-auto">{title}</Modal.Title>
        </Modal.Header>
        <Modal.Body className="text-center">
          {children}
          {/* <Form>
            {progress !== null && (
              <div className="mt-3">
                <ProgressBar now={progress} label={`${progress}%`} />
              </div>
            )}

            
          </Form> */}
          {/* {error && (
            <div className="text-danger mt-3" role="alert">
              {error}
            </div>
          )} */}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose}>
            Close
          </Button>
          <Button variant="primary" onClick={handleConfirm}>
            Confirm
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
};

export default ConfirmationModal;
