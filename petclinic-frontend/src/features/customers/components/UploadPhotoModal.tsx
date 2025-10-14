import { FormEvent, useState, useRef } from 'react';
import * as PropTypes from 'prop-types';
import { uploadOwnerPhoto } from '../api/uploadOwnerPhoto';
import './customers.css';

interface UploadPhotoModalProps {
  ownerId: string;
  isOpen: boolean;
  onClose: () => void;
  onPhotoUploaded: () => void;
}

const UploadPhotoModal: React.FC<UploadPhotoModalProps> = ({
  ownerId,
  isOpen,
  onClose,
  onPhotoUploaded,
}): JSX.Element | null => {
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
      setErrors({ file: 'Please select an image file (JPG, PNG, etc.)' });
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setErrors({ file: 'File size must be less than 5MB' });
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
      setErrors({ file: 'Please select a file' });
      return;
    }

    setIsUploading(true);
    setErrors({});

    try {
      await uploadOwnerPhoto(ownerId, selectedFile);
      onPhotoUploaded();
      onClose();
      setSelectedFile(null);
      setPreviewUrl('');
    } catch (error) {
      console.error('Error uploading photo:', error);
      setErrors({ upload: 'Failed to upload photo. Please try again.' });
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
          <h2>Upload Profile Photo</h2>
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
              <label htmlFor="photo-upload">Select Photo:</label>
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
                  Choose File
                </button>
                {selectedFile && (
                  <span className="file-name">{selectedFile.name}</span>
                )}
              </div>
            </div>
            {errors.file && <div className="error-message">{errors.file}</div>}
          </div>

          {previewUrl && (
            <div className="form-group">
              <label>Preview:</label>
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
              Cancel
            </button>
            <button
              type="submit"
              disabled={!selectedFile || isUploading}
              className={`submit-button ${!selectedFile || isUploading ? 'disabled' : ''}`}
            >
              {isUploading ? 'Uploading...' : 'Upload Photo'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

UploadPhotoModal.propTypes = {
  ownerId: PropTypes.string.isRequired,
  isOpen: PropTypes.bool.isRequired,
  onClose: PropTypes.func.isRequired,
  onPhotoUploaded: PropTypes.func.isRequired,
};

export default UploadPhotoModal;
