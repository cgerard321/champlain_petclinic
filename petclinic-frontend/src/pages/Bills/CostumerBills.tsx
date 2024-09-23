import { NavBar } from '@/layouts/AppNavBar.tsx';
import BillsListTable from '@/features/bills/BillsListTable.tsx';
import { Link } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes';

export default function CustomerBillingPage(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1>Your Bills</h1>
      <div style={{ textAlign: 'right', marginBottom: '10px' }}>
        <Link
          to={AppRoutePaths.CustomerBillsHistory}
          style={{ textDecoration: 'none' }}
        >
          <button className="btn btn-primary">Bills History</button>
        </Link>
      </div>
      <BillsListTable />
    </div>
  );
}
