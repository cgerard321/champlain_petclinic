import * as React from 'react';
import createPdf from './api/createPdf';

interface CreatePdfButtonProps {
  inventoryId: string;
}

const CreatePdfButton: React.FC<CreatePdfButtonProps> = ({ inventoryId }) => {
  const handleClick = async (): Promise<void> => {
    await createPdf(inventoryId);
  };

  return (
    <button className="btn btn-primary" onClick={handleClick}>
      Download PDF
    </button>
  );
};

export default CreatePdfButton;
