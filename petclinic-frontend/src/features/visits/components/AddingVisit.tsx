import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { FormEvent, useState, useEffect } from 'react';
import './EditVisit.css';
import { Status } from '@/features/visits/models/Status';
import { addVisit } from '@/features/visits/api/addVisit';
import { getAvailableVets, VetResponse } from '@/features/visits/api/getVets';
import {
  getAvailableSlots,
  TimeSlot,
} from '@/features/visits/api/getAvailableSlots';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel';
import BasicModal from '@/shared/components/BasicModal';

interface ApiError {
  message: string;
}

interface AddingVisitProps {
  showButton: JSX.Element;
}

// Add vetId and vetName to TimeSlot for tracking
interface TimeSlotWithVet extends TimeSlot {
  vetId?: string;
  vetName?: string;
}

type VisitType = {
  description: string;
  petId: string;
  practitionerId: string;
  selectedDate: string;
  selectedTimeSlot: string;
  assignedVetId: string;
  status: Status;
  isEmergency: boolean;
};

const AddingVisit: React.FC<AddingVisitProps> = ({
  showButton,
}): JSX.Element => {
  const [visit, setVisit] = useState<VisitType>({
    description: '',
    petId: '',
    practitionerId: 'no-preference',
    selectedDate: '',
    selectedTimeSlot: '',
    assignedVetId: '',
    status: 'UPCOMING' as Status,
    isEmergency: false,
  });

  const [vets, setVets] = useState<VetResponse[]>([]);
  const [timeSlots, setTimeSlots] = useState<TimeSlotWithVet[]>([]);
  const [loadingVets, setLoadingVets] = useState<boolean>(true);
  const [loadingSlots, setLoadingSlots] = useState<boolean>(false);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);

  const navigate = useNavigate();
  //fetch vets
  useEffect(() => {
    const fetchVets = async (): Promise<void> => {
      try {
        setLoadingVets(true);
        const vetsResponse = await getAvailableVets();
        const activeVets = Array.isArray(vetsResponse)
          ? vetsResponse.filter(vet => vet.active)
          : [];
        setVets(activeVets);
      } catch (error) {
        const apiError = error as ApiError;
        setErrorMessage(`Error loading vets: ${apiError.message}`);
      } finally {
        setLoadingVets(false);
      }
    };

    fetchVets();
  }, []);

  // Fetch time slots when vet AND date are selected
  useEffect(() => {
    const fetchTimeSlots = async (): Promise<void> => {
      // Need date to fetch slots
      if (!visit.selectedDate) {
        setTimeSlots([]);
        return;
      }

      try {
        setLoadingSlots(true);
        setErrorMessage('');

        // Specific vet selected - fetch only their slots
        if (visit.practitionerId !== 'no-preference') {
          const response = await getAvailableSlots(
            visit.practitionerId,
            visit.selectedDate
          );

          const vet = vets.find(v => v.vetId === visit.practitionerId);
          const slotsWithVet: TimeSlotWithVet[] = response.data.map(slot => ({
            ...slot,
            vetId: visit.practitionerId,
            vetName: vet ? `Dr. ${vet.firstName} ${vet.lastName}` : 'Unknown',
          }));

          setTimeSlots(slotsWithVet);

          if (response.data.length === 0) {
            setErrorMessage('No available slots for this vet on this date');
          }
        }
        // "No Preference" - fetch slots from ALL vets and combine
        else {
          const allSlots: TimeSlotWithVet[] = [];

          // Fetch slots from each vet
          const slotPromises = vets.map(async vet => {
            try {
              const response = await getAvailableSlots(
                vet.vetId,
                visit.selectedDate
              );
              // Add vet info to each slot
              return response.data.map(slot => ({
                ...slot,
                vetId: vet.vetId,
                vetName: `Dr. ${vet.firstName} ${vet.lastName}`,
              }));
            } catch (error) {
              console.error(
                `Error fetching slots for vet ${vet.vetId}:`,
                error
              );
              return [];
            }
          });

          const results = await Promise.all(slotPromises);

          // Flatten all slots
          results.forEach(vetSlots => {
            allSlots.push(...vetSlots);
          });

          // Remove duplicate time slots - keep only available ones
          const uniqueSlots = allSlots.reduce((acc, slot) => {
            const existingSlot = acc.find(s => s.startTime === slot.startTime);

            if (!existingSlot) {
              acc.push(slot);
            } else if (slot.available && !existingSlot.available) {
              const index = acc.indexOf(existingSlot);
              acc[index] = slot;
            }

            return acc;
          }, [] as TimeSlotWithVet[]);

          // Sort by time
          uniqueSlots.sort(
            (a, b) =>
              new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
          );

          setTimeSlots(uniqueSlots);

          if (uniqueSlots.length === 0) {
            setErrorMessage(
              'No available slots from any veterinarian on this date'
            );
          }
        }
      } catch (error) {
        console.error('Error fetching time slots:', error);
        const apiError = error as ApiError;
        setErrorMessage(`Error loading time slots: ${apiError.message}`);
        setTimeSlots([]);
      } finally {
        setLoadingSlots(false);
      }
    };

    fetchTimeSlots();
  }, [visit.practitionerId, visit.selectedDate, vets]);
  const handleChange = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
    >
  ): void => {
    const { name, value } = e.target;
    setVisit(prevVisit => ({
      ...prevVisit,
      [name]: value,
    }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }

    if (name === 'selectedDate' || name === 'practitionerId') {
      setVisit(prev => ({
        ...prev,
        selectedTimeSlot: '',
        assignedVetId: '',
      }));
    }
  };

  const handleTimeSlotSelect = (slot: TimeSlotWithVet): void => {
    setVisit(prev => ({
      ...prev,
      selectedTimeSlot: slot.startTime,
      assignedVetId: slot.vetId || '',
    }));
    if (errors.selectedTimeSlot) {
      setErrors(prev => ({ ...prev, selectedTimeSlot: '' }));
    }
  };

  const validate = (): boolean => {
    const newErrors: { [key: string]: string } = {};
    if (!visit.petId) newErrors.petId = 'Pet ID is required';
    if (!visit.description.trim())
      newErrors.description = 'Description is required';
    if (!visit.selectedDate) newErrors.selectedDate = 'Please select a date';
    if (!visit.selectedTimeSlot)
      newErrors.selectedTimeSlot = 'Please select a time slot';
    if (!visit.assignedVetId) {
      newErrors.assignedVetId = 'No vet assigned to this time slot';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!validate()) return;

    setIsSubmitting(true);
    setErrorMessage('');
    setSuccessMessage('');

    const formattedVisit: VisitRequestModel = {
      visitDate: visit.selectedTimeSlot,
      description: visit.description,
      petId: visit.petId,
      practitionerId: visit.assignedVetId,
      status: visit.status,
      isEmergency: visit.isEmergency,
    };

    try {
      await addVisit(formattedVisit); // Pass the Date object directly
      setSuccessMessage('Visit added successfully!');
      setShowNotification(true);
      setVisit({
        description: '',
        petId: '',
        practitionerId: 'no-preference',
        assignedVetId: '',
        selectedDate: '',
        selectedTimeSlot: '',
        status: 'UPCOMING' as Status,
        //visitEndDate: new Date(),
        isEmergency: false,
      });
      setTimeout(() => {
        navigate('/visits');
      }, 2000);
    } catch (error) {
      const apiError = error as ApiError;
      setErrorMessage(`Error adding visit: ${apiError.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatTimeSlot = (isoString: string): string => {
    const date = new Date(isoString);
    return date.toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true,
    });
  };

  const getMorningSlots = (): TimeSlotWithVet[] => {
    return timeSlots.filter(slot => {
      const hour = new Date(slot.startTime).getHours();
      return hour < 12 && slot.available;
    });
  };

  const getAfternoonSlots = (): TimeSlotWithVet[] => {
    return timeSlots.filter(slot => {
      const hour = new Date(slot.startTime).getHours();
      return hour >= 12 && slot.available;
    });
  };

  if (loadingVets) {
    return (
      <div>
        <div className="loading">Loading veterinarians...</div>
      </div>
    );
  }

  const morningSlots = getMorningSlots();
  const afternoonSlots = getAfternoonSlots();

  return (
    <BasicModal
      title="Schedule Visit For Your Pet"
      showButton={showButton}
      formId="addvisit"
      validate={validate}
      refreshPageOnConfirm={true}
      confirmText={isSubmitting ? 'Adding...' : 'Add'}
      errorMessage={errorMessage}
    >
      <form id="addvisit" onSubmit={handleSubmit}>
        <label htmlFor="petId">
          Pet ID: <span className="required">*</span>{' '}
          {errors.petId && <span className="error">{errors.petId}</span>}
        </label>
        <input
          type="text"
          id="petId"
          name="petId"
          value={visit.petId}
          onChange={handleChange}
          placeholder="Enter pet ID"
        />

        <br />
        <div className="form-group">
          <label htmlFor="description">
            Description: <span className="required">*</span>{' '}
            {errors.description && (
              <span className="error">{errors.description}</span>
            )}
          </label>
          <br />
          <br />
          <textarea
            id="description"
            name="description"
            value={visit.description}
            onChange={handleChange}
            placeholder="Describe the reason for the visit..."
            rows={4}
          />
        </div>
        <div className="form-group">
          <label htmlFor="practitionerId">Veterinarian Preference:</label>
          <select
            id="practitionerId"
            name="practitionerId"
            value={visit.practitionerId}
            onChange={handleChange}
          >
            <option value="no-preference">
              No Preference (Show All Available Times)
            </option>
            {vets.map(vet => (
              <option key={vet.vetId} value={vet.vetId}>
                Dr. {vet.firstName} {vet.lastName}
                {vet.specialties &&
                  vet.specialties.length > 0 &&
                  ` (${vet.specialties.map(s => s.name).join(', ')})`}
              </option>
            ))}
          </select>
          <small className="help-text">
            {visit.practitionerId === 'no-preference'
              ? 'Showing available slots from all veterinarians'
              : 'Showing slots only for selected veterinarian'}
          </small>
        </div>
        <br />
        <label htmlFor="selectedDate">
          Date: <span className="required">*</span>{' '}
          {errors.selectedDate && (
            <span className="error">{errors.selectedDate}</span>
          )}
        </label>
        <input
          type="date"
          id="selectedDate"
          name="selectedDate"
          value={visit.selectedDate}
          onChange={handleChange}
          min={new Date().toISOString().split('T')[0]}
        />
        <br />
        {visit.selectedDate && (
          <div className="form-group">
            <label>
              Available Time Slots:{' '}
              {errors.selectedTimeSlot && (
                <span className="error">{errors.selectedTimeSlot}</span>
              )}
            </label>

            {loadingSlots ? (
              <div className="loading-slots">
                {visit.practitionerId === 'no-preference'
                  ? 'Loading available times from all veterinarians...'
                  : 'Loading available times...'}
              </div>
            ) : timeSlots.length === 0 ? (
              <div className="no-slots">
                No available time slots for this date.
              </div>
            ) : (
              <>
                {/* Morning Slots */}
                {morningSlots.length > 0 && (
                  <div className="time-section">
                    <h4>Morning</h4>
                    <div className="time-slots-grid">
                      {morningSlots.map((slot, index) => (
                        <button
                          key={index}
                          type="button"
                          className={`time-slot ${
                            visit.selectedTimeSlot === slot.startTime
                              ? 'selected'
                              : ''
                          }`}
                          onClick={() => handleTimeSlotSelect(slot)}
                          title={slot.vetName}
                        >
                          <div className="slot-time">
                            {formatTimeSlot(slot.startTime)}
                          </div>
                          {visit.practitionerId === 'no-preference' && (
                            <div className="slot-vet">{slot.vetName}</div>
                          )}
                        </button>
                      ))}
                    </div>
                  </div>
                )}

                {/* Afternoon Slots */}
                {afternoonSlots.length > 0 && (
                  <div className="time-section">
                    <h4>Afternoon</h4>
                    <div className="time-slots-grid">
                      {afternoonSlots.map((slot, index) => (
                        <button
                          key={index}
                          type="button"
                          className={`time-slot ${
                            visit.selectedTimeSlot === slot.startTime
                              ? 'selected'
                              : ''
                          }`}
                          onClick={() => handleTimeSlotSelect(slot)}
                          title={slot.vetName}
                        >
                          <div className="slot-time">
                            {formatTimeSlot(slot.startTime)}
                          </div>
                          {visit.practitionerId === 'no-preference' && (
                            <div className="slot-vet">{slot.vetName}</div>
                          )}
                        </button>
                      ))}
                    </div>
                  </div>
                )}
              </>
            )}
          </div>
        )}

        {/* Show assigned vet when "No Preference" */}
        {visit.practitionerId === 'no-preference' && visit.assignedVetId && (
          <div className="assigned-vet-info">
            ✓ You will see:{' '}
            {
              timeSlots.find(s => s.startTime === visit.selectedTimeSlot)
                ?.vetName
            }
          </div>
        )}
      </form>
      {showNotification && <div className="notification">{successMessage}</div>}
    </BasicModal>
  );
};

export default AddingVisit;
