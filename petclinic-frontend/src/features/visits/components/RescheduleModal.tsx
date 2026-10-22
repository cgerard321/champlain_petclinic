import * as React from 'react';
import { useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import DatePickerComponent from 'react-datepicker';
import { format } from 'date-fns';
import 'react-datepicker/dist/react-datepicker.css';

import BasicModal from '@/shared/components/BasicModal';
import {
  getAvailableSlots,
  TimeSlot,
} from '@/features/visits/api/getAvailableSlots';
import { getAvailableDates } from '@/features/visits/api/getAvailableDates';
import { Visit } from '@/features/visits/models/Visit';

import './RescheduleModal.css';

interface RescheduleModalProps {
  showButton: JSX.Element;
  visit: Visit;
  onConfirm: (visitDate: string) => Promise<void>;
}

const DAY_IN_MS = 24 * 60 * 60 * 1000;

const toIsoDate = (date: Date): string => format(date, 'yyyy-MM-dd');

const RescheduleModal: React.FC<RescheduleModalProps> = ({
  showButton,
  visit,
  onConfirm,
}) => {
  const { t, i18n } = useTranslation('visits');
  // null until the available dates load; every future day stays selectable in the meantime
  const [availableDates, setAvailableDates] = useState<Set<string> | null>(
    null
  );
  const [selectedDate, setSelectedDate] = useState<Date | null>(null);
  const [timeSlots, setTimeSlots] = useState<TimeSlot[]>([]);
  const [selectedTime, setSelectedTime] = useState('');
  const [isLoadingSlots, setIsLoadingSlots] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const minDate = new Date(Date.now() + DAY_IN_MS);

  useEffect(() => {
    const endDate = new Date();
    endDate.setMonth(endDate.getMonth() + 6);

    getAvailableDates(
      visit.practitionerId,
      toIsoDate(new Date()),
      toIsoDate(endDate)
    )
      .then(response => setAvailableDates(new Set(response.data)))
      .catch(error => console.error('Error fetching available dates:', error));
  }, [visit.practitionerId]);

  const isAvailableDay = (date: Date): boolean =>
    availableDates === null || availableDates.has(toIsoDate(date));

  const loadTimeSlots = (date: Date | null): void => {
    setSelectedDate(date);
    setSelectedTime('');
    setTimeSlots([]);
    setErrorMessage('');

    if (!date) return;

    setIsLoadingSlots(true);
    getAvailableSlots(visit.practitionerId, toIsoDate(date))
      .then(response => {
        // Only keep free slots that are still more than 24 hours away
        setTimeSlots(
          response.data.filter(
            slot =>
              slot.available &&
              new Date(slot.startTime).getTime() - Date.now() > DAY_IN_MS
          )
        );
      })
      .catch(error => {
        console.error('Error fetching time slots:', error);
        setErrorMessage(t('followUp.slotsError'));
      })
      .finally(() => setIsLoadingSlots(false));
  };

  const formatTime = (dateTime: string): string =>
    new Date(dateTime).toLocaleTimeString(i18n.language, {
      hour: '2-digit',
      minute: '2-digit',
    });

  const validate = (): boolean => {
    if (!selectedDate) {
      setErrorMessage(t('followUp.dateRequired'));
      return false;
    }

    if (!selectedTime) {
      setErrorMessage(t('followUp.timeRequired'));
      return false;
    }

    setErrorMessage('');
    return true;
  };

  return (
    <BasicModal
      title={t('followUp.rescheduleTitle')}
      showButton={showButton}
      validate={validate}
      onConfirm={() => onConfirm(selectedTime)}
      confirmText={t('followUp.confirmReschedule')}
      errorMessage={errorMessage}
    >
      <div className="reschedule-form">
        <div className="reschedule-visit-details">
          <div>
            <span>{t('followUp.pet')}</span>
            <strong>{visit.petName}</strong>
          </div>
          <div>
            <span>{t('followUp.veterinarian')}</span>
            <strong>
              {visit.vetFirstName} {visit.vetLastName}
            </strong>
          </div>
        </div>

        <div className="reschedule-field">
          <label htmlFor="rescheduleDate">{t('followUp.date')}</label>
          <DatePickerComponent
            id="rescheduleDate"
            selected={selectedDate}
            onChange={(date: Date | null) => loadTimeSlots(date)}
            minDate={minDate}
            filterDate={isAvailableDay}
            dateFormat="yyyy-MM-dd"
            className="reschedule-input"
            wrapperClassName="reschedule-date-wrapper"
          />
        </div>

        <div className="reschedule-field">
          <label htmlFor="rescheduleTime">{t('followUp.time')}</label>
          <select
            id="rescheduleTime"
            className="reschedule-input"
            value={selectedTime}
            disabled={timeSlots.length === 0}
            onChange={e => {
              setSelectedTime(e.target.value);
              setErrorMessage('');
            }}
          >
            <option value="" />
            {timeSlots.map(slot => (
              <option key={slot.startTime} value={slot.startTime}>
                {formatTime(slot.startTime)} - {formatTime(slot.endTime)}
              </option>
            ))}
          </select>
          {isLoadingSlots ? (
            <small className="reschedule-hint">
              {t('followUp.loadingSlots')}
            </small>
          ) : (
            selectedDate &&
            timeSlots.length === 0 &&
            !errorMessage && (
              <small className="reschedule-hint">{t('followUp.noSlots')}</small>
            )
          )}
        </div>
      </div>
    </BasicModal>
  );
};

export default RescheduleModal;
