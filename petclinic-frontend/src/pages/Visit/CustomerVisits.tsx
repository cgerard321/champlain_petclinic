import { NavBar } from '@/layouts/AppNavBar';
import VisitListTable from '@/features/visits/VisitListTable';
import './Visits.css';

export default function CustomerVisits(): JSX.Element {
  return (
    <>
      <NavBar />
      <VisitListTable />
    </>
  );
}
