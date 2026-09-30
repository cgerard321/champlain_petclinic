import { useRef, useState } from 'react';
import * as PropTypes from 'prop-types';
import { uploadCustomerPhoto } from '../api/uploadCustomerPhoto.ts';
import { useTranslation } from 'react-i18next';

interface UploadProfilePhotoProps {
  customerId: string;
  onPhotoUploaded: () => void;
  disabled?: boolean;
}

const UploadProfilePhoto: React.FC<UploadProfilePhotoProps> = ({
  customerId,
  onPhotoUploaded,
  disabled = false,
}) => {
  const { t } = useTranslation('customers');
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isUploading, setIsUploading] = useState(false);

  const handleFileSelect = async (
    event: React.ChangeEvent<HTMLInputElement>
  ): Promise<void> => {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      alert(t('uploadProfilePhoto.errors.invalidFileType'));
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      alert(t('uploadProfilePhoto.errors.fileTooLarge'));
      return;
    }

    setIsUploading(true);
    try {
      await uploadCustomerPhoto(customerId, file);
      onPhotoUploaded();
    } catch (error) {
      console.error('Error uploading photo:', error);
      alert(t('uploadProfilePhoto.errors.uploadFailed'));
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
        {isUploading
          ? t('uploadProfilePhoto.uploading')
          : t('uploadProfilePhoto.changePhoto')}
      </button>
    </>
  );
};

UploadProfilePhoto.propTypes = {
  customerId: PropTypes.string.isRequired,
  onPhotoUploaded: PropTypes.func.isRequired,
  disabled: PropTypes.bool,
};

export default UploadProfilePhoto;
