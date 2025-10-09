import VisitListTable from '@/features/visits/VisitListTable';
import { NavBar } from '@/layouts/AppNavBar';
import './Visits.css';

export default function Visits(): JSX.Element {
  return (
    <>
      <NavBar />

      <VisitListTable />
    </>
  );
}
