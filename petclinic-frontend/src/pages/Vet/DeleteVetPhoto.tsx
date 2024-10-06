import React from 'react';
import { Button } from 'react-bootstrap';
import { deleteVetPhoto } from '@/features/veterinarians/api/deleteVetPhoto';

interface DeleteVetPhotoProps {
  vetId: string;
  onPhotoDeleted: () => void;
}

const DeleteVetPhoto: React.FC<DeleteVetPhotoProps> = ({
  vetId,
  onPhotoDeleted,
}) => {
  const handleDeletePhoto = async () => {
    const confirmed = window.confirm(
      "Are you sure you want to delete the vet's photo?"
    );
    if (confirmed) {
      try {
        await deleteVetPhoto(vetId);
        alert('Photo deleted successfully.');
        onPhotoDeleted();
      } catch (error) {
        console.error('Error deleting photo:', error);
        alert('Failed to delete photo.');
      }
    }
  };

  return (
    <Button variant="danger" onClick={handleDeletePhoto}>
      Delete Photo
    </Button>
  );
};

export default DeleteVetPhoto;
