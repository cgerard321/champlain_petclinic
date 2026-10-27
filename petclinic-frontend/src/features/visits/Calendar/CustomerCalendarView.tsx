import { useState, useEffect } from 'react';
import {
  format,
  startOfWeek,
  endOfWeek,
  startOfMonth,
  endOfMonth,
  startOfYear,
  endOfYear,
  eachDayOfInterval,
  eachMonthOfInterval,
  isSameDay,
  isSameMonth,
  isToday,
  addMonths,
  subMonths,
  addYears,
  subYears,
  addWeeks,
  subWeeks,
} from 'date-fns';
import { Modal } from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Visit } from '../models/Visit';
import { getAllOwnerVisits } from '../api/getAllOwnerVisits';
import { useUser } from '@/context/UserContext';
import './CalendarView.css';
import './CustomerCalendarView.css';
import { FaChevronLeft, FaChevronRight, FaCalendarAlt } from 'react-icons/fa';

type ViewMode = 'year' | 'month' | 'week';

const LEGEND_STATUSES = [
  'UPCOMING',
  'CONFIRMED',
  'COMPLETED',
  'CANCELLED',
  'ARCHIVED',
];

export default function CustomerCalendarView(): JSX.Element {
  const { user } = useUser();
  const userId = user?.userId;
  const navigate = useNavigate();
  const { t, i18n } = useTranslation('visits');
  const locale = i18n.language.startsWith('fr') ? 'fr-CA' : 'en-CA';

  const [visits, setVisits] = useState<Visit[]>([]);
  const [filteredVisits, setFilteredVisits] = useState<Visit[]>([]);
  const [currentDate, setCurrentDate] = useState<Date>(new Date());
  const [viewMode, setViewMode] = useState<ViewMode>('month');
  const [selectedDay, setSelectedDay] = useState<Date | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    const controller = new AbortController();

    const fetchVisits = async (): Promise<void> => {
      if (!userId) {
        setError(t('calendar.userNotFound'));
        setIsLoading(false);
        return;
      }

      setIsLoading(true);
      setError('');

      try {
        const fetchedVisits: Visit[] = [];

        // Add visits to the calendar as each event arrives.
        for await (const visit of getAllOwnerVisits(
          userId,
          controller.signal
        )) {
          fetchedVisits.push(visit);
          setVisits([...fetchedVisits]);
        }
      } catch (err) {
        if (controller.signal.aborted) return;

        console.error('Error fetching visits:', err);
        setError(t('calendar.loadError'));
      } finally {
        setIsLoading(false);
      }
    };

    fetchVisits();

    return () => controller.abort();
  }, [userId, t]);

  useEffect(() => {
    if (!visits.length) {
      setFilteredVisits([]);
      return;
    }

    let startDate: Date;
    let endDate: Date;

    switch (viewMode) {
      case 'year':
        startDate = startOfYear(currentDate);
        endDate = endOfYear(currentDate);
        break;
      case 'month':
        startDate = startOfMonth(currentDate);
        endDate = endOfMonth(currentDate);
        break;
      case 'week':
        startDate = startOfWeek(currentDate, { weekStartsOn: 0 });
        endDate = endOfWeek(currentDate, { weekStartsOn: 0 });
        break;
    }

    const filtered = visits.filter(visit => {
      const visitDate = new Date(visit.visitDate);
      return visitDate >= startDate && visitDate <= endDate;
    });

    setFilteredVisits(filtered);
  }, [visits, currentDate, viewMode]);

  const handlePrevious = (): void => {
    switch (viewMode) {
      case 'year':
        setCurrentDate(subYears(currentDate, 1));
        break;
      case 'month':
        setCurrentDate(subMonths(currentDate, 1));
        break;
      case 'week':
        setCurrentDate(subWeeks(currentDate, 1));
        break;
    }
  };

  const handleNext = (): void => {
    switch (viewMode) {
      case 'year':
        setCurrentDate(addYears(currentDate, 1));
        break;
      case 'month':
        setCurrentDate(addMonths(currentDate, 1));
        break;
      case 'week':
        setCurrentDate(addWeeks(currentDate, 1));
        break;
    }
  };

  const handleToday = (): void => {
    setCurrentDate(new Date());
  };

  const getVisitsForDate = (date: Date): Visit[] => {
    return filteredVisits.filter(visit =>
      isSameDay(new Date(visit.visitDate), date)
    );
  };

  const getStatusLabel = (status: string): string =>
    t(`calendar.status.${status}`, { defaultValue: status });

  const formatTime = (date: string): string => format(new Date(date), 'HH:mm');

  const renderEmergencyBadge = (): JSX.Element => (
    <span className="calendar-emergency-badge">{t('calendar.emergency')}</span>
  );

  const renderVisitCard = (visit: Visit): JSX.Element => (
    <div
      key={visit.visitId}
      className={`week-visit-item status-${visit.status.toLowerCase()}`}
      onClick={(): void => navigate(`/visits/${visit.visitId}`)}
    >
      <div className="visit-time">
        {formatTime(visit.visitDate)} - {formatTime(visit.visitEndDate)}
        {visit.isEmergency && renderEmergencyBadge()}
      </div>
      <div className="visit-details">
        <div className="visit-pet">
          {visit.petName || t('calendar.unknownPet')}
        </div>
        <div className="visit-status">{getStatusLabel(visit.status)}</div>
        <div className="visit-vet">
          {t('calendar.vetName', {
            firstName: visit.vetFirstName,
            lastName: visit.vetLastName,
          })}
        </div>
        <div className="visit-description">{visit.description}</div>
        {visit.status === 'COMPLETED' && visit.vetNote && (
          <div className="visit-description">
            {t('calendar.vetNote')} {visit.vetNote}
          </div>
        )}
      </div>
    </div>
  );

  const renderYearView = (): JSX.Element => {
    const months = eachMonthOfInterval({
      start: startOfYear(currentDate),
      end: endOfYear(currentDate),
    });

    return (
      <div className="calendar-year-view">
        <div className="year-grid">
          {months.map((month, index) => {
            const monthVisits = filteredVisits.filter(visit =>
              isSameMonth(new Date(visit.visitDate), month)
            );

            return (
              <div
                key={index}
                className="year-month-cell"
                onClick={(): void => {
                  setCurrentDate(month);
                  setViewMode('month');
                }}
              >
                <div className="month-name">
                  {month.toLocaleDateString(locale, { month: 'long' })}
                </div>
                <div className="month-visit-count">
                  {t('calendar.visitCount', { count: monthVisits.length })}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  const renderMonthView = (): JSX.Element => {
    const monthStart = startOfMonth(currentDate);
    const monthEnd = endOfMonth(currentDate);
    const startDate = startOfWeek(monthStart, { weekStartsOn: 0 });
    const endDate = endOfWeek(monthEnd, { weekStartsOn: 0 });

    const days = eachDayOfInterval({ start: startDate, end: endDate });
    const weeks = [];

    for (let i = 0; i < days.length; i += 7) {
      weeks.push(days.slice(i, i + 7));
    }

    return (
      <div className="calendar-month-view">
        <div className="calendar-weekdays">
          {days.slice(0, 7).map(day => (
            <div key={day.toISOString()} className="calendar-weekday">
              {day.toLocaleDateString(locale, { weekday: 'short' })}
            </div>
          ))}
        </div>

        <div className="calendar-days-grid">
          {weeks.map((week, weekIndex) => (
            <div key={weekIndex} className="calendar-week-row">
              {week.map((day, dayIndex) => {
                const dayVisits = getVisitsForDate(day);
                const isCurrentMonth = isSameMonth(day, currentDate);
                const isTodayDate = isToday(day);

                return (
                  <div
                    key={dayIndex}
                    className={`calendar-day-cell ${!isCurrentMonth ? 'other-month' : ''} ${isTodayDate ? 'today' : ''}`}
                    onClick={(): void => setSelectedDay(day)}
                  >
                    <div className="day-number">{format(day, 'd')}</div>

                    <div className="day-visits">
                      {dayVisits.slice(0, 3).map(visit => (
                        <div
                          key={visit.visitId}
                          className={`visit-item customer-visit-label status-${visit.status.toLowerCase()}`}
                        >
                          <span className="visit-time">
                            {formatTime(visit.visitDate)}
                          </span>
                          <span className="visit-pet">
                            {visit.petName || t('calendar.unknownPet')}
                          </span>
                          <span className="visit-status">
                            {getStatusLabel(visit.status)}
                            {visit.isEmergency && renderEmergencyBadge()}
                          </span>
                        </div>
                      ))}

                      {dayVisits.length > 3 && (
                        <div className="visit-item-more">
                          {t('calendar.more', { count: dayVisits.length - 3 })}
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderWeekView = (): JSX.Element => {
    const weekStart = startOfWeek(currentDate, { weekStartsOn: 0 });
    const weekEnd = endOfWeek(currentDate, { weekStartsOn: 0 });
    const days = eachDayOfInterval({ start: weekStart, end: weekEnd });

    return (
      <div className="calendar-week-view">
        <div className="week-grid">
          {days.map((day, index) => {
            const dayVisits = getVisitsForDate(day);
            const isTodayDate = isToday(day);

            return (
              <div
                key={index}
                className={`week-day-column customer-week-day ${isTodayDate ? 'today' : ''}`}
                onClick={(): void => setSelectedDay(day)}
              >
                {/* Day header */}
                <div className="week-day-header">
                  <div className="week-day-name">
                    {day.toLocaleDateString(locale, { weekday: 'short' })}
                  </div>
                  <div className="week-day-number">{format(day, 'd')}</div>
                </div>

                <div className="week-day-visits">
                  {dayVisits.length > 0 ? (
                    dayVisits.map(visit => renderVisitCard(visit))
                  ) : (
                    <div className="no-visits">
                      {t('calendar.noAppointments')}
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  const renderDayModal = (): JSX.Element => {
    const dayVisits = selectedDay ? getVisitsForDate(selectedDay) : [];

    return (
      <Modal
        show={selectedDay !== null}
        onHide={(): void => setSelectedDay(null)}
        size="lg"
        centered
        className="customer-day-modal customer-theme"
      >
        <Modal.Header closeButton>
          <Modal.Title>
            {selectedDay &&
              selectedDay.toLocaleDateString(locale, {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric',
              })}
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          {dayVisits.length === 0 ? (
            <p className="no-visits">{t('calendar.noAppointments')}</p>
          ) : (
            <div className="week-day-visits">
              {dayVisits.map(visit => renderVisitCard(visit))}
            </div>
          )}
        </Modal.Body>
      </Modal>
    );
  };

  const getViewTitle = (): string => {
    switch (viewMode) {
      case 'year':
        return format(currentDate, 'yyyy');
      case 'month':
        return currentDate.toLocaleDateString(locale, {
          month: 'long',
          year: 'numeric',
        });
      case 'week':
        const weekStart = startOfWeek(currentDate, { weekStartsOn: 0 });
        const weekEnd = endOfWeek(currentDate, { weekStartsOn: 0 });
        return `${weekStart.toLocaleDateString(locale, { month: 'short', day: 'numeric' })} - ${weekEnd.toLocaleDateString(locale, { month: 'short', day: 'numeric', year: 'numeric' })}`;
    }
  };

  if (isLoading) {
    return (
      <div className="calendar-loading">
        <div className="spinner"></div>
        <p>{t('calendar.loading')}</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="calendar-error">
        <p>{error}</p>
        <button onClick={(): void => window.location.reload()}>
          {t('calendar.retry')}
        </button>
      </div>
    );
  }

  return (
    <div className="calendar-view-container customer-theme">
      <div className="calendar-header">
        <div className="calendar-title">
          <FaCalendarAlt />
          <h2>{t('calendar.title')}</h2>
        </div>

        <div className="calendar-controls">
          <div className="view-mode-buttons">
            <button
              className={viewMode === 'year' ? 'active' : ''}
              onClick={(): void => setViewMode('year')}
            >
              {t('calendar.year')}
            </button>
            <button
              className={viewMode === 'month' ? 'active' : ''}
              onClick={(): void => setViewMode('month')}
            >
              {t('calendar.month')}
            </button>
            <button
              className={viewMode === 'week' ? 'active' : ''}
              onClick={(): void => setViewMode('week')}
            >
              {t('calendar.week')}
            </button>
          </div>

          <div className="navigation-controls">
            <button onClick={handlePrevious} className="nav-button">
              <FaChevronLeft />
            </button>
            <button onClick={handleToday} className="today-button">
              {t('calendar.today')}
            </button>
            <button onClick={handleNext} className="nav-button">
              <FaChevronRight />
            </button>
          </div>

          <div className="current-period">
            <h3>{getViewTitle()}</h3>
          </div>
        </div>
      </div>

      <div className="customer-calendar-body">
        <div className="calendar-content">
          {viewMode === 'year' && renderYearView()}
          {viewMode === 'month' && renderMonthView()}
          {viewMode === 'week' && renderWeekView()}
        </div>

        <div className="calendar-legend">
          {LEGEND_STATUSES.map(status => (
            <div key={status} className="legend-item">
              <span
                className={`legend-dot status-${status.toLowerCase()}`}
              ></span>
              <span>{getStatusLabel(status)}</span>
            </div>
          ))}
          <div className="legend-item">
            <span className="legend-dot legend-emergency"></span>
            <span>{t('calendar.emergency')}</span>
          </div>
        </div>
      </div>

      {renderDayModal()}
    </div>
  );
}
