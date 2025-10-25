import VisitListTable from '@/features/visits/VisitListTable';
import { NavBar } from '@/layouts/AppNavBar';

export default function Visits(): JSX.Element {
  return (
    <>
      <NavBar />
      <VisitListTable />
    </>
  );
}
