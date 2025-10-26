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
import { Visit } from '../models/Visit';
import { getAllVisits } from '../api/getAllVisits';
import { getVisitsForPractitioner } from '../api/getVisitsForPractitioner';
import { useUser, IsAdmin, IsVet, IsOwner } from '@/context/UserContext';
import './CalendarView.css';
import { FaChevronLeft, FaChevronRight, FaCalendarAlt } from 'react-icons/fa';
import { getAllOwnerVisits } from '../api/getAllOwnerVisits';

type ViewMode = 'year' | 'month' | 'week';

export default function CalendarView(): JSX.Element {
  const { user } = useUser();
  const isAdmin = IsAdmin();
  const isVet = IsVet();
  const isOwner = IsOwner();

  const [visits, setVisits] = useState<Visit[]>([]);
  const [filteredVisits, setFilteredVisits] = useState<Visit[]>([]);
  const [currentDate, setCurrentDate] = useState<Date>(new Date());
  const [viewMode, setViewMode] = useState<ViewMode>('month');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');

  useEffect(() => {
    const fetchVisits = async (): Promise<void> => {
      setIsLoading(true);
      setError('');

      try {
        let fetchedVisits: Visit[] = [];

        if (isAdmin) {
          fetchedVisits = await getAllVisits();
        } else if (isVet && user?.userId) {
          fetchedVisits = await getVisitsForPractitioner(user.userId);
        } else if (isOwner && user?.userId) {
          fetchedVisits = await getAllOwnerVisits(user.userId);
        }

        setVisits(fetchedVisits);
      } catch (err) {
        console.error('Error fetching visits:', err);
        setError('Failed to load visits. Please try again.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchVisits();
  }, [isAdmin, isVet, isOwner, user?.userId]);

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
                <div className="month-name">{format(month, 'MMMM')}</div>
                <div className="month-visit-count">
                  {monthVisits.length} visit
                  {monthVisits.length !== 1 ? 's' : ''}
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
          {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
            <div key={day} className="calendar-weekday">
              {day}
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
                  >
                    <div className="day-number">{format(day, 'd')}</div>
                    <div className="day-visits">
                      {dayVisits.slice(0, 3).map((visit, idx) => (
                        <div
                          key={idx}
                          className={`visit-item status-${visit.status.toLowerCase()}`}
                          title={`${visit.petName || 'Pet'} - ${visit.description}`}
                        >
                          <span className="visit-time">
                            {format(new Date(visit.visitDate), 'HH:mm')}
                          </span>
                          <span className="visit-pet">{visit.petName}</span>
                        </div>
                      ))}
                      {dayVisits.length > 3 && (
                        <div className="visit-item-more">
                          +{dayVisits.length - 3} more
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
                className={`week-day-column ${isTodayDate ? 'today' : ''}`}
              >
                <div className="week-day-header">
                  <div className="week-day-name">{format(day, 'EEE')}</div>
                  <div className="week-day-number">{format(day, 'd')}</div>
                </div>
                <div className="week-day-visits">
                  {dayVisits.length > 0 ? (
                    dayVisits.map((visit, idx) => (
                      <div
                        key={idx}
                        className={`week-visit-item status-${visit.status.toLowerCase()}`}
                      >
                        <div className="visit-time">
                          {format(new Date(visit.visitDate), 'HH:mm')}
                        </div>
                        <div className="visit-details">
                          <div className="visit-pet">
                            {visit.petName || 'Unknown Pet'}
                          </div>
                          <div className="visit-vet">
                            {visit.vetFirstName} {visit.vetLastName}
                          </div>
                          <div className="visit-description">
                            {visit.description}
                          </div>
                        </div>
                      </div>
                    ))
                  ) : (
                    <div className="no-visits">No visits scheduled</div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  const getViewTitle = (): string => {
    switch (viewMode) {
      case 'year':
        return format(currentDate, 'yyyy');
      case 'month':
        return format(currentDate, 'MMMM yyyy');
      case 'week':
        const weekStart = startOfWeek(currentDate, { weekStartsOn: 0 });
        const weekEnd = endOfWeek(currentDate, { weekStartsOn: 0 });
        return `${format(weekStart, 'MMM d')} - ${format(weekEnd, 'MMM d, yyyy')}`;
    }
  };

  if (isLoading) {
    return (
      <div className="calendar-loading">
        <div className="spinner"></div>
        <p>Loading visits...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="calendar-error">
        <p>{error}</p>
        <button onClick={(): void => window.location.reload()}>Retry</button>
      </div>
    );
  }

  return (
    <div className="calendar-view-container">
      <div className="calendar-header">
        <div className="calendar-title">
          <FaCalendarAlt />
          <h2>Visits Calendar</h2>
        </div>

        <div className="calendar-controls">
          <div className="view-mode-buttons">
            <button
              className={viewMode === 'year' ? 'active' : ''}
              onClick={(): void => setViewMode('year')}
            >
              Year
            </button>
            <button
              className={viewMode === 'month' ? 'active' : ''}
              onClick={(): void => setViewMode('month')}
            >
              Month
            </button>
            <button
              className={viewMode === 'week' ? 'active' : ''}
              onClick={(): void => setViewMode('week')}
            >
              Week
            </button>
          </div>

          <div className="navigation-controls">
            <button onClick={handlePrevious} className="nav-button">
              <FaChevronLeft />
            </button>
            <button onClick={handleToday} className="today-button">
              Today
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

      <div className="calendar-content">
        {viewMode === 'year' && renderYearView()}
        {viewMode === 'month' && renderMonthView()}
        {viewMode === 'week' && renderWeekView()}
      </div>

      <div className="calendar-legend">
        <div className="legend-item">
          <span className="legend-dot status-upcoming"></span>
          <span>Upcoming</span>
        </div>
        <div className="legend-item">
          <span className="legend-dot status-confirmed"></span>
          <span>Confirmed</span>
        </div>
        <div className="legend-item">
          <span className="legend-dot status-completed"></span>
          <span>Completed</span>
        </div>
        <div className="legend-item">
          <span className="legend-dot status-cancelled"></span>
          <span>Cancelled</span>
        </div>
      </div>
    </div>
  );
}
