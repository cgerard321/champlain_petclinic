import * as React from 'react';
import { deleteVet } from '@/features/veterinarians/api/deleteVet';

interface DeleteVetProps {
  vetId: string;
  onVetDeleted: () => void;
}

const DeleteVet: React.FC<DeleteVetProps> = ({
  vetId,
  onVetDeleted,
}): JSX.Element => {
  const handleDelete = async (): Promise<void> => {
    if (window.confirm('Are you sure you want to delete this vet?')) {
      try {
        await deleteVet(vetId);
        onVetDeleted();
        alert('Vet deleted successfully');
      } catch (error) {
        console.error('Error deleting vet:', error);
        alert('Failed to delete vet');
      }
    }
  };

  return (
    <button onClick={handleDelete} className="btn btn-danger">
      Delete
    </button>
  );
};

export default DeleteVet;
