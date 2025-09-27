import { NavBar } from '@/layouts/AppNavBar';
import OwnerBookingVisit from '@/features/visits/models/OwnerBookingVisit';
import './Visits.css';

export default function OwnerBookAppointment(): JSX.Element {
  return (
    <>
      <NavBar />
      <div className="page-container">
        <OwnerBookingVisit />
      </div>
    </>
  );
}
