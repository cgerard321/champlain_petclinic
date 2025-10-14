import VisitListTable from '@/features/visits/VisitListTable';
import { NavBar } from '@/layouts/AppNavBar';
import './Visits.css';

export default function Visits(): JSX.Element {
  return (
    <>
      <NavBar />

      <div className="page-container">
        <h2>Visits Management</h2>
        <VisitListTable />
      </div>
    </>
  );
}
