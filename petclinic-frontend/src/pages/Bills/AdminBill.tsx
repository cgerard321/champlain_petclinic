import { NavBar } from '@/layouts/AppNavBar.tsx';
import AdminBillsListTable from '@/features/bills/AdminBillsListTable.tsx';

export default function AdminBillingPage(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1>All Bills</h1>
      <AdminBillsListTable />
    </div>
  );
}
