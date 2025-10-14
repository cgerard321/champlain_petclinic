import { useState } from 'react';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import AdminBillsListTable from '@/features/bills/AdminBillsListTable.tsx';
import { Currency } from '@/features/bills/utils/convertCurrency';

export default function AdminBillingPage(): JSX.Element {
  const [currency, setCurrency] = useState<Currency>('CAD');

  return (
    <div>
      <NavBar />
      <h1>All Bills</h1>
      <AdminBillsListTable currency={currency} setCurrency={setCurrency} />
    </div>
  );
}
