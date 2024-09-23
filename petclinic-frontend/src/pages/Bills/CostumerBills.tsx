import { NavBar } from '@/layouts/AppNavBar.tsx';
import BillsListTable from '@/features/bills/BillsListTable.tsx';
import './Bills.css';


export default function CustomerBillingPage(): JSX.Element {
  return (
      <div>
          <NavBar/>
          <h1>Your Bills</h1>
          <BillsListTable/>
      </div>
  );
}
