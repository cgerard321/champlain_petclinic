import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import { FormEvent, useState, useEffect } from 'react';
import { useUser } from '@/context/UserContext';
import './EditVisit.css';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel';
import { Status } from '@/features/visits/models/Status';
import { addVisit } from '@/features/visits/api/addVisit';
import { getAvailableVets, VetResponse } from '@/features/visits/api/getVets';
import {
  getAvailableSlots,
  TimeSlot,
} from '@/features/visits/api/getAvailableSlots';

interface ApiError {
  message: string;
}

interface TimeSlotWithVet extends TimeSlot {
  vetId?: string;
  vetName?: string;
}

type OwnerVisitType = {
  description: string;
  petId: string;
  practitionerId: string;
  selectedDate: string;
  selectedTimeSlot: string;
  assignedVetId: string;
  status: Status;
};

const OwnerBookingVisit: React.FC = (): JSX.Element => {
  const { user } = useUser();
  const [visit, setVisit] = useState<OwnerVisitType>({
    description: '',
    petId: '',
    practitionerId: 'no-preference',
    selectedDate: '',
    selectedTimeSlot: '',
    assignedVetId: '',
    status: 'UPCOMING' as Status,
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

  useEffect(() => {
    const fetchTimeSlots = async (): Promise<void> => {
      if (!visit.selectedDate) {
        setTimeSlots([]);
        return;
      }

      try {
        setLoadingSlots(true);
        setErrorMessage('');

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
        } else {
          const allSlots: TimeSlotWithVet[] = [];

          const slotPromises = vets.map(async vet => {
            try {
              const response = await getAvailableSlots(
                vet.vetId,
                visit.selectedDate
              );
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

          results.forEach(vetSlots => {
            allSlots.push(...vetSlots);
          });

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

  const handleCancel = (): void => {
    if (window.history.length > 1) {
      navigate(-1);
    } else {
      navigate('/customer/visits');
    }
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
      ownerId: user.userId,
      jwtToken:
        localStorage.getItem('authToken') ||
        localStorage.getItem('token') ||
        '',
    };

    try {
      await addVisit(formattedVisit);
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
      });
      setTimeout(() => {
        navigate('/customer/visits');
      }, 2000);
    } catch (error) {
      const apiError = error as ApiError;
      setErrorMessage(`Error adding visits: ${apiError.message}`);
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
      <div className="profile-edit">
        <h1>Schedule Visit For Your Pet</h1>
        <div className="loading">Loading veterinarians...</div>
      </div>
    );
  }

  const morningSlots = getMorningSlots();
  const afternoonSlots = getAfternoonSlots();

  return (
    <div className="profile-edit">
      <h1>Schedule Visit For Your Pet</h1>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label htmlFor="petId">Pet ID: </label>
          <input
            type="text"
            id="petId"
            name="petId"
            value={visit.petId}
            onChange={handleChange}
            placeholder="Enter pet ID"
          />
          {errors.petId && <span className="error">{errors.petId}</span>}
        </div>

        <div className="form-group">
          <label htmlFor="description">Description: </label>
          <textarea
            id="description"
            name="description"
            value={visit.description}
            onChange={handleChange}
            placeholder="Describe the reason for the visit..."
            rows={4}
          />
          {errors.description && (
            <span className="error">{errors.description}</span>
          )}
        </div>

        <div className="form-group">
          <label htmlFor="practitionerId">Veterinarian Preference: </label>
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

        <div className="form-group">
          <label htmlFor="selectedDate">Date: </label>
          <input
            type="date"
            id="selectedDate"
            name="selectedDate"
            value={visit.selectedDate}
            onChange={handleChange}
            min={new Date().toISOString().split('T')[0]}
          />
          {errors.selectedDate && (
            <span className="error">{errors.selectedDate}</span>
          )}
        </div>

        {visit.selectedDate && (
          <div className="form-group">
            <label>Available Time Slots: </label>

            {loadingSlots ? (
              <div className="loading-slots">
                {visit.practitionerId === 'no-preference'
                  ? 'Loading available times from all veterinarians...'
                  : 'Loading available times...'}
              </div>
            ) : timeSlots.length === 0 ? (
              <div className="no-slots">
                No available time slots for this date. Please select another
                date.
              </div>
            ) : (
              <>
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

            {errors.selectedTimeSlot && (
              <span className="error">{errors.selectedTimeSlot}</span>
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

        <div className="button-group"></div>
        <button className="cancel" type="button" onClick={handleCancel}>
          Cancel
        </button>
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? 'Adding...' : 'Add'}
        </button>
      </form>
      {showNotification && <div className="notification">{successMessage}</div>}
      {errorMessage && <div className="error">{errorMessage}</div>}
    </div>
  );
};

export default OwnerBookingVisit;
