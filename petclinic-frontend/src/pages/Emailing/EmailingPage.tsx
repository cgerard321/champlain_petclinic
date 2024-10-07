import { NavBar } from '@/layouts/AppNavBar.tsx';
import EmailingListTable from '@/features/Emailing/EmailingListTable.tsx';
import EmailingOptions from '@/features/Emailing/SendRawEmail/RawEmailPopUp.tsx';

export default function EmailingPage(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1>Emails</h1>
      <EmailingOptions />
      <EmailingListTable />
    </div>
  );
}
