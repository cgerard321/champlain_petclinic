import AddEmergencyForm from '@/features/visits/Emergency/AddEmergencyForm';
import { NavBar } from '@/layouts/AppNavBar';

export default function Emergency(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1>leave an emergency</h1>
      <AddEmergencyForm />
    </div>
  );
}
