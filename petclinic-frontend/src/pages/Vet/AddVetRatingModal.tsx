import { FormEvent, useMemo, useState } from 'react';
import { addVetRating } from '@/features/veterinarians/api/addVetRating';
import {
  PREDEFINED_DESCRIPTIONS,
  PredefinedDescription,
} from '@/features/veterinarians/models/PredefinedDescription';
import { useUser } from '@/context/UserContext';
import './AddVetRatingModal.css';

interface AddVetRatingModalProps {
  vetId: string;
  onClose: () => void;
  onSubmitSuccess: () => void;
}

interface RatingFormState {
  rateScore: string;
  rateDescription: string;
  predefinedDescription: PredefinedDescription | '';
  customerName: string;
}

const initialErrors = {
  rateScore: '',
  rateDescription: '',
  customerName: '',
  general: '',
};

const AddVetRatingModal = ({
  vetId,
  onClose,
  onSubmitSuccess,
}: AddVetRatingModalProps): JSX.Element => {
  const { user } = useUser();

  const defaultCustomerName = useMemo(
    () => user?.username?.trim() || user?.email?.trim() || '',
    [user]
  );

  const [formState, setFormState] = useState<RatingFormState>({
    rateScore: '',
    rateDescription: '',
    predefinedDescription: '',
    customerName: defaultCustomerName,
  });
  const [errors, setErrors] = useState({ ...initialErrors });
  const [isSubmitting, setIsSubmitting] = useState(false);

  const resetModal = (): void => {
    setFormState({
      rateScore: '',
      rateDescription: '',
      predefinedDescription: '',
      customerName: defaultCustomerName,
    });
    setErrors({ ...initialErrors });
  };

  const closeModal = (): void => {
    if (isSubmitting) return;
    resetModal();
    onClose();
  };

  const handleInputChange = (
    event: React.ChangeEvent<
      HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement
    >
  ): void => {
    const { name, value } = event.target;
    setFormState(prev => ({
      ...prev,
      [name]: value,
    }));

    if (errors[name as keyof typeof errors]) {
      setErrors(prevErrors => ({
        ...prevErrors,
        [name]: '',
      }));
    }
  };

  const validate = (): boolean => {
    const newErrors = { ...initialErrors };
    const score = Number(formState.rateScore);

    if (!formState.customerName.trim()) {
      newErrors.customerName = 'Name is required.';
    }

    if (Number.isNaN(score)) {
      newErrors.rateScore = 'Rating must be a number between 1 and 5.';
    } else if (score < 1 || score > 5) {
      newErrors.rateScore = 'Rating must be between 1 and 5.';
    }

    if (!formState.rateDescription.trim()) {
      newErrors.rateDescription = 'Description is required.';
    }

    setErrors(newErrors);
    return (
      !newErrors.rateScore &&
      !newErrors.rateDescription &&
      !newErrors.customerName
    );
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    setErrors({ ...initialErrors });

    if (!validate()) {
      return;
    }

    setIsSubmitting(true);

    try {
      await addVetRating({
        vetId,
        rateScore: Number(formState.rateScore),
        rateDescription: formState.rateDescription.trim(),
        predefinedDescription: formState.predefinedDescription || undefined,
        customerName: formState.customerName.trim(),
        rateDate: new Date().toISOString(),
      });

      onSubmitSuccess();
      resetModal();
      onClose();
    } catch (error) {
      console.error('Failed to submit rating:', error);
      setErrors(prev => ({
        ...prev,
        general: 'Unable to submit rating. Please try again.',
      }));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="rating-modal-overlay" role="dialog" aria-modal="true">
      <div className="rating-modal">
        <header className="rating-modal-header">
          <h2>Add Your Review</h2>
          <button
            className="rating-modal-close"
            onClick={closeModal}
            type="button"
            aria-label="Close add review form"
          >
            ×
          </button>
        </header>

        <form className="rating-form" onSubmit={handleSubmit}>
          {errors.general && (
            <div className="rating-form-error" role="alert">
              {errors.general}
            </div>
          )}

          <div className="rating-form-group">
            <label htmlFor="customerName">Your Name</label>
            <input
              id="customerName"
              name="customerName"
              type="text"
              value={formState.customerName}
              onChange={handleInputChange}
              placeholder="Enter your name"
              disabled={isSubmitting}
            />
            {errors.customerName && (
              <span className="rating-form-error">{errors.customerName}</span>
            )}
          </div>

          <div className="rating-form-group">
            <label htmlFor="rateScore">Rating (1-5)</label>
            <input
              id="rateScore"
              name="rateScore"
              type="number"
              min={1}
              max={5}
              step={1}
              value={formState.rateScore}
              onChange={handleInputChange}
              placeholder="Choose a rating"
              disabled={isSubmitting}
            />
            {errors.rateScore && (
              <span className="rating-form-error">{errors.rateScore}</span>
            )}
          </div>

          <div className="rating-form-group">
            <label htmlFor="predefinedDescription">Overall Experience</label>
            <select
              id="predefinedDescription"
              name="predefinedDescription"
              value={formState.predefinedDescription}
              onChange={handleInputChange}
              disabled={isSubmitting}
            >
              <option value="">Select an option</option>
              {PREDEFINED_DESCRIPTIONS.map(option => (
                <option key={option} value={option}>
                  {option.charAt(0).toUpperCase() +
                    option.slice(1).toLowerCase()}
                </option>
              ))}
            </select>
          </div>

          <div className="rating-form-group">
            <label htmlFor="rateDescription">Tell us about your visit</label>
            <textarea
              id="rateDescription"
              name="rateDescription"
              value={formState.rateDescription}
              onChange={handleInputChange}
              placeholder="Share details about your experience with this veterinarian"
              rows={4}
              disabled={isSubmitting}
            />
            {errors.rateDescription && (
              <span className="rating-form-error">
                {errors.rateDescription}
              </span>
            )}
          </div>

          <div className="rating-form-actions">
            <button
              type="button"
              className="rating-cancel-button"
              onClick={closeModal}
              disabled={isSubmitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="rating-submit-button"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Submitting…' : 'Submit Review'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default AddVetRatingModal;
