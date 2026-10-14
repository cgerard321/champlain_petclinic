import * as React from 'react';
import { deleteVetEducation } from '@/features/veterinarians/api/deleteVetEducation.ts';

interface DeleteVetEducationProps {
  vetId: string;
  educationId: string;
  onEducationDeleted: (educationId: string) => void;
}

const DeleteVetEducation: React.FC<DeleteVetEducationProps> = ({
                                                                 vetId,
                                                                 educationId,
                                                                 onEducationDeleted,
                                                               }) => {
  const [showModal, setShowModal] = React.useState<boolean>(false);

  const handleDeleteClick = (event: React.MouseEvent<HTMLButtonElement>): void => {
    event.stopPropagation();
    setShowModal(true);
  };

  const handleConfirmDelete = async (): Promise<void> => {
    try {
      await deleteVetEducation(vetId, educationId);
      setShowModal(false);
      onEducationDeleted(educationId);
    } catch (error) {
      console.error('Failed to delete education:', error);
      setShowModal(false);
    }
  };

  return (
      <>
        <button onClick={handleDeleteClick} className="btn btn-danger">
          Delete Education
        </button>

        {showModal && (
            <div
                role="dialog"
                aria-modal="true"
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
                <h4 style={{ marginBottom: '12px' }}>Confirm Deletion</h4>
                <p style={{ color: '#555', marginBottom: '20px' }}>
                  Are you sure you want to delete this Education?
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
                    Delete
                  </button>
                </div>
              </div>
            </div>
        )}
      </>
  );
};

export default DeleteVetEducation;