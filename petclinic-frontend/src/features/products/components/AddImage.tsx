/* eslint-disable no-console */
import { useState } from 'react';
import { Button, Form, Modal } from 'react-bootstrap';

interface AddImageProps {
  addImage: (formData: FormData) => Promise<void>;
}

export default function AddImage({ addImage }: AddImageProps): JSX.Element {
  const [show, setShow] = useState(false);

  const handleClose = (): void => setShow(false);
  const handleShow = (): void => setShow(true);

  const handleSubmit = async (
    event: React.FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    const form = event.currentTarget;
    const fileInput =
      form.querySelector<HTMLInputElement>('input[type="file"]');

    if (fileInput && fileInput.files && fileInput.files.length > 0) {
      const file = fileInput.files[0];
      // const imageName = file.name;
      // const imageType = file.type;

      const formData = new FormData();
      formData.append('imageName', file.name);
      formData.append('imageType', file.type);
      formData.append('imageData', file);

      // const arrayBuffer = await file.arrayBuffer();
      // const imageData = new Uint8Array(arrayBuffer);
      // const imageData = null;

      // const newImage: Omit<ImageModel, 'imageId'> = {
      //   imageName,
      //   imageType,
      //   imageData,
      // };
      // console.log(imageName);
      // console.log(imageType);
      // console.log(imageData);

      try {
        await addImage(formData);
        handleClose();
      } catch (error) {
        console.error('Error adding image:', error);
      }
    }
  };

  return (
    <div>
      <Button onClick={handleShow}>Add image</Button>
      <Modal
        show={show}
        onHide={handleClose}
        backdrop="static"
        keyboard={false}
      >
        <Modal.Header closeButton>Product Details</Modal.Header>
        <Modal.Body>
          <Form id="addModal" onSubmit={handleSubmit}>
            <Form.Group controlId="formFile" className="mb-3">
              <Form.Label>Upload Image</Form.Label>
              <Form.Control type="file" />
            </Form.Group>
          </Form>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose}>
            Close
          </Button>
          <Button form="addModal" variant="primary" type="submit">
            Add Product
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  );
}
