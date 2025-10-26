import { NavBar } from '@/layouts/AppNavBar';
import CustomerCalendarView from '@/features/visits/Calendar/CustomerCalendarView';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { FaArrowLeft } from 'react-icons/fa';
import './VisitsCalendar.css';

export default function CustomerVisitsCalendar(): JSX.Element {
  const navigate = useNavigate();

  const handleBackToList = (): void => {
    navigate(AppRoutePaths.CustomerVisits);
  };

  return (
    <>
      <NavBar />

      <div className="visits-calendar-page customer-page">
        <div className="page-container">
          <div className="calendar-navigation">
            <button
              className="btn btn-outline-secondary back-to-list-btn"
              onClick={handleBackToList}
            >
              <FaArrowLeft /> Back to My Visits
            </button>
          </div>

          <CustomerCalendarView />
        </div>
      </div>
    </>
  );
}
