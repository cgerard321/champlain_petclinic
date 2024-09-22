import * as React from 'react'; // Correct import for React

interface DeleteVetProps {
  vetId: string;
  onVetDeleted: (event: React.MouseEvent, vetId: string) => void;
}

const DeleteVet: React.FC<DeleteVetProps> = ({ vetId, onVetDeleted }) => {
  const handleDelete = (event: React.MouseEvent): void => {
    // Added void as return type
    event.stopPropagation(); // Prevent event from bubbling up
    if (window.confirm('Are you sure you want to delete this vet?')) {
      onVetDeleted(event, vetId);
    }
  };

  return (
    <button onClick={handleDelete} className="btn btn-danger">
      Delete
    </button>
  );
};

export default DeleteVet;
