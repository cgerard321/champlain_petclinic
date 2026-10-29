import * as React from 'react';
import { FormEvent, useState, useEffect } from 'react';
import './EditVisit.css';
import { Status } from '@/features/visits/models/Status';
import { addVisit } from '@/features/visits/api/addVisit';
import { getAvailableVets } from '@/features/visits/api/getVets';
import {
  getAvailableSlots,
  TimeSlot,
} from '@/features/visits/api/getAvailableSlots';
import { VisitRequestModel } from '@/features/visits/models/VisitRequestModel';
import BasicModal from '@/shared/components/BasicModal';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel';
import axiosInstance from '@/shared/api/axiosInstance';
import { useUser } from '@/context/UserContext';
import { getVetsAvailabilitiesGeneralEmergency } from '../api/getAvailabilityGeneralEmergency';
import EmergencyAvailability from '@/features/visits/models/EmergencyResponse';
import { getVetsAvailabilitiesSurgicalEmergency } from '../api/getVetsAvailabilitiesSurgicalEmergency';
import { getVetsAvailabilitiesDentalEmergency } from '../api/getVetsAvailabilitiesDentalEmergency';

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

type EmergencyType = 'general' | 'surgical' | 'dental' | 'radiology';

const AddingVisit: React.FC<AddingVisitProps> = ({ showButton }) => {
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

  const [pets, setPets] = useState<PetResponseModel[]>([]);
  const [loadingPets, setLoadingPets] = useState<boolean>(true);
  const [vets, setVets] = useState<
    Awaited<ReturnType<typeof getAvailableVets>>
  >([]);
  const [timeSlots, setTimeSlots] = useState<TimeSlotWithVet[]>([]);
  const [loadingVets, setLoadingVets] = useState<boolean>(true);
  const [loadingSlots, setLoadingSlots] = useState<boolean>(false);
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [showNotification, setShowNotification] = useState<boolean>(false);
  const { userId, isLoading } = useUser();
  const [emergencyType, setEmergencyType] = useState<EmergencyType | ''>('');
  const [emergencyAvailabilities, setEmergencyAvailabilities] = useState<
    EmergencyAvailability[]
  >([]);
  useEffect(() => {
    const loadEmergencyAvailabilities = async (): Promise<void> => {
      if (!visit.isEmergency || !emergencyType) {
        setEmergencyAvailabilities([]);
        return;
      }

      let response;

      if (emergencyType === 'general') {
        response = await getVetsAvailabilitiesGeneralEmergency();
      } else if (emergencyType === 'dental') {
        response = await getVetsAvailabilitiesDentalEmergency();
      } else if (emergencyType === 'surgical') {
        response = await getVetsAvailabilitiesSurgicalEmergency();
      } else {
        setEmergencyAvailabilities([]);
        return;
      }

      setEmergencyAvailabilities(response.data);
    };
    void loadEmergencyAvailabilities();
  }, [visit.isEmergency, emergencyType]);

  useEffect(() => {
    if (visit.isEmergency && !visit.selectedDate) {
      setVisit(prev => ({
        ...prev,
        selectedDate: getLocalDateString(new Date()),
      }));
    }
  }, [visit.isEmergency, visit.selectedDate]);

  useEffect(() => {
    if (!isLoading && userId) {
      console.warn('Current user ID:', userId);
    }
  }, [userId, isLoading]);

  //Fetch pets
  useEffect(() => {
    if (isLoading || !userId) return;

    const fetchPets = async (): Promise<void> => {
      try {
        setLoadingPets(true);
        const petsResponse = await axiosInstance.get(
          `/pets/customers/${userId}/pets`,
          { useV2: false }
        );

        let petsData: PetResponseModel[] = [];

        if (typeof petsResponse.data === 'string') {
          const pieces = petsResponse.data.split('\n').filter(Boolean);

          for (const piece of pieces) {
            if (piece.startsWith('data:')) {
              try {
                const pet = JSON.parse(
                  piece.slice(5).trim()
                ) as PetResponseModel;

                petsData.push(pet);
              } catch (error) {
                console.warn('Error parsing pet data:', error);
              }
            }
          }
        } else if (Array.isArray(petsResponse.data)) {
          petsData = petsResponse.data;
        }
        const petsDataActive = petsData.filter(pet => pet.isActive === 'true');
        console.warn(petsDataActive);
        setPets(petsDataActive);
      } catch (error) {
        console.error('Error fetching pets:', error);
        setErrorMessage('Failed to load pets. Please try again.');
      } finally {
        setLoadingPets(false);
      }
    };

    fetchPets();
  }, [userId, isLoading]);

  useEffect(() => {
    console.warn('Pets state updated:', pets);
  }, [pets]);

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

        //EMERGENCY
        if (visit.isEmergency) {
          const selectedAvailabilities = visit.practitionerId
            ? emergencyAvailabilities.filter(
                availability =>
                  availability.veterinarian.vetId === visit.practitionerId
              )
            : emergencyAvailabilities;

          const emergencySlots: TimeSlotWithVet[] =
            selectedAvailabilities.flatMap(availability =>
              availability.availableSlots.map(slot => ({
                ...slot,
                startTime: new Date(slot.startTime).toISOString(),
                endTime: new Date(slot.endTime).toISOString(),
                vetId: availability.veterinarian.vetId,
                vetName: `Dr. ${availability.veterinarian.firstName} ${availability.veterinarian.lastName}`,
              }))
            );

          const filteredSlots = emergencySlots.filter(slot =>
            slot.startTime.startsWith(visit.selectedDate)
          );

          setTimeSlots(filteredSlots);
          return;
        }

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
  }, [
    visit.practitionerId,
    visit.selectedDate,
    visit.isEmergency,
    emergencyType,
    emergencyAvailabilities,
    vets,
  ]);

  const getLocalDateString = (date: Date): string => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  };

  const changeSelectedDate = (days: number): void => {
    const baseDate = visit.selectedDate
      ? new Date(`${visit.selectedDate}T12:00:00`)
      : new Date();

    baseDate.setDate(baseDate.getDate() + days);

    const newDate = getLocalDateString(baseDate);
    const today = getLocalDateString(new Date());

    if (newDate < today) return;

    setVisit(prev => ({
      ...prev,
      selectedDate: newDate,
      selectedTimeSlot: '',
      assignedVetId: '',
    }));
  };

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
        window.location.reload();
      }, 1000);
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

  const morningSlots = getMorningSlots();
  const afternoonSlots = getAfternoonSlots();
  const emergencySlotsByVet = timeSlots.reduce<
    Record<string, TimeSlotWithVet[]>
  >((groups, slot) => {
    const vetId = slot.vetId;

    if (!vetId) return groups;

    if (!groups[vetId]) {
      groups[vetId] = [];
    }

    groups[vetId].push(slot);
    return groups;
  }, {});
  const allInactivePets = pets.every(p => p.isActive === 'false');
  const noPetsAssigned = pets.length === 0;

  //RETURN UI

  return (
    <BasicModal
      title="Schedule Visit For Your Pet"
      showButton={showButton}
      formId="addvisit"
      validate={validate}
      confirmText={isSubmitting ? 'Adding...' : 'Add'}
      errorMessage={errorMessage}
    >
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

      {loadingVets ? (
        <div>
          <div className="loading">Loading veterinarians...</div>
        </div>
      ) : (
        <form id="addvisit" onSubmit={handleSubmit}>
          <label htmlFor="petId">
            Select Pet: <span className="required">*</span>{' '}
            {errors.petId && <span className="error">{errors.petId}</span>}
          </label>
          {loadingPets ? (
            <p>Loading pets...</p>
          ) : allInactivePets ? (
            <p style={{ color: 'red' }}>You have no active pets </p>
          ) : noPetsAssigned ? (
            <p style={{ color: 'red' }}>No pet is assigned to you </p>
          ) : (
            <select
              id="petId"
              name="petId"
              value={visit.petId}
              onChange={handleChange}
              className={errors.petId ? 'error-input' : ''}
            >
              <option value="">Select a Pet</option>
              {pets.map(pet => (
                <option key={pet.petId} value={pet.petId}>
                  {pet.name} {pet.isActive === 'false' ? '(Inactive)' : ''}
                </option>
              ))}
            </select>
          )}
          <br></br>

          {visit.isEmergency && (
            <div className="form-group">
              <label htmlFor="emergencyType">
                Type of Emergency: <span className="required">*</span>
              </label>

              <select
                id="emergencyType"
                name="emergencyType"
                value={emergencyType}
                onChange={e => {
                  setEmergencyType(e.target.value as EmergencyType | '');
                  setVisit(prev => ({
                    ...prev,
                    practitionerId: '',
                    selectedDate: '',
                    selectedTimeSlot: '',
                    assignedVetId: '',
                  }));
                }}
              >
                <option value="">Select an emergency type</option>

                <option value="general">
                  General emergency (difficulty breathing, severe bleeding,
                  poisoning)
                </option>

                <option value="surgical">
                  Surgical emergency (deep wounds, internal injuries, swallowed
                  foreign objects)
                </option>

                <option value="dental">
                  Dental emergency (broken tooth, severe gum bleeding, facial
                  swelling)
                </option>

                <option value="radiology">
                  Radiology emergency ( suspected broken bone, chest injury,
                  possible internal injury)
                </option>
              </select>
            </div>
          )}

          <div className="form-group">
            <label htmlFor="description">
              Description: <span className="required">*</span>{' '}
              {errors.description && (
                <span className="error">{errors.description}</span>
              )}
            </label>
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
              {visit.isEmergency ? (
                <>
                  <option value="">
                    {' '}
                    No Preference (Show All Available Times)
                  </option>
                  {emergencyAvailabilities.map(availability => (
                    <option
                      key={availability.veterinarian.vetId}
                      value={availability.veterinarian.vetId}
                    >
                      Dr. {availability.veterinarian.firstName}{' '}
                      {availability.veterinarian.lastName}
                    </option>
                  ))}
                </>
              ) : (
                <>
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
                </>
              )}
            </select>
            <small className="help-text">
              {visit.practitionerId === 'no-preference'
                ? 'Showing available slots from all veterinarians'
                : 'Showing slots only for selected veterinarian'}
            </small>
          </div>
          {/* <label htmlFor="selectedDate">
            Date: <span className="required">*</span>{' '}
            {errors.selectedDate && (
              <span className="error">{errors.selectedDate}</span>
            )}
          </label> */}
          {visit.isEmergency ? (
            <div className="emergency-date-header">
              <button
                type="button"
                className="emergency-date-arrow"
                onClick={() => changeSelectedDate(-1)}
                disabled={
                  !visit.selectedDate ||
                  visit.selectedDate <= getLocalDateString(new Date())
                }
                aria-label="Previous day"
              >
                &lt;
              </button>

              <div className="emergency-date-title">
                <span>Today</span>
                <strong>
                  {visit.selectedDate &&
                    new Date(
                      `${visit.selectedDate}T12:00:00`
                    ).toLocaleDateString('en-US', {
                      weekday: 'short',
                      month: 'short',
                      day: 'numeric',
                    })}
                </strong>
              </div>

              <input
                type="date"
                id="selectedDate"
                name="selectedDate"
                value={visit.selectedDate}
                onChange={handleChange}
                min={getLocalDateString(new Date())}
              />

              <button
                type="button"
                className="emergency-date-arrow"
                onClick={() => changeSelectedDate(1)}
                disabled={!visit.selectedDate}
                aria-label="Next day"
              >
                &gt;
              </button>

              {errors.selectedDate && (
                <span className="error">{errors.selectedDate}</span>
              )}
            </div>
          ) : (
            <>
              <label htmlFor="selectedDate">
                Date: <span className="required">*</span>
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
            </>
          )}
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
              ) : visit.isEmergency ? (
                Object.entries(emergencySlotsByVet).map(([vetId, slots]) => (
                  <div
                    key={vetId}
                    className="time-section emergency-vet-section"
                  >
                    <h4>{slots[0]?.vetName}</h4>

                    <div className="time-slots-grid">
                      {slots
                        .filter(slot => slot.available)
                        .sort(
                          (a, b) =>
                            new Date(a.startTime).getTime() -
                            new Date(b.startTime).getTime()
                        )
                        .map(slot => (
                          <button
                            key={`${vetId}-${slot.startTime}`}
                            type="button"
                            className={`time-slot ${
                              visit.selectedTimeSlot === slot.startTime &&
                              visit.assignedVetId === slot.vetId
                                ? 'selected'
                                : ''
                            }`}
                            onClick={() => handleTimeSlotSelect(slot)}
                          >
                            {formatTimeSlot(slot.startTime)}
                          </button>
                        ))}
                    </div>
                  </div>
                ))
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
      )}

      {showNotification && <div className="notification">{successMessage}</div>}
    </BasicModal>
  );
};

export default AddingVisit;
