import { NavBar } from '@/layouts/AppNavBar.tsx';
import EmailingListTable from '@/features/Emailing/EmailSentViews/EmailingListTable.tsx';
import EmailingOptions from '@/features/Emailing/SendRawEmail/RawEmailPopUp.tsx';
import 'bootstrap/dist/css/bootstrap.min.css';

export default function EmailingPage(): JSX.Element {
  return (
    <div>
      <NavBar />
      <h1>Emails</h1>
      <div className="container text-center">
        <div className="row">
          <div className="col-sm-3">
            <EmailingOptions />
            <br />
            <EmailingListTable />
          </div>
          <div className="col-sm-7"></div>
        </div>
      </div>
    </div>
  );
}
