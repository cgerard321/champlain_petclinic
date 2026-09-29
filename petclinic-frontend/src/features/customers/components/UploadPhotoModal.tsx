import { FormEvent, useState, useRef } from 'react';
import * as PropTypes from 'prop-types';
import { uploadCustomerPhoto } from '../api/uploadCustomerPhoto.ts';
import './customers.css';
import { useTranslation } from 'react-i18next';



interface UploadPhotoModalProps {
  customerId: string;
  isOpen: boolean;
  onClose: () => void;
  onPhotoUploaded: () => void;
}

const UploadPhotoModal: React.FC<UploadPhotoModalProps> = ({
  customerId,
  isOpen,
  onClose,
  onPhotoUploaded,
}): JSX.Element | null => {
  const { t } = useTranslation('customers');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [previewUrl, setPreviewUrl] = useState<string>('');
  const [isUploading, setIsUploading] = useState(false);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const fileInputRef = useRef<HTMLInputElement>(null);

  if (!isOpen) return null;

  const handleFileChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ): void => {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      setErrors({
        file: t('uploadPhotoModal.errors.invalidFileType'),
      });
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setErrors({
        file: t('uploadPhotoModal.errors.fileTooLarge'),
      });
      return;
    }

    setErrors({});
    setSelectedFile(file);

    const reader = new FileReader();
    reader.onload = e => {
      setPreviewUrl(e.target?.result as string);
    };
    reader.readAsDataURL(file);
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>): Promise<void> => {
    e.preventDefault();

    if (!selectedFile) {
      setErrors({
        file: t('uploadPhotoModal.errors.noFileSelected'),
      });
      return;
    }

    setIsUploading(true);
    setErrors({});

    try {
      await uploadCustomerPhoto(customerId, selectedFile);
      onPhotoUploaded();
      onClose();
      setSelectedFile(null);
      setPreviewUrl('');
    } catch (error) {
      console.error('Error uploading photo:', error);
      setErrors({
        upload: t('uploadPhotoModal.errors.uploadFailed'),
      });
    } finally {
      setIsUploading(false);
    }
  };

  const handleClose = (): void => {
    if (!isUploading) {
      onClose();
      setSelectedFile(null);
      setPreviewUrl('');
      setErrors({});
    }
  };

  const handleButtonClick = (): void => {
    fileInputRef.current?.click();
  };

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{t('uploadPhotoModal.title')}</h2>
          <button
            type="button"
            className="close-button"
            onClick={handleClose}
            disabled={isUploading}
          >
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-body">
          <div className="form-group">
            <div className="file-input-row">
              <label htmlFor="photo-upload">
                {t('uploadPhotoModal.selectPhoto')}
              </label>
              <div className="file-input-wrapper">
                <input
                  ref={fileInputRef}
                  type="file"
                  id="photo-upload"
                  accept="image/*"
                  onChange={handleFileChange}
                  style={{ display: 'none' }}
                  disabled={isUploading}
                />
                <button
                  type="button"
                  onClick={handleButtonClick}
                  disabled={isUploading}
                  className="file-select-button"
                >
                  {t('uploadPhotoModal.chooseFile')}
                </button>
                {selectedFile && (
                  <span className="file-name" title={selectedFile.name}>
                    {selectedFile.name}
                  </span>
                )}
              </div>
            </div>
            {errors.file && <div className="error-message">{errors.file}</div>}
          </div>

          {previewUrl && (
            <div className="form-group">
              <label>{t('uploadPhotoModal.preview')}</label>
              <div className="image-preview">
                <img
                  src={previewUrl}
                  alt="Preview"
                  style={{
                    maxWidth: '200px',
                    maxHeight: '200px',
                    objectFit: 'cover',
                    borderRadius: '8px',
                    border: '1px solid #ddd',
                  }}
                />
              </div>
            </div>
          )}

          {errors.upload && (
            <div className="error-message">{errors.upload}</div>
          )}

          <div className="modal-footer">
            <button
              type="button"
              onClick={handleClose}
              disabled={isUploading}
              className="cancel-button"
            >
              {t('uploadPhotoModal.cancel')}
            </button>
            <button
              type="submit"
              disabled={!selectedFile || isUploading}
              className={`submit-button ${!selectedFile || isUploading ? 'disabled' : ''}`}
            >
              {isUploading
                  ? t('uploadPhotoModal.uploading')
                  : t('uploadPhotoModal.uploadPhoto')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

UploadPhotoModal.propTypes = {
  customerId: PropTypes.string.isRequired,
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onPhotoUploaded: PropTypes.func.isRequired,
};

export default UploadPhotoModal;
