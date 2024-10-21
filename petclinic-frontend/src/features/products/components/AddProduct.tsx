import { useState } from 'react';
import { Button, Form, Modal } from 'react-bootstrap';
import { ProductModel } from '../models/ProductModels/ProductModel';
import { ImageModel } from '../models/ProductModels/ImageModel';
import { DeliverType } from '@/features/products/models/ProductModels/DeliverType.ts';

interface AddProductProps {
  addProduct: (product: ProductModel) => Promise<ProductModel>;
  addImage: (formData: FormData) => Promise<ImageModel>;
}

export default function AddProduct({
  addProduct,
  addImage,
}: AddProductProps): JSX.Element {
  const [show, setShow] = useState(false);
  const [productType, setProductType] = useState('');
  const [dateAdded, setDateAdded] = useState('');
  const [releaseDate, setReleaseDate] = useState('');
  const [error, setError] = useState('');
  const [deliveryType, setDeliveryType] = useState<DeliverType>(
    DeliverType.DELIVERY
  );

  const handleClose = (): void => {
    setShow(false);
    setError('');
  };
  const handleShow = (): void => setShow(true);

  const validateDates = (): boolean => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const addedDate = new Date(dateAdded);
    const releaseDateObj = releaseDate ? new Date(releaseDate) : null;

    if (addedDate > today) {
      setError('Date Added cannot be in the future.');
      return false;
    }

    if (releaseDateObj && releaseDateObj < addedDate) {
      setError('Release Date cannot be before Date Added.');
      return false;
    }

    setError('');
    return true;
  };

  const handleSubmit = async (
    event: React.FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    if (!validateDates()) {
      return;
    }

    const form = event.currentTarget;
    let createdImage = null;

    const fileInput =
      form.querySelector<HTMLInputElement>('input[type="file"]');

    if (fileInput && fileInput.files && fileInput.files.length > 0) {
      const file = fileInput.files[0];

      const formData = new FormData();
      formData.append('imageName', file.name);
      formData.append('imageType', file.type);
      formData.append('imageData', file);

      try {
        createdImage = await addImage(formData);
      } catch (error) {
        console.error('Error adding image:', error);
      }
    }

    if (!createdImage) {
      console.error('Failed creating image');
      return;
    }

    const imageId = createdImage.imageId;
    const productName = (
      form.elements.namedItem('productName') as HTMLInputElement
    ).value;
    const productDescription = (
      form.elements.namedItem('productDescription') as HTMLInputElement
    ).value;
    const productSalePrice = parseFloat(
      (form.elements.namedItem('productSalePrice') as HTMLInputElement).value
    );
    const productQuantity = parseInt(
      (form.elements.namedItem('productQuantity') as HTMLInputElement).value,
      10
    );
    const requestCount = 0;
    const averageRating = 0;
    const productId = '';
    const isUnlisted = false;

    const dateAddedObj = new Date(dateAdded);
    const releaseDateObj = releaseDate ? new Date(releaseDate) : undefined;

    let productStatus: 'PRE_ORDER' | 'AVAILABLE' | 'OUT_OF_STOCK';
    if (releaseDateObj && releaseDateObj > new Date()) {
      productStatus = 'PRE_ORDER';
    } else if (productQuantity > 0) {
      productStatus = 'AVAILABLE';
    } else {
      error;
      productStatus = 'OUT_OF_STOCK';
    }

    const newProduct: ProductModel = {
      productId,
      imageId,
      productName,
      productDescription,
      productSalePrice,
      averageRating,
      productQuantity,
      productStatus,
      requestCount,
      productType,
      isUnlisted,
      dateAdded: dateAddedObj,
      releaseDate: releaseDateObj,
      deliveryType,
    };

    try {
      await addProduct(newProduct);
      handleClose();
    } catch (error) {
      console.error('Error adding product:', error);
    }
  };

  return (
    <div>
      <Button variant="secondary" onClick={handleShow}>
        Add Product
      </Button>
      <Modal
        show={show}
        onHide={handleClose}
        backdrop="static"
        keyboard={false}
      >
        <Modal.Header closeButton>Product Details</Modal.Header>
        <Modal.Body>
          <Form id="addModal" onSubmit={handleSubmit}>
            <Form.Group className="mb-3" controlId="formGridName">
              <Form.Label>Name</Form.Label>
              <Form.Control
                type="text"
                name="productName"
                placeholder="Product Name"
                required
              />
            </Form.Group>
            <Form.Group className="mb-3" controlId="formGridDescription">
              <Form.Label>Description</Form.Label>
              <Form.Control
                placeholder="Product Description"
                as="textarea"
                name="productDescription"
                rows={2}
                required
              />
            </Form.Group>
            <Form.Group className="mb-3" controlId="formGridSalePrice">
              <Form.Label>Sale Price</Form.Label>
              <Form.Control
                type="number"
                step="0.01"
                name="productSalePrice"
                placeholder="Sale Price"
                required
              />
            </Form.Group>
            <Form.Group className="mb-3" controlId="formGridQuantity">
              <Form.Label>Quantity</Form.Label>
              <Form.Control
                type="number"
                name="productQuantity"
                placeholder="Product Quantity"
                required
              />
            </Form.Group>
            <Form.Group className="mb-3" controlId="formGridType">
              <Form.Label>Type</Form.Label>
              <Form.Control
                type="text"
                name="productType"
                placeholder="Product Type"
                value={productType}
                onChange={e => setProductType(e.target.value)}
                required
              />
            </Form.Group>
            <Form.Group className="mb-3" controlId="formGridDateAdded">
              <Form.Label>Date Added</Form.Label>
              <Form.Control
                type="date"
                name="dateAdded"
                value={dateAdded}
                onChange={e => setDateAdded(e.target.value)}
                max={new Date().toISOString().split('T')[0]}
                required
              />
            </Form.Group>
            <Form.Group className="mb-3" controlId="formGridReleaseDate">
              <Form.Label>Release Date (for pre-orders)</Form.Label>
              <Form.Control
                type="date"
                name="releaseDate"
                value={releaseDate}
                onChange={e => setReleaseDate(e.target.value)}
                min={dateAdded}
              />
            </Form.Group>
            <Form.Group controlId="formFile" className="mb-3">
              <Form.Label>Upload Image</Form.Label>
              <Form.Control type="file" />
            </Form.Group>
            <Form.Group className="mb-3" controlId="formGridDeliveryType">
              <Form.Label>Delivery Type</Form.Label>
              <Form.Select
                value={deliveryType}
                onChange={e => setDeliveryType(e.target.value as DeliverType)}
              >
                <option value={DeliverType.DELIVERY}>Delivery</option>
                <option value={DeliverType.PICKUP}>Pickup</option>
                <option value={DeliverType.DELIVERY_AND_PICKUP}>
                  Delivery and Pickup
                </option>
                <option value={DeliverType.NO_DELIVERY_OPTION}>
                  No Delivery Option
                </option>
              </Form.Select>
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
