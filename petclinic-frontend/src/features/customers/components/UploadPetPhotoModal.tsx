import { FormEvent, useState, useRef } from 'react';
import * as PropTypes from 'prop-types';
import { addPetPhoto } from '../api/addPetPhoto';
import './customers.css';
import { useTranslation } from 'react-i18next';



interface UploadPetPhotoModalProps {
  isOpen: boolean;
  onClose: () => void;
  petId: string;
  onPhotoUploaded: (petId: string) => void;
}

const UploadPetPhotoModal: React.FC<UploadPetPhotoModalProps> = ({
  petId,
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
        file: t('uploadPetPhotoModal.alert.photoRequired'),
      });
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setErrors({
        file: t('uploadPetPhotoModal.alert.photoSize'),
      });
      return;
    }

    setErrors({});
    setSelectedFile(file);

    const reader = new FileReader();
    reader.onload = e => setPreviewUrl(e.target?.result as string);
    reader.readAsDataURL(file);
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>): Promise<void> => {
    e.preventDefault();

    if (!selectedFile) {
      setErrors({
        file: t('uploadPetPhotoModal.alert.fileRequired'),
      });
      return;
    }

    setIsUploading(true);
    setErrors({});

    try {
      await addPetPhoto(petId, selectedFile);
      onPhotoUploaded(petId);
      onClose();
      setSelectedFile(null);
      setPreviewUrl('');
    } catch (error) {
      console.error('Error uploading pet photo:', error);
      setErrors({
        upload: t('uploadPetPhotoModal.alert.uploadFailed'),
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

  const handleButtonClick = (): void => fileInputRef.current?.click();

  return (
    <div className="modal-overlay" onClick={handleClose}>
      <div className="modal-content" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{t('uploadPetPhotoModal.title')}</h2>
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
                {t('uploadPetPhotoModal.selectPhoto')}
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
                  {t('uploadPetPhotoModal.chooseFile')}
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
              <label>{t('uploadPetPhotoModal.preview')}</label>
              <div className="image-preview">
                <img
                  src={previewUrl}
                  alt={t('uploadPetPhotoModal.preview')}
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
              {t('uploadPetPhotoModal.cancelButton')}
            </button>
            <button
              type="submit"
              disabled={!selectedFile || isUploading}
              className={`submit-button ${!selectedFile || isUploading ? 'disabled' : ''}`}
            >
              {isUploading
                  ? t('uploadPetPhotoModal.uploading')
                  : t('uploadPetPhotoModal.uploadPhoto')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

UploadPetPhotoModal.propTypes = {
  petId: PropTypes.string.isRequired,
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onPhotoUploaded: PropTypes.func.isRequired,
};

export default UploadPetPhotoModal;
