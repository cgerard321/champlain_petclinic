import * as React from 'react';
import { deleteVet } from '@/features/veterinarians/api/deleteVet';

interface DeleteVetProps {
  vetId: string;
  onVetDeleted: (event: React.MouseEvent, vetId: string) => void;
}

const DeleteVet: React.FC<DeleteVetProps> = ({ vetId, onVetDeleted }) => {
  const handleDelete = async (
    event: React.MouseEvent<HTMLButtonElement>
  ): Promise<void> => {
    event.stopPropagation();
    if (window.confirm('Are you sure you want to delete this vet?')) {
      try {
        await deleteVet(vetId);
        onVetDeleted(event, vetId);
      } catch (error) {
        console.error('Failed to delete vet:', error);
      }
    }
  };
  return (
    <button onClick={handleDelete} className="btn btn-danger">
      Deactivate
    </button>
  );
};

export default DeleteVet;

/*
// Temporary replacement for the delete button.
// Original delete logic is commented out above so it can be restored easily.
// This component now shows a modal explaining the delete feature is disabled.


const DeleteVet: React.FC<DeleteVetProps> = () => {
  const [isModalOpen, setIsModalOpen] = React.useState(false);
  const cardElementRef = React.useRef<HTMLElement | null>(null);
  const prevPositionRef = React.useRef<string | null>(null);

  const handleClick = (event: React.MouseEvent<HTMLButtonElement>): void => {
    event.stopPropagation();

    // Find the nearest card container so the overlay will be positioned inside it.
    const cardEl = (event.currentTarget as HTMLElement).closest(
      '.card-vets'
    ) as HTMLElement | null;

    if (cardEl) {
      cardElementRef.current = cardEl;
      prevPositionRef.current = cardEl.style.position || '';
      // Ensure overlay absolute positioning is relative to the card
      if (!prevPositionRef.current || prevPositionRef.current === 'static') {
        cardEl.style.position = 'relative';
      }
    }

    setIsModalOpen(true);
  };

  const closeModal = (event?: React.MouseEvent): void => {
    event?.stopPropagation();
    setIsModalOpen(false);

    // Restore previous position if we changed it
    const cardEl = cardElementRef.current;
    if (cardEl) {
      cardEl.style.position = prevPositionRef.current ?? '';
      cardElementRef.current = null;
      prevPositionRef.current = null;
    }
  };

  return (
    <>
      <button onClick={handleClick} className="btn btn-danger">
        Delete
      </button>

      {isModalOpen && (
        <div
          role="dialog"
          aria-modal="true"
          aria-label="Delete disabled"
          onClick={closeModal}
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
            backgroundColor: 'rgba(0,0,0,0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
        >
          <div
            onClick={e => e.stopPropagation()}
            style={{
              background: '#fff',
              padding: '20px',
              borderRadius: '8px',
              maxWidth: '400px',
              width: '90%',
              textAlign: 'center',
            }}
          >
            <h3>Function temporarily disabled</h3>
            <p>
              The delete vet feature is temporarily disabled. It will be
              re-enabled later.
            </p>
            <div style={{ marginTop: '16px' }}>
              <button
                onClick={closeModal}
                style={{
                  padding: '8px 16px',
                  borderRadius: '4px',
                  border: 'none',
                  background: '#007bff',
                  color: '#fff',
                  cursor: 'pointer',
                }}
              >
                OK
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default DeleteVet;

*/
