import { NavBar } from '@/layouts/AppNavBar';
import CustomerVisitListTable from '@/features/visits/CustomerVisitListTable';

export default function CustomerVisits(): JSX.Element {
  return (
    <>
      <NavBar />
      <CustomerVisitListTable />
    </>
  );
}
