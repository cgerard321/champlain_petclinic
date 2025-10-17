import * as React from 'react';
import { FormEvent, useState } from 'react';
import { PrescriptionRequestDTO } from '../models/PrescriptionRequestDTO';
import { MedicationDTO } from '../models/MedicationDTO';
import { createPrescription } from '../api/createPrescription';
import BasicModal from '@/shared/components/BasicModal';
import './PrescriptionForm.css';

interface PrescriptionModalProps {
  showButton: JSX.Element;
  visitId: string;
  vetFirstName: string;
  vetLastName: string;
  ownerFirstName: string;
  ownerLastName: string;
  petName: string;
  onClose?: () => void;
}

interface ApiError {
  message: string;
}

type PrescriptionType = {
  vetFirstName: string;
  vetLastName: string;
  ownerFirstName: string;
  ownerLastName: string;
  petName: string;
  directions: string;
  medications: MedicationDTO[];
};

const PrescriptionModal: React.FC<PrescriptionModalProps> = ({
  showButton,
  visitId,
  vetFirstName,
  vetLastName,
  ownerFirstName,
  ownerLastName,
  petName,
  onClose,
}): JSX.Element => {
  const [prescription, setPrescription] = useState<PrescriptionType>({
    vetFirstName,
    vetLastName,
    ownerFirstName,
    ownerLastName,
    petName,
    directions: '',
    medications: [
      {
        name: '',
        strength: '',
        dosage: '',
        frequency: '',
        quantity: 0,
      },
    ],
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);

  const validate = (): boolean => {
    const newErrors: Record<string, string> = {};
    if (!prescription.directions)
      newErrors.directions = 'Directions are required';

    prescription.medications.forEach((med, index) => {
      if (!med.name)
        newErrors[`medication${index}name`] = 'Medication name is required';
      if (!med.quantity || med.quantity <= 0)
        newErrors[`medication${index}quantity`] = 'Valid quantity is required';
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleAddMedication = (): void => {
    setPrescription(prev => ({
      ...prev,
      medications: [
        ...prev.medications,
        {
          name: '',
          strength: '',
          dosage: '',
          frequency: '',
          quantity: 0,
        },
      ],
    }));
  };

  const handleRemoveMedication = (index: number): void => {
    setPrescription(prev => ({
      ...prev,
      medications: prev.medications.filter((_, i) => i !== index),
    }));
  };

  const handleMedicationChange = (
    index: number,
    field: keyof MedicationDTO,
    value: string | number
  ): void => {
    setPrescription(prev => {
      const updatedMedications = [...prev.medications];
      updatedMedications[index] = {
        ...updatedMedications[index],
        [field]: field === 'quantity' ? Number(value) : value,
      };
      return { ...prev, medications: updatedMedications };
    });
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    const prescriptionRequest: PrescriptionRequestDTO = {
      ...prescription,
      date: new Date().toISOString(),
    };

    try {
      await createPrescription(visitId, prescriptionRequest);
      setSuccessMessage('Prescription created successfully!');
      setShowNotification(true);

      // ✅ Call onClose after a short delay (so the user can see the success message)
      setTimeout(() => {
        setShowNotification(false);
        if (onClose) onClose(); // ✅ use it here
      }, 1000);
    } catch (error) {
      const apiError = error as ApiError;
      setErrorMessage(`Error creating prescription: ${apiError.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <BasicModal
      title="Create Prescription"
      showButton={showButton}
      formId="prescriptionForm"
      validate={validate}
      confirmText={isLoading ? 'Creating...' : 'Create'}
      errorMessage={errorMessage}
    >
      <form id="prescriptionForm" onSubmit={handleSubmit}>
        {prescription.medications.map((medication, index) => (
          <div key={index}>
            <h4>Medication {index + 1}</h4>

            <label>
              Name:{' '}
              {errors[`medication${index}name`] && (
                <span className="error">
                  {errors[`medication${index}name`]}
                </span>
              )}
            </label>
            <input
              type="text"
              value={medication.name}
              onChange={e =>
                handleMedicationChange(index, 'name', e.target.value)
              }
              required
            />
            <br />

            <label>Strength:</label>
            <input
              type="text"
              value={medication.strength}
              onChange={e =>
                handleMedicationChange(index, 'strength', e.target.value)
              }
            />
            <br />

            <label>Dosage:</label>
            <input
              type="text"
              value={medication.dosage}
              onChange={e =>
                handleMedicationChange(index, 'dosage', e.target.value)
              }
            />
            <br />

            <label>Frequency:</label>
            <input
              type="text"
              value={medication.frequency}
              onChange={e =>
                handleMedicationChange(index, 'frequency', e.target.value)
              }
            />
            <br />

            <label>
              Quantity:{' '}
              {errors[`medication${index}quantity`] && (
                <span className="error">
                  {errors[`medication${index}quantity`]}
                </span>
              )}
            </label>
            <input
              type="number"
              value={medication.quantity}
              onChange={e =>
                handleMedicationChange(index, 'quantity', e.target.value)
              }
              required
              min="1"
            />
            <br />

            {prescription.medications.length > 1 && (
              <button
                type="button"
                onClick={() => handleRemoveMedication(index)}
                className="button"
              >
                Remove Medication
              </button>
            )}
            <br />
          </div>
        ))}

        <button type="button" onClick={handleAddMedication} className="button">
          Add Another Medication
        </button>
        <br />

        <label>
          Directions:{' '}
          {errors.directions && (
            <span className="error">{errors.directions}</span>
          )}
        </label>
        <textarea
          value={prescription.directions}
          onChange={e =>
            setPrescription(prev => ({ ...prev, directions: e.target.value }))
          }
          required
          rows={4}
        />
      </form>

      {showNotification && <div className="notification">{successMessage}</div>}
    </BasicModal>
  );
};

export default PrescriptionModal;
