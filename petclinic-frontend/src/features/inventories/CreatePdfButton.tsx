import * as React from 'react';
import createPdf from './api/createPdf';

interface CreatePdfButtonProps {
  inventoryId: string;
}

const CreatePdfButton: React.FC<CreatePdfButtonProps> = ({ inventoryId }) => {
  const [loading, setLoading] = React.useState(false);
  const [errorMsg, setErrorMsg] = React.useState<string>('');

  const handleClick = async (): Promise<void> => {
    if (loading) return;
    setLoading(true);
    setErrorMsg('');

    const { errorMessage } = await createPdf(inventoryId);
    if (errorMessage) {
      setErrorMsg(errorMessage);
      alert(errorMessage);
    }
    setLoading(false);
  };

  return (
    <>
      <button
        className="btn btn-primary"
        onClick={handleClick}
        disabled={loading}
      >
        {loading ? 'Downloading…' : 'Download PDF'}
      </button>
      {errorMsg && (
        <div className="field-error" style={{ marginTop: 8 }}>
          {errorMsg}
        </div>
      )}
    </>
  );
};

export default CreatePdfButton;
