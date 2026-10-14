import * as React from 'react';
import { deleteVet } from '@/features/veterinarians/api/deleteVet';

interface DeleteVetProps {
  vetId: string;
  onVetDeleted: (event: React.MouseEvent, vetId: string) => void;
}

const DeleteVet: React.FC<DeleteVetProps> = ({ vetId, onVetDeleted }) => {
  const [showModal, setShowModal] = React.useState<boolean>(false);
  const [savedEvent, setSavedEvent] = React.useState<React.MouseEvent<HTMLButtonElement> | null>(null);

  const handleDeleteClick = (event: React.MouseEvent<HTMLButtonElement>): void => {
    event.stopPropagation();
    setSavedEvent(event);
    setShowModal(true);
  };

  const handleConfirmDelete = async (): Promise<void> => {
    try {
      await deleteVet(vetId);
      setShowModal(false);
      if (savedEvent) {
        onVetDeleted(savedEvent, vetId);
      }
    } catch (error) {
      console.error('Failed to delete vet:', error);
      setShowModal(false);
    }
  };

  return (
      <>
        <button onClick={handleDeleteClick} className="btn btn-danger">
          Deactivate
        </button>

        {showModal && (
            <div
                role="dialog"
                aria-modal="true"
                aria-label="Confirm Deactivation"
                onClick={(e) => {
                  e.stopPropagation();
                  setShowModal(false);
                }}
                style={{
                  position: 'fixed',
                  top: 0,
                  left: 0,
                  width: '100vw',
                  height: '100vh',
                  backgroundColor: 'rgba(0, 0, 0, 0.5)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  zIndex: 1050,
                }}
            >
              <div
                  onClick={(e) => e.stopPropagation()}
                  style={{
                    background: '#fff',
                    padding: '24px',
                    borderRadius: '8px',
                    maxWidth: '400px',
                    width: '90%',
                    textAlign: 'center',
                    boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
                  }}
              >
                <h3 style={{ marginBottom: '12px', fontSize: '1.25rem' }}>Confirm Action</h3>
                <p style={{ color: '#555', marginBottom: '20px' }}>
                  Are you sure you want to delete this vet?
                </p>
                <div style={{ display: 'flex', justifyContent: 'center', gap: '12px' }}>
                  <button
                      onClick={(e) => {
                        e.stopPropagation();
                        setShowModal(false);
                      }}
                      className="btn btn-secondary"
                  >
                    Cancel
                  </button>
                  <button onClick={handleConfirmDelete} className="btn btn-danger">
                    Confirm Delete
                  </button>
                </div>
              </div>
            </div>
        )}
      </>
  );
};

export default DeleteVet;