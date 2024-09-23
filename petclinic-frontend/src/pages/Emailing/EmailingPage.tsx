import { NavBar } from '@/layouts/AppNavBar.tsx';
import EmailingListTable from '@/features/Emailing/EmailingListTable.tsx';

export default function EmailingPage(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1>Emails</h1>
      <EmailingListTable />
    </div>
  );
}
