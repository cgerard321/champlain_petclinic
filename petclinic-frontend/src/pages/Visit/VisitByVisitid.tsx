import VisitByVisitId from '@/features/visits/components/VisitByVisitId';
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
