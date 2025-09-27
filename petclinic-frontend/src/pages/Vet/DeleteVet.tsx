import * as React from 'react'; // Correct import for React
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
      Delete
    </button>
  );
};

export default DeleteVet;
