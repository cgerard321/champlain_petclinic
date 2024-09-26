import { useState } from 'react';
import { Button, Form, Modal } from 'react-bootstrap';
import { ProductModel } from '../models/ProductModels/ProductModel';

interface AddProductProps {
  addProduct: (product: Omit<ProductModel, 'productId'>) => Promise<void>;
}

export default function AddProduct({
  addProduct,
}: AddProductProps): JSX.Element {
  const [show, setShow] = useState(false);
  const [productType, setProductType] = useState('');

  const handleClose = (): void => setShow(false);
  const handleShow = (): void => setShow(true);

  const handleSubmit = async (
    event: React.FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    const form = event.currentTarget;
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
    const status = 'AVAILABLE';

    const newProduct: Omit<ProductModel, 'productId'> = {
      productName,
      productDescription,
      productSalePrice,
      averageRating,
      productQuantity,
      requestCount,
      status,
      productType,
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
                  onChange={e => setProductType(e.target.value)} /////////////////
                  required
              />
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
