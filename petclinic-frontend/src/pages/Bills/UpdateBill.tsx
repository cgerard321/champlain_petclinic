import { NavBar } from '@/layouts/AppNavBar.tsx';
import UpdateBillForm from '@/features/bills/UpdateBillForm.tsx';

export default function UpdateBillPage(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1>Update Bills</h1>
      <UpdateBillForm />
    </div>
  );
}
