import * as React from 'react';
import { useState } from 'react';
import { Button, Form, Modal, ProgressBar } from 'react-bootstrap';
import { addPhotoToAlbum } from '@/features/veterinarians/api/AddPhotoToAlbum';

interface UploadAlbumPhotoProps {
  vetId: string;
  onUploadComplete: () => void;
}

const UploadAlbumPhoto: React.FC<UploadAlbumPhotoProps> = ({
  vetId,
  onUploadComplete,
}) => {
  const [photoName, setPhotoName] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [show, setShow] = useState(false);
  const [busy, setBusy] = useState(false);
  const [progress, setProgress] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Show modal
  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleShow = () => setShow(true);
  // Close modal
  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleClose = () => {
    if (busy) return;
    setShow(false);
    setPhotoName('');
    setSelectedFile(null);
    setBusy(false);
    setProgress(null);
    setError(null);
  };

  // Handle file input change
  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] || null;
    setSelectedFile(file);
    if (file && !photoName) setPhotoName(file.name.replace(/\.[^/.]+$/, ''));
  };

  // Upload the photo
  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleUpload = async () => {
    if (!selectedFile) {
      setError('Please choose a file.');
      return;
    }
    setBusy(true);
    setError(null);
    setProgress(0);

    try {
      await addPhotoToAlbum(
        vetId,
        photoName || selectedFile.name,
        selectedFile
      );
      onUploadComplete();
      handleClose();
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Error uploading photo.');
    } finally {
      setBusy(false);
      setProgress(null);
    }
  };

  return (
    <>
      <Button
        variant="primary"
        onClick={handleShow}
        style={{
          backgroundColor: '#00a627ff',
          borderColor: '#00a627ff',
        }}
      >
        Add Photo to Album
      </Button>

      <Modal show={show} onHide={handleClose} backdrop={busy ? 'static' : true}>
        <Modal.Header className="d-flex justify-content-between align-items-center">
          <Modal.Title className="mx-auto">Upload Photo</Modal.Title>
        </Modal.Header>
        <Modal.Body className="text-center">
          <Form>
            <Form.Group controlId="photoName">
              <Form.Label>Photo Name</Form.Label>
              <Form.Control
                type="text"
                value={photoName}
                onChange={e => setPhotoName(e.target.value)}
                placeholder="Enter Photo Name (Optional)"
                disabled={busy}
                className="mx-auto"
                style={{ maxWidth: '300px' }}
              />
            </Form.Group>

            <Form.Group controlId="fileUpload" className="mt-3">
              <Form.Label>Choose a Photo</Form.Label>
              <Form.Control
                type="file"
                accept="image/*"
                onChange={handleFileChange}
                required
                disabled={busy}
                className="mx-auto"
                style={{ maxWidth: '300px' }}
              />
            </Form.Group>

            {progress !== null && (
              <div className="mt-3">
                <ProgressBar now={progress} label={`${progress}%`} />
              </div>
            )}

            {error && (
              <div className="text-danger mt-3" role="alert">
                {error}
              </div>
            )}
          </Form>
        </Modal.Body>
        <Modal.Footer className="justify-content-center">
          <Button
            style={{ backgroundColor: '#f93142ff', borderColor: '#f93142ff' }}
            onClick={handleClose}
            disabled={busy}
          >
            Close
          </Button>

          <Button
            style={{ backgroundColor: '#00a627ff', borderColor: '#00a627ff' }}
            variant="primary"
            onClick={handleUpload}
            disabled={!selectedFile || busy}
          >
            {busy ? 'Uploading…' : 'Upload'}
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
};

export default UploadAlbumPhoto;
