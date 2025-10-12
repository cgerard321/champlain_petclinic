import { NavBar } from '@/layouts/AppNavBar';
import CalendarView from '@/features/visits/Calendar/CalendarView';
import './VisitsCalendar.css';

export default function VisitsCalendar(): JSX.Element {
  return (
    <>
      <NavBar />
      <div className="visits-calendar-page">
        <div className="page-container">
          <CalendarView />
        </div>
      </div>
    </>
  );
}
