import { useRef, useState } from 'react';
import * as PropTypes from 'prop-types';
import { uploadOwnerPhoto } from '../api/uploadOwnerPhoto';

interface UploadProfilePhotoProps {
  ownerId: string;
  onPhotoUploaded: () => void;
  disabled?: boolean;
}

const UploadProfilePhoto: React.FC<UploadProfilePhotoProps> = ({
  ownerId,
  onPhotoUploaded,
  disabled = false,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isUploading, setIsUploading] = useState(false);

  const handleFileSelect = async (
    event: React.ChangeEvent<HTMLInputElement>
  ): Promise<void> => {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      alert('Please select an image file (JPG, PNG, etc.)');
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      alert('File size must be less than 5MB');
      return;
    }

    setIsUploading(true);
    try {
      await uploadOwnerPhoto(ownerId, file);
      onPhotoUploaded();
    } catch (error) {
      console.error('Error uploading photo:', error);
      alert('Failed to upload photo. Please try again.');
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  const handleButtonClick = (): void => {
    if (!disabled && !isUploading) {
      fileInputRef.current?.click();
    }
  };

  return (
    <>
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        onChange={handleFileSelect}
        style={{ display: 'none' }}
      />
      <button
        onClick={handleButtonClick}
        disabled={disabled || isUploading}
        style={{
          padding: '8px 16px',
          backgroundColor: disabled ? '#ccc' : '#007bff',
          color: 'white',
          border: 'none',
          borderRadius: '4px',
          cursor: disabled ? 'not-allowed' : 'pointer',
          fontSize: '14px',
          marginTop: '8px',
        }}
      >
        {isUploading ? 'Uploading...' : 'Change Photo'}
      </button>
    </>
  );
};

UploadProfilePhoto.propTypes = {
  ownerId: PropTypes.string.isRequired,
  onPhotoUploaded: PropTypes.func.isRequired,
  disabled: PropTypes.bool,
};

export default UploadProfilePhoto;
