import VisitListTable from '@/features/visits/VisitListTable';
import { NavBar } from '@/layouts/AppNavBar';

export default function Visits(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h2>This is the Visits Page</h2>
      <VisitListTable />
    </div>
  );
}
