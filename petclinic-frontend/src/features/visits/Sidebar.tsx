import './Sidebar.css';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { useState } from 'react';

export default function VisitListTable(): JSX.Element {
  const [currentTab, setCurrentTab] = useState<string | null>('Emergencies');

  const navigate = useNavigate();

  //   const renderUnreadCircle = (enabled: boolean): JSX.Element => (
  //     <svg
  //       className="unread-icon"
  //       viewBox="0 0 24 24"
  //       xmlns="http://www.w3.org/2000/svg"
  //       strokeWidth="3"
  //     >
  //       <path
  //         d="M12 9.5C13.3807 9.5 14.5 10.6193 14.5 12C14.5 13.3807 13.3807 14.5 12 14.5C10.6193 14.5 9.5 13.3807 9.5 12C9.5 10.6193 10.6193 9.5 12 9.5Z"
  //         fill="#000000"
  //       />
  //     </svg>
  //   );

  //   const renderVisitNumber = (amount: number): string => {
  //     if (amount > 0) {
  //       if (amount > 99) {
  //         return '99+';
  //       } else {
  //         return `${amount}`;
  //       }
  //     } else {
  //       return '';
  //     }
  //   };

  const renderSidebarItem = (
    name: string,
    emergency: boolean = false
    // visitAmount: number
  ): JSX.Element => (
    <li>
      <a
        className={
          (name == currentTab ? 'active' : '') + (emergency ? 'emergency' : '')
        }
        onClick={() => {
          setCurrentTab(name);
        }}
      >
        {/* {renderUnreadCircle(true)} */}
        <span>{name}</span>
        {/* {renderVisitNumber(visitAmount)} */}
      </a>
    </li>
  );

  const renderSidebar = (title: string): JSX.Element => (
    <aside id="sidebar">
      <ul>
        <li>
          <h2>
            {title} <a>&#9776;</a>
          </h2>
          {/* <button id="toggle-btn"></button> */}
        </li>

        <li>
          <button
            className="btn btn-primary"
            onClick={() => navigate(AppRoutePaths.AddVisit)}
            title="Create"
          >
            <svg viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
              <title>pen-to-square</title>
              <path d="M7.744 19.189l-1.656 5.797c-0.019 0.062-0.029 0.133-0.029 0.207 0 0.413 0.335 0.748 0.748 0.748 0.001 0 0.001 0 0.002 0h-0c0.001 0 0.002 0 0.003 0 0.075 0 0.146-0.011 0.214-0.033l-0.005 0.001 5.622-1.656c0.124-0.037 0.23-0.101 0.315-0.186l-0 0 17.569-17.394c0.137-0.135 0.223-0.323 0.223-0.531v-0c0-0 0-0.001 0-0.001 0-0.207-0.084-0.395-0.219-0.531l-4.141-4.142c-0.136-0.136-0.324-0.22-0.531-0.22s-0.395 0.084-0.531 0.22v0l-17.394 17.394c-0.088 0.088-0.153 0.198-0.189 0.321l-0.001 0.005zM25.859 3.061l3.078 3.078-3.078 3.047-3.079-3.047zM21.72 7.2l3.073 3.041-12.756 12.628-4.133 1.217 1.229-4.299zM30 13.25c-0.414 0-0.75 0.336-0.75 0.75v0 15.25h-26.5v-26.5h15.25c0.414 0 0.75-0.336 0.75-0.75s-0.336-0.75-0.75-0.75v0h-16c-0.414 0-0.75 0.336-0.75 0.75v0 28c0 0.414 0.336 0.75 0.75 0.75h28c0.414-0 0.75-0.336 0.75-0.75v0-16c-0-0.414-0.336-0.75-0.75-0.75v0z" />
            </svg>
            Create
          </button>
        </li>
        <li>
          <button
            className="btn btn-primary"
            onClick={() => navigate('/reviews')}
            title="Reviews"
          >
            <svg viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
              <path d="M16 23.21l7.13 4.13-1.5-7.62a.9.9 0 0 1 .27-.83l5.64-5.29-7.64-.93a.9.9 0 0 1-.71-.52L16 5.1l-3.22 7a.9.9 0 0 1-.71.52l-7.6.93 5.63 5.29a.9.9 0 0 1 .27.83l-1.51 7.67zm0 2l-7.9 4.58a.9.9 0 0 1-1.34-.95l1.73-9-6.65-6.3A.9.9 0 0 1 2.36 12l9-1.08 3.81-8.32a.9.9 0 0 1 1.64 0l3.81 8.32 9 1.08a.9.9 0 0 1 .51 1.55l-6.66 6.3 1.68 9a.9.9 0 0 1-1.34.94z"></path>
            </svg>
            Reviews
          </button>
        </li>
        {renderSidebarItem('All')}
        {renderSidebarItem('Emergencies', true)}
        {renderSidebarItem('Confirmed')}
        {renderSidebarItem('Upcoming')}
        {renderSidebarItem('Completed')}
        {renderSidebarItem('Cancelled')}
        {renderSidebarItem('Archived')}
      </ul>
    </aside>
  );

  return renderSidebar('Visits');
}
