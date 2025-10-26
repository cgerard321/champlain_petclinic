import { FormEvent, useEffect, useMemo, useState } from 'react';
import { Modal, Form, Button } from 'react-bootstrap';
import { Workday } from '@/features/veterinarians/models/Workday';
import { VetRequestModel } from '@/features/veterinarians/models/VetRequestModel';
import { updateVet } from '@/features/veterinarians/api/updateVet';
import './WorkInformationModal.css';

interface WorkInformationModalProps {
  show: boolean;
  vet: VetRequestModel;
  onClose: () => void;
  onSuccess: () => void;
  onError: (message: string) => void;
}

type DaySchedule = {
  enabled: boolean;
  start: string;
  end: string;
};

type DayScheduleMap = Record<Workday, DaySchedule>;
type HourSegment = { start: number; end: number };

const DEFAULT_START = '09:00';
const DEFAULT_END = '17:00';

const WORKDAY_ORDER: Workday[] = [
  Workday.Monday,
  Workday.Tuesday,
  Workday.Wednesday,
  Workday.Thursday,
  Workday.Friday,
  Workday.Saturday,
  Workday.Sunday,
];

const WEEKDAY_ORDER = WORKDAY_ORDER.slice(0, 5);
const WEEKEND_ORDER = WORKDAY_ORDER.slice(5);

const toHourValue = (hour: number): string =>
  `${hour.toString().padStart(2, '0')}:00`;

const toDisplayLabel = (value: string): string => {
  const [hourString] = value.split(':');
  const hour = parseInt(hourString ?? '0', 10);
  const period = hour >= 12 ? 'PM' : 'AM';
  const normalizedHour = hour % 12 || 12;
  return `${normalizedHour}:00 ${period}`;
};

const TIME_OPTIONS = Array.from({ length: 24 }, (_, hour) => {
  const value = toHourValue(hour);
  return { value, label: toDisplayLabel(value) };
});

const isHourSegment = (segment: HourSegment | null): segment is HourSegment =>
  segment != null;

const parseWorkHours = (
  workHoursJson: string
): Record<string, { start: string; end: string }> => {
  if (!workHoursJson) return {};

  try {
    const parsed = JSON.parse(workHoursJson) as Record<string, string[]>;
    return Object.entries(parsed).reduce(
      (acc, [day, slots]) => {
        if (Array.isArray(slots) && slots.length > 0) {
          const normalizedSlots = slots
            .map(slot => {
              const [, start, end] = slot.split('_');
              const startHour = start ? parseInt(start, 10) : NaN;
              const endHour = end ? parseInt(end, 10) : startHour + 1;
              if (Number.isNaN(startHour) || Number.isNaN(endHour)) {
                return null;
              }
              return { start: startHour, end: endHour };
            })
            .filter(isHourSegment)
            .sort((a, b) => a.start - b.start);

          if (normalizedSlots.length > 0) {
            const first = normalizedSlots[0];
            const last = normalizedSlots[normalizedSlots.length - 1];
            acc[day] = {
              start: toHourValue(first.start),
              end: toHourValue(last.end),
            };
          }
        }
        return acc;
      },
      {} as Record<string, { start: string; end: string }>
    );
  } catch {
    return {};
  }
};

const createInitialSchedule = (vet: VetRequestModel): DayScheduleMap => {
  const scheduleFromJson = parseWorkHours(vet.workHoursJson);
  return WORKDAY_ORDER.reduce((acc, day) => {
    const daySchedule = scheduleFromJson[day];
    const isEnabled = vet.workday?.includes(day) || Boolean(daySchedule);
    acc[day] = {
      enabled: Boolean(isEnabled),
      start: daySchedule?.start ?? DEFAULT_START,
      end: daySchedule?.end ?? DEFAULT_END,
    };
    return acc;
  }, {} as DayScheduleMap);
};

const buildHourSegments = (start: string, end: string): string[] => {
  const startHour = parseInt(start.split(':')[0] ?? '0', 10);
  const endHour = parseInt(end.split(':')[0] ?? '0', 10);

  const segments: string[] = [];
  for (let hour = startHour; hour < endHour; hour += 1) {
    segments.push(`Hour_${hour}_${hour + 1}`);
  }
  return segments;
};

export default function WorkInformationModal({
  show,
  vet,
  onClose,
  onSuccess,
  onError,
}: WorkInformationModalProps): JSX.Element {
  const [resume, setResume] = useState<string>(vet.resume ?? '');
  const [active, setActive] = useState<boolean>(vet.active);
  const [daySchedules, setDaySchedules] = useState<DayScheduleMap>(() =>
    createInitialSchedule(vet)
  );
  const [formError, setFormError] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [includeWeekend, setIncludeWeekend] = useState<boolean>(() => {
    const initialSchedule = createInitialSchedule(vet);
    return WEEKEND_ORDER.some(day => initialSchedule[day]?.enabled);
  });

  const timeOptions = useMemo(() => TIME_OPTIONS, []);
  const displayedDays = includeWeekend ? WORKDAY_ORDER : WEEKDAY_ORDER;

  useEffect(() => {
    const nextSchedule = createInitialSchedule(vet);
    setResume(vet.resume ?? '');
    setActive(vet.active);
    setDaySchedules(nextSchedule);
    setIncludeWeekend(WEEKEND_ORDER.some(day => nextSchedule[day]?.enabled));
  }, [vet]);

  const handleToggleDay = (day: Workday): void => {
    setDaySchedules(prev => ({
      ...prev,
      [day]: {
        ...prev[day],
        enabled: !prev[day].enabled,
      },
    }));
  };

  const handleTimeChange = (
    day: Workday,
    key: 'start' | 'end',
    value: string
  ): void => {
    setDaySchedules(prev => ({
      ...prev,
      [day]: {
        ...prev[day],
        [key]: value,
      },
    }));
  };

  const handleWeekendToggle = (isChecked: boolean): void => {
    setIncludeWeekend(isChecked);
  };

  const buildRequestPayload = (): VetRequestModel | null => {
    const workHours: Record<string, string[]> = {};
    const selectedWorkdays: Workday[] = [];

    for (const day of WORKDAY_ORDER) {
      if (!includeWeekend && WEEKEND_ORDER.includes(day)) {
        workHours[day] = [];
        continue;
      }
      const schedule = daySchedules[day];
      if (!schedule) continue;

      if (schedule.enabled) {
        const startHour = parseInt(schedule.start.split(':')[0] ?? '0', 10);
        const endHour = parseInt(schedule.end.split(':')[0] ?? '0', 10);

        if (endHour <= startHour) {
          setFormError(`End time must be after start time for ${day}.`);
          return null;
        }

        workHours[day] = buildHourSegments(schedule.start, schedule.end);
        selectedWorkdays.push(day);
      } else {
        workHours[day] = [];
      }
    }

    setFormError('');

    return {
      ...vet,
      resume,
      active,
      workday: selectedWorkdays,
      workHoursJson: JSON.stringify(workHours),
    };
  };

  const handleSubmit = async (event: FormEvent): Promise<void> => {
    event.preventDefault();
    const payload = buildRequestPayload();
    if (!payload) return;

    try {
      setIsSubmitting(true);
      await updateVet(vet.vetId, payload);
      onSuccess();
      onClose();
    } catch (error) {
      console.error('Failed to update work information:', error);
      onError('Failed to update work information. Please try again.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <Modal
      show={show}
      onHide={onClose}
      centered
      backdrop="static"
      className="work-info-modal"
    >
      <Form onSubmit={handleSubmit}>
        <div className="work-info-modal__header">
          <h2>Edit Work Information</h2>
          <p>
            Update your resume, working days, and availability so owners know
            when they can book with you.
          </p>
        </div>
        <Modal.Body className="work-info-modal__body">
          <Form.Group className="mb-4">
            <Form.Label className="modal-label">Resume</Form.Label>
            <Form.Control
              as="textarea"
              rows={4}
              value={resume}
              onChange={event => setResume(event.target.value)}
              className="modal-textarea"
              placeholder="Share your experience and professional background..."
              required
            />
          </Form.Group>

          <Form.Group className="mb-4 modal-switch-group">
            <Form.Label className="modal-label mb-2">Active Status</Form.Label>
            <Form.Check
              type="switch"
              id="vet-active-switch"
              label={active ? 'Active' : 'Inactive'}
              checked={active}
              onChange={event => setActive(event.target.checked)}
            />
          </Form.Group>

          <div className="workdays-section">
            <h3>Weekly Availability</h3>
            <p>
              Select the days you work and the time range you are available.
            </p>
            <Form.Group className="weekend-toggle">
              <Form.Check
                type="switch"
                id="weekend-toggle"
                label="Can work on weekends?"
                checked={includeWeekend}
                onChange={event => handleWeekendToggle(event.target.checked)}
              />
            </Form.Group>
            <div className="workday-grid">
              {displayedDays.map(day => {
                const schedule = daySchedules[day];
                return (
                  <div
                    key={day}
                    className={`workday-card ${
                      schedule.enabled ? 'workday-card--active' : ''
                    }`}
                  >
                    <div className="workday-card__header">
                      <Form.Check
                        type="checkbox"
                        id={`workday-${day}`}
                        label={day}
                        checked={schedule.enabled}
                        onChange={() => handleToggleDay(day)}
                      />
                    </div>
                    <div className="workday-card__times">
                      <Form.Label>From</Form.Label>
                      <Form.Select
                        value={schedule.start}
                        onChange={event =>
                          handleTimeChange(day, 'start', event.target.value)
                        }
                        disabled={!schedule.enabled}
                        className="time-select"
                      >
                        {timeOptions.map(option => (
                          <option key={option.value} value={option.value}>
                            {option.label}
                          </option>
                        ))}
                      </Form.Select>
                      <Form.Label>To</Form.Label>
                      <Form.Select
                        value={schedule.end}
                        onChange={event =>
                          handleTimeChange(day, 'end', event.target.value)
                        }
                        disabled={!schedule.enabled}
                        className="time-select"
                      >
                        {timeOptions.map(option => (
                          <option key={option.value} value={option.value}>
                            {option.label}
                          </option>
                        ))}
                      </Form.Select>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {formError && <p className="form-error">{formError}</p>}
        </Modal.Body>
        <Modal.Footer className="modal-footer">
          <Button
            variant="outline-secondary"
            onClick={onClose}
            className="modal-button secondary"
            disabled={isSubmitting}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            variant="primary"
            className="modal-button primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Saving...' : 'Save Changes'}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
}
