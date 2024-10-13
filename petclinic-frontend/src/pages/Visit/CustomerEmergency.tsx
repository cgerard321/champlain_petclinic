import { NavBar } from '@/layouts/AppNavBar';
import EmergencyVisitCustomer from '@/features/visits/EmergencyVisitCustomer';

export default function CustomerEmergency(): JSX.Element {
  return (
    <>
      <NavBar />
      <div className="page-container">
        <h2>Your Emergency Visits</h2>
        <EmergencyVisitCustomer />
      </div>
    </>
  );
}
