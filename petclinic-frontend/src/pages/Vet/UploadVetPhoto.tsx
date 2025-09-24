import { useState, FormEvent, ChangeEvent, FC } from 'react';
import { Button, Form, Modal, Spinner } from 'react-bootstrap';
import { addPhotoByVetId } from '@/features/veterinarians/api/addPhotoByVetId';

interface UploadVetPhotoProps {
  // Add props here if needed in the future
}

/**
 * Component for uploading veterinarian photos.
 * Provides a modal interface for selecting and uploading image files.
 */
const UploadVetPhoto: FC<UploadVetPhotoProps> = () => {
  const [vetId, setVetId] = useState('');
  const [photoName, setPhotoName] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [show, setShow] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleShow = (): void => setShow(true);
  const handleClose = (): void => {
    setShow(false);
    resetForm();
  };

  const resetForm = (): void => {
    setVetId('');
    setPhotoName('');
    setSelectedFile(null);
    setErrorMsg(null);
    setSubmitting(false);
  };

  const handleFileChange = (event: ChangeEvent<HTMLInputElement>): void => {
    const file = event.target.files?.[0] || null;
    setSelectedFile(file);
    setErrorMsg(null);
  };

  const handleUpload = async (event?: FormEvent): Promise<void> => {
    event?.preventDefault();

    setErrorMsg(null);
    if (!selectedFile) {
      setErrorMsg('Please choose a file to upload.');
      return;
    }

    setSubmitting(true);
    try {
      await addPhotoByVetId(
        vetId || '',
        photoName || selectedFile.name,
        selectedFile
      );
      handleClose();
    } catch (err: unknown) {
      console.error('Upload error', err);
      setErrorMsg('Upload failed. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <Button variant="primary" onClick={handleShow}>
        Upload Vet Photo
      </Button>

      <Modal show={show} onHide={handleClose}>
        <Modal.Header closeButton>
          <Modal.Title>Upload Photo</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form>
            <Form.Group controlId="vetId">
              <Form.Label>Vet ID</Form.Label>
              <Form.Control
                type="text"
                value={vetId}
                onChange={e => setVetId(e.target.value)}
                placeholder="Enter Vet ID"
                required
              />
            </Form.Group>
            <Form.Group controlId="photoName">
              <Form.Label>Photo Name</Form.Label>
              <Form.Control
                type="text"
                value={photoName}
                onChange={e => setPhotoName(e.target.value)}
                placeholder="Enter Photo Name"
                required
              />
            </Form.Group>
            <Form.Group controlId="fileUpload">
              <Form.Label>Choose a Photo</Form.Label>
              <Form.Control
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                required
                disabled={submitting}
              />
            </Form.Group>
            {errorMsg && <div className="alert alert-danger">{errorMsg}</div>}
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose}>
            Close
          </Button>
          <Button
            variant="primary"
            onClick={handleUpload}
            disabled={submitting}
          >
            {submitting ? (
              <>
                <Spinner animation="border" size="sm" className="me-2" />
                Uploading...
              </>
            ) : (
              'Upload'
            )}
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
};

export default UploadVetPhoto;
