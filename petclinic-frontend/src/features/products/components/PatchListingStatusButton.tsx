import { useEffect, useState } from 'react';
import { Button, Modal } from 'react-bootstrap';
import { ProductModel } from '../models/ProductModels/ProductModel';
import { getProduct } from '../api/getProduct';
import { patchListingStatus } from '../api/patchListingStatus';

interface PatchListingStatus {
  productId: string;
}

export default function PatchListingStatusButton({
  productId,
}: PatchListingStatus): JSX.Element {
  const [show, setShow] = useState(false);
  const [product, setProduct] = useState<ProductModel | null>(null);

  const handleClose = (): void => setShow(false);
  const handleShow = (): void => setShow(true);

  useEffect(() => {
    const fetchProduct = async (): Promise<void> => {
      try {
        const fetchedProduct = await getProduct(productId);
        setProduct(fetchedProduct);
      } catch (err) {
        console.error('Failed to fetch product', err);
      }
    };

    fetchProduct();
  }, [productId]);

  const handleSubmit = async (
    event: React.FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();

    if (product) {
      const isUnlisted = !product.isUnlisted;

      const patchedProduct: Partial<ProductModel> = {
        isUnlisted,
      };

      await patchListingStatus(productId, patchedProduct);
      setProduct({ ...product, isUnlisted });
    }
    handleClose();
  };
  return (
    <>
      {!product?.isUnlisted ? (
        <Button variant="primary" onClick={handleShow}>
          Unlist Item
        </Button>
      ) : (
        <Button variant="primary" onClick={handleShow}>
          List Item
        </Button>
      )}

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
          Do you want to {product?.isUnlisted ? 'list back' : 'unlist'} this
          item? Customers will{' '}
          {product?.isUnlisted
            ? 'be able to see item again.'
            : 'not be able to see this item anymore until you relist it back.'}
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
