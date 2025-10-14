import { NavBar } from '@/layouts/AppNavBar';
import CalendarView from '@/features/visits/Calendar/CalendarView';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { FaArrowLeft } from 'react-icons/fa';
import './VisitsCalendar.css';

export default function VisitsCalendar(): JSX.Element {
  const navigate = useNavigate();

  const handleBackToList = (): void => {
    navigate(AppRoutePaths.Visits);
  };

  return (
    <>
      <NavBar />
      <div className="visits-calendar-page">
        <div className="page-container">
          <div className="calendar-navigation">
            <button
              className="btn btn-secondary back-to-list-btn"
              onClick={handleBackToList}
            >
              <FaArrowLeft /> Back to List View
            </button>
          </div>
          <CalendarView />
        </div>
      </div>
    </>
  );
}
