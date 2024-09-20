import VisitByVisitId from '@/features/visits/visits/VisitByVisitId';
import { NavBar } from '@/layouts/AppNavBar';

export default function Visits(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h2></h2>
      <VisitByVisitId />
    </div>
  );
}
