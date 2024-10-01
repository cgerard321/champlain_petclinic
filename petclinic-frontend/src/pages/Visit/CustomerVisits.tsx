import { NavBar } from '@/layouts/AppNavBar';
import CustomerVisitListTable from '@/features/visits/CustomerVisitListTable';
import './Visits.css';

export default function CustomerVisits(): JSX.Element {
  return (
    <>
      <NavBar />
      <div className="page-container">
        <h2>Your Visits</h2>
        <CustomerVisitListTable />
      </div>
    </>
  );
}
