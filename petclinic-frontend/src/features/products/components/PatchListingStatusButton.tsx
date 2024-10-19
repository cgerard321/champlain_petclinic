import { useState } from 'react';
import { Button, Modal } from 'react-bootstrap';
import { ProductModel } from '../models/ProductModels/ProductModel';
import { getProduct } from '../api/getProduct';
import { patchListingStatus } from '../api/patchListingStatus';

interface PatchListingStatus {
  productId: string;
  // getProduct: (productId: string) => Promise<ProductModel>;
  // patchListingStatus: (
  //   productId: string
  //   productRequestModel: Partial<ProductModel>
  // ) => Promise<ProductModel>;
}

export default function PatchListingStatusButton({
  productId,
  // getProduct,
  // patchListingStatus,
}: PatchListingStatus): JSX.Element {
  const [show, setShow] = useState(false);

  const handleClose = (): void => setShow(false);
  const handleShow = (): void => setShow(true);

  const handleSubmit = async (
    event: React.FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    const product = await getProduct(productId);

    if (product.isUnlisted === false) {
      const isUnlisted = true;

      const patchedProduct: Partial<ProductModel> = {
        isUnlisted,
      };

      patchListingStatus(productId, patchedProduct);
      // } else {
      //   const isUnlisted = false;
    }
    handleClose();
  };
  return (
    <>
      <Button variant="primary" onClick={handleShow}>
        Unlist Item
      </Button>

      <Modal
        show={show}
        onHide={handleClose}
        backdrop="static"
        keyboard={false}
      >
        <Modal.Header closeButton>
          <Modal.Title>Unlist Item</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          Do you want to unlist this item? Customers will not be able to see
          this item anymore until you relist it back.
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={handleClose}>
            Cancel
          </Button>

          <form onSubmit={handleSubmit}>
            <Button type="submit">Yes</Button>
          </form>
        </Modal.Footer>
      </Modal>
    </>
  );
}
