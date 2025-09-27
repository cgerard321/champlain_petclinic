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
  const handleDeleteEducation = async (
    event: React.MouseEvent<HTMLButtonElement>
  ): Promise<void> => {
    event.stopPropagation();
    if (window.confirm('Are you sure you want to delete this Education?')) {
      try {
        await deleteVetEducation(vetId, educationId);
        onEducationDeleted(educationId);
      } catch (error) {
        console.error('Failed to delete education:', error);
      }
    }
  };

  return (
    <button onClick={handleDeleteEducation} className="btn btn-danger">
      Delete Education
    </button>
  );
};

export default DeleteVetEducation;
