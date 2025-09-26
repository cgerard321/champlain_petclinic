import VisitListTable from '@/features/visits/VisitListTable';
import SideBar from '@/features/visits/Sidebar';
import { NavBar } from '@/layouts/AppNavBar';
import './Visits.css';

export default function Visits(): JSX.Element {
  return (
    <>
      <NavBar />

      <div className="page-container">
        <SideBar />
        <VisitListTable />
      </div>
    </>
  );
}
