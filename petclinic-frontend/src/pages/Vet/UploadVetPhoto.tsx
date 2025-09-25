import * as React from 'react';
import { useState } from 'react';
import { Button, Form, Modal } from 'react-bootstrap';
import { addPhotoByVetId } from '@/features/veterinarians/api/addPhotoByVetId';

const UploadVetPhoto: React.FC = (): JSX.Element => {
  const [vetId, setVetId] = useState('');
  const [photoName, setPhotoName] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [show, setShow] = useState(false);

  const handleShow = (): void => setShow(true);
  const handleClose = (): void => {
    setShow(false);
    resetForm();
  };

  const resetForm = (): void => {
    setVetId('');
    setPhotoName('');
    setSelectedFile(null);
  };

  const handleFileChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ): void => {
    const file = event.target.files?.[0] || null;
    setSelectedFile(file);
  };

  const handleUpload = async (): Promise<void> => {
    if (!selectedFile) {
      console.error('No file selected');
      return;
    }

    try {
      await addPhotoByVetId(
        vetId,
        photoName || selectedFile.name,
        selectedFile
      );
      handleClose();
    } catch (error) {
      console.error('Error uploading photo:', error);
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
              />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose}>
            Close
          </Button>
          <Button variant="primary" onClick={handleUpload}>
            Upload
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
};

export default UploadVetPhoto;
