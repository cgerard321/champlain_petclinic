import * as React from 'react';
import { FormEvent, useState, useEffect, useRef } from 'react';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel';
import { Status } from '@/features/visits/models/Status';
import { VisitResponseModel } from '../models/VisitResponseModel';
import { getVisit } from '../api/getVisit';
import { updateVisit } from '../api/updateVisit';
import { getAvailableVets, VetResponse } from '@/features/visits/api/getVets';
import { /*useUser,*/ IsVet, IsAdmin } from '@/context/UserContext';

import BasicModal from '@/shared/components/BasicModal';
import PrescriptionModal from '@/features/visits/Prescription/prescriptionComponents/prescriptionModal';
import CreateBillModal from './CreateBillModal';

import './EditVisit.css';

interface EditingVisitProps {
  showButton: JSX.Element;
  visitId: string;
}

interface ApiError {
  message: string;
}

type VisitType = {
  visitStartDate: Date;
  description: string;
  petId: string;
  petName: string;
  practitionerId: string;
  isEmergency: boolean;
  status: Status;
  ownerFirstName?: string;
  ownerLastName?: string;
};

const formatDate = (date: Date): string => {
  const pad = (n: number): string => n.toString().padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
};

const EditingVisit: React.FC<EditingVisitProps> = ({
  showButton,
  visitId,
}): JSX.Element => {
  const [visit, setVisit] = useState<VisitType>({
    visitStartDate: new Date(),
    description: '',
    petId: '',
    petName: '',
    practitionerId: '',
    isEmergency: false,
    status: 'UPCOMING' as Status,
  });

  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);

  const [vets, setVets] = useState<VetResponse[]>([]);
  const [showPrescriptionModal, setShowPrescriptionModal] =
    useState<boolean>(false);
  const [showCreateBillModal, setShowCreateBillModal] =
    useState<boolean>(false);
  const prescriptionTriggerRef = useRef<HTMLButtonElement | null>(null);
  const createBillTriggerRef = useRef<HTMLButtonElement | null>(null);
  //const { user } = useUser();
  const isVet = IsVet();
  const isAdmin = typeof IsAdmin === 'function' ? IsAdmin() : false;

  useEffect(() => {
    const fetchVisitData = async (): Promise<void> => {
      if (visitId) {
        try {
          const response: VisitResponseModel = await getVisit(visitId);
          setVisit({
            practitionerId: response.practitionerId,
            description: response.description,
            petId: response.petId,
            petName: response.petName,
            visitStartDate: new Date(response.visitDate),
            status: response.status,
            isEmergency: response.isEmergency,
          });
        } catch (error) {
          console.error(`Error fetching visit with ID ${visitId}:`, error);
        }
      }
    };

    if (visitId) {
      fetchVisitData();
    }
  }, [visitId]);

  //fetch vets
  useEffect(() => {
    const fetchVets = async (): Promise<void> => {
      try {
        const vetsResponse = await getAvailableVets();
        const activeVets = Array.isArray(vetsResponse)
          ? vetsResponse.filter(vet => vet.active)
          : [];
        setVets(activeVets);
      } catch (error) {
        const apiError = error as ApiError;
        setErrorMessage(`Error loading vets: ${apiError.message}`);
      }
    };

    fetchVets();
  }, []);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, value } = e.target;
    setVisit(prevVisit => ({
      ...prevVisit,
      [name]: name === 'visitStartDate' ? new Date(value) : value,
    }));
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!visit.petId) newErrors.petId = 'Pet ID is required';
    if (!visit.visitStartDate)
      newErrors.visitStartDate = 'Visit date is required';
    if (!visit.description) newErrors.description = 'Description is required';
    if (!visit.practitionerId)
      newErrors.practitionerId = 'Practitioner ID is required';
    if (!visit.status) newErrors.status = 'Status is required';
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate()) return;

    setIsLoading(true);
    setErrorMessage('');
    setSuccessMessage('');

    const formattedVisit: VisitRequestModel = {
      visitDate: visit.visitStartDate.toISOString(),
      description: visit.description,
      petId: visit.petId,
      practitionerId: visit.practitionerId,
      isEmergency: visit.isEmergency,
      status: visit.status,
    };

    try {
      if (visitId) {
        await updateVisit(visitId, formattedVisit);
        setSuccessMessage('Visit updated successfully!');
        setShowNotification(true);
        setTimeout(() => setShowNotification(false), 3000);
        setTimeout(() => {
          window.location.reload();
        }, 1000);
      }
    } catch (error) {
      const apiError = error as ApiError;
      setErrorMessage(`Error updating visit: ${apiError.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  const handlePrescriptionClick = (): void => {
    setShowPrescriptionModal(true);
    setTimeout(() => {
      prescriptionTriggerRef.current?.click();
    }, 0);
  };

  const handleCreateBillClick = (): void => {
    // open the modal via the hidden trigger to avoid nested form submission
    setShowCreateBillModal(true);
    setTimeout(() => {
      createBillTriggerRef.current?.click();
    }, 0);
  };

  return (
    <>
      <BasicModal
        title="Edit Visit"
        showButton={showButton}
        formId="modalform"
        validate={validate}
        confirmText={isLoading ? 'Updating...' : 'Update'}
        errorMessage={errorMessage}
      >
        <form id="modalform" onSubmit={handleSubmit}>
          <label>Pet Name:</label>
          <input disabled={true} value={visit.petName}></input>
          <br />
          <label>
            Description:{' '}
            {errors.description && (
              <span className="error">{errors.description}</span>
            )}
          </label>

          <input
            type="text"
            name="description"
            value={visit.description}
            onChange={handleChange}
            required
          />

          <br />
          <div className="form-group">
            <label htmlFor="practitionerId">Veterinarian Preference:</label>
            <select
              id="practitionerId"
              name="practitionerId"
              value={visit.practitionerId}
              onChange={handleChange}
            >
              {vets.map(vet => (
                <option key={vet.vetId} value={vet.vetId}>
                  Dr. {vet.firstName} {vet.lastName}
                  {vet.specialties &&
                    vet.specialties.length > 0 &&
                    ` (${vet.specialties.map(s => s.name).join(', ')})`}
                </option>
              ))}
            </select>
          </div>
          <br />
          <label>
            Visit Date:{' '}
            {errors.visitStartDate && (
              <span className="error">{errors.visitStartDate}</span>
            )}
            Visit Date:{' '}
            {errors.visitStartDate && <span className="error">Required</span>}
          </label>

          <input
            type="datetime-local"
            name="visitStartDate"
            value={formatDate(visit.visitStartDate)}
            onChange={handleChange}
            required
          />
          <br />
          <label>
            Status:{' '}
            {errors.status && <span className="error">{errors.status}</span>}
          </label>

          <select name="status" value={visit.status} onChange={handleChange}>
            <option value="CONFIRMED">Confirmed</option>
            <option value="UPCOMING">Upcoming</option>
            <option value="COMPLETED">Completed</option>
          </select>
          <br />
          <div className="form-group emergency-toggle-group">
            <label htmlFor="isEmergency">
              <span className="emergency-label-text">
                <span>Emergency Visit</span>
              </span>
              <div className="switch-wrapper">
                <input
                  type="checkbox"
                  id="isEmergency"
                  name="isEmergency"
                  checked={visit.isEmergency}
                  onChange={e =>
                    setVisit(prev => ({
                      ...prev,
                      isEmergency: e.target.checked,
                    }))
                  }
                  className="switch-input"
                />
                <span className="switch-slider"></span>
              </div>
            </label>
          </div>
          <br />
          {visit.status === 'COMPLETED' && (isVet || isAdmin) && (
            <div style={{ display: 'flex', gap: '8px' }}>
              <button
                type="button"
                onClick={handlePrescriptionClick}
                className="button"
              >
                Create Prescription
              </button>

              <button
                type="button"
                onClick={handleCreateBillClick}
                className="button"
              >
                Create Bill
              </button>
            </div>
          )}
        </form>
        {showNotification && (
          <div className="notification">{successMessage}</div>
        )}
      </BasicModal>

      <CreateBillModal
        showButton={
          <button
            ref={createBillTriggerRef}
            style={{ display: 'none' }}
            aria-hidden="true"
            tabIndex={-1}
            type="button"
          />
        }
        visitId={visitId}
        vetId={visit.practitionerId}
        vetFirstName={
          vets.find(v => v.vetId === visit.practitionerId)?.firstName || ''
        }
        vetLastName={
          vets.find(v => v.vetId === visit.practitionerId)?.lastName || ''
        }
        ownerFirstName={visit.ownerFirstName || ''}
        ownerLastName={visit.ownerLastName || ''}
        visitDate={
          visit.visitStartDate instanceof Date
            ? visit.visitStartDate.toISOString()
            : visit.visitStartDate
        }
      />

      {showPrescriptionModal && (
        <PrescriptionModal
          showButton={
            <button
              ref={prescriptionTriggerRef}
              style={{ display: 'none' }}
              aria-hidden="true"
              tabIndex={-1}
            />
          }
          visitId={visitId}
          vetFirstName={
            vets.find(vet => vet.vetId === visit.practitionerId)?.firstName ||
            ''
          }
          vetLastName={
            vets.find(vet => vet.vetId === visit.practitionerId)?.lastName || ''
          }
          ownerFirstName={visit.ownerFirstName || ''}
          ownerLastName={visit.ownerLastName || ''}
          petName={visit.petName}
        />
      )}
    </>
  );
};

export default EditingVisit;
