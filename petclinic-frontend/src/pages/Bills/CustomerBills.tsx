import { useState } from 'react';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import BillsListTable from '@/features/bills/BillsListTable.tsx';
import CurrentBalance from '@/features/bills/CurrentBalance';
import { Currency } from '@/features/bills/utils/convertCurrency';

export default function CustomerBillingPage(): JSX.Element {
  const [currency, setCurrency] = useState<Currency>('CAD');

  return (
    <div>
      <NavBar />
      <h1>Your Bills</h1>
      <CurrentBalance currency={currency} />
      <BillsListTable currency={currency} setCurrency={setCurrency} />
    </div>
  );
}
