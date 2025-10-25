import { useEffect, useState } from 'react';
import { Visit } from './models/Visit';
import './VisitListTable.css';
import { useNavigate } from 'react-router-dom';

import { exportVisitsCSV } from './api/exportVisitsCSV';
import { getAllVisits } from './api/getAllVisits';
import { IsVet } from '@/context/UserContext';
// import { AppRoutePaths } from '@/shared/models/path.routes';
import { archiveVisit } from './api/archiveVisit';
import { cancelVisit } from './api/cancelVisit';

import eyeIcon from '@/assets/Icons/eyeDark.svg';
import pencilIcon from '@/assets/Icons/pencilDark.svg';
import archiveIcon from '@/assets/Icons/archiveDark.svg';
import xcrossIcon from '@/assets/Icons/xcrossDark.svg';
import pentosquareIcon from '@/assets/Icons/pentosquareLight.svg';
import starIcon from '@/assets/Icons/starEmptyLight.svg';
import AddingVisit from './components/AddingVisit';

import BasicModal from '@/shared/components/BasicModal';
import VisitDetails from '@/features/visits/components/VisitDetails';
import EditingVisit from './components/EditingVisit';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { FaCalendarAlt } from 'react-icons/fa';

export default function VisitListTable(): JSX.Element {
  const isVet = IsVet();
  // full list fetched from backend
  const [visits, setVisits] = useState<Visit[]>([]);
  // list currently shown in the UI (filtered by search term / tabs)
  const [displayedVisits, setDisplayedVisits] = useState<Visit[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>(''); // Search term state

  //use sidebar to select which table is shown
  const [currentTab, setCurrentTab] = useState<string | null>('All');

  const navigate = useNavigate();

  useEffect(() => {
    const getVisits = async (): Promise<void> => {
      try {
        const fetchedVisits = await getAllVisits();
        setVisits(fetchedVisits);
        setDisplayedVisits(fetchedVisits);
      } catch (error) {
        console.error('Error fetching visits:', error);
      }
    };
    getVisits();
  }, []);

  // Sort visits: emergency visits first, then by start date
  const sortVisits = (visitsList: Visit[]): Visit[] => {
    return [...visitsList].sort((a, b) => {
      // Emergency visits come first
      if (a.isEmergency && !b.isEmergency) return -1;
      if (!a.isEmergency && b.isEmergency) return 1;

      // Within the same emergency status, sort by start date (most recent first)
      return new Date(b.visitDate).getTime() - new Date(a.visitDate).getTime();
    });
  };

  // Update the displayed list whenever the search term or the full visits list changes.
  // This avoids refetching from the API and preserves the full list in `visits`.
  useEffect(() => {
    const term = searchTerm.trim().toLowerCase();
    if (term.length > 0) {
      const filtered = visits.filter(v =>
        (v.description || '').toLowerCase().includes(term)
      );
      setDisplayedVisits(sortVisits(filtered));
    } else {
      setDisplayedVisits(sortVisits(visits));
    }
  }, [searchTerm, visits]);

  // Filter visits based on status
  // Derive the different lists from the displayed list so search / tabs compose
  const emergencyVisits = sortVisits(
    displayedVisits.filter(visit => visit.isEmergency)
  );
  const confirmedVisits = sortVisits(
    displayedVisits.filter(visit => {
      return visit.status === 'CONFIRMED';
    })
  );
  const upcomingVisits = sortVisits(
    displayedVisits.filter(visit => {
      return visit.status === 'UPCOMING';
    })
  );
  const completedVisits = sortVisits(
    displayedVisits.filter(visit => {
      return visit.status === 'COMPLETED';
    })
  );
  const cancelledVisits = sortVisits(
    displayedVisits.filter(visit => {
      return visit.status === 'CANCELLED';
    })
  );
  const archivedVisits = sortVisits(
    displayedVisits.filter(visit => {
      return visit.status === 'ARCHIVED';
    })
  );

  // Handle archiving the visit
  const handleArchive = async (visitId: string): Promise<void> => {
    try {
      await archiveVisit(visitId, updatedVisit => {
        // This should probably be removed once the visit list will be reactive
        setVisits(prev => {
          return prev.map(visit => {
            if (visit.visitId === visitId) return updatedVisit;
            return visit;
          });
        });
      });
      setTimeout(() => {
        window.location.reload();
      }, 1000);
    } catch (error) {
      return;
    }
  };

  const handleCancel = async (visitId: string): Promise<void> => {
    try {
      await cancelVisit(visitId, updatedVisit => {
        // Update the full visits list; the displayed list will update automatically
        // via the search effect above.
        setVisits(prev => {
          return prev.map(visit => {
            if (visit.visitId === visitId) return updatedVisit;
            return visit;
          });
        });
      });

      setTimeout(() => {
        window.location.reload();
      }, 1000);
    } catch (error) {
      return;
    }
  };

  // RENDERING

  // Buttons
  const renderCancelButton = (): JSX.Element => (
    <a>
      <img className="icon-visits" src={xcrossIcon} title="Cancel" />
    </a>
  );

  const renderArchiveButton = (): JSX.Element => (
    <a>
      <img className="icon-visits" src={archiveIcon} title="Archive" />
    </a>
  );

  const renderEditButton = (): JSX.Element => (
    <a>
      <img className="icon-visits" src={pencilIcon} title="Edit" />
    </a>
  );

  const renderViewButton = (): JSX.Element => (
    <a>
      <img className="icon-visits" src={eyeIcon} title="View" />
    </a>
  );

  // Sidebar

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
            {title} {/* <a>&#9776;</a> */}
          </h2>
          {/* <button id="toggle-btn"></button> */}
        </li>

        <li>
          <AddingVisit
            showButton={
              <button className="btn btn-primary" title="Create">
                <img src={pentosquareIcon} />
                Create
              </button>
            }
          />
        </li>
        <li>
          <button
            className="btn btn-primary"
            onClick={() => navigate('/reviews')}
            title="Reviews"
          >
            <img src={starIcon} />
            Reviews
          </button>
        </li>
        <li>
          <button
            className="btn btn-primary"
            onClick={() => navigate(AppRoutePaths.VisitsCalendar)}
            title="Calendar View"
          >
            <FaCalendarAlt className="me-2" />
            Calendar View
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

  // Unified table renderer for all visits
  const renderTable = (title: string, visits: Visit[]): JSX.Element =>
    currentTab == title ? (
      <div className="visit-table-section">
        <table>
          <thead>
            <tr>
              <th>Visit Id</th>
              <th>Pet Name</th>
              <th>Description</th>
              <th>Veterinarian</th>
              <th>Vet Email</th>
              <th>Vet Phone Number</th>
              <th>Start Date</th>
              <th>End Date</th>
              <th className="status-column">Status</th>
              <th className="action-column"></th>
            </tr>
          </thead>
          <tbody>
            {visits.map(visit => (
              <tr
                key={visit.visitId}
                className={visit.isEmergency ? 'emergency-visit' : ''}
              >
                <td>{visit.visitId}</td>
                <td>{visit.petName}</td>
                <td>{visit.description}</td>
                <td>
                  {visit.vetFirstName} {visit.vetLastName}
                </td>
                <td>{visit.vetEmail}</td>
                <td>{visit.vetPhoneNumber}</td>
                <td>{new Date(visit.visitDate).toLocaleString()}</td>
                <td>{new Date(visit.visitEndDate).toLocaleString()}</td>
                <td
                  className="status-column"
                  style={{
                    color:
                      visit.status === 'CONFIRMED'
                        ? 'green'
                        : visit.status === 'UPCOMING'
                          ? 'orange'
                          : visit.status === 'CANCELLED'
                            ? 'red'
                            : visit.status === 'COMPLETED'
                              ? 'blue'
                              : visit.status === 'ARCHIVED'
                                ? 'gray'
                                : 'inherit',
                    fontWeight: 'bold',
                  }}
                >
                  {visit.status}
                </td>
                <td className="action-column">
                  {/* <a
                      className="icon"
                      onClick={() => navigate(`/visits/${visit.visitId}`)}
                      title="View"
                    >
                      {renderViewButton()}
                    </a> */}
                  <BasicModal
                    title="Visit Details"
                    showButton={renderViewButton()}
                  >
                    <VisitDetails visitId={visit.visitId} />
                  </BasicModal>

                  <EditingVisit
                    showButton={renderEditButton()}
                    visitId={visit.visitId}
                  />
                  {visit.status === 'COMPLETED' && !isVet && (
                    // <a
                    //   className="icon"
                    //   onClick={() => handleArchive(visit.visitId)}
                    //   title="Archive"
                    // >
                    //   {renderArchiveButton()}
                    // </a>
                    <BasicModal
                      title="Archive Visit"
                      showButton={renderArchiveButton()}
                      onConfirm={() => handleArchive(visit.visitId)}
                    >
                      <div>
                        This will set the status of this visit to Archived.
                      </div>
                      <div>Do you wish to proceed?</div>
                    </BasicModal>
                    // </a>
                  )}

                  {visit.status !== 'CANCELLED' &&
                    visit.status !== 'ARCHIVED' &&
                    visit.status !== 'COMPLETED' &&
                    !isVet && (
                      // <a
                      //   className="icon"
                      //   onClick={() => handleCancel(visit.visitId)}
                      //   title="Cancel"
                      // >
                      //   <img src={xcrossIcon} />
                      // </a>
                      // <a className="icon" title="Delete">
                      <BasicModal
                        title="Cancel Visit"
                        showButton={renderCancelButton()}
                        onConfirm={() => handleCancel(visit.visitId)}
                      >
                        <div>
                          This will set the status of this visit to Canceled.
                        </div>
                        <div>Do you wish to proceed?</div>
                      </BasicModal>
                      // </a>
                    )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    ) : (
      <></>
    );

  const renderVisitsTables = (): JSX.Element => {
    return (
      <div className="page-container">
        <div className="visit-action-bar">
          {/* Search bar for filtering visits */}
          <div className="search-bar">
            <input
              type="text"
              placeholder="Search by description"
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)} // Update the search term when input changes
            />
          </div>

          <button
            className="btn-primary csv-btn"
            onClick={exportVisitsCSV}
            title="Download CSV"
          >
            Download CSV
          </button>
        </div>
        {renderTable('All', displayedVisits)}
        {renderTable('Emergencies', emergencyVisits)}
        {renderTable('Confirmed', confirmedVisits)}
        {renderTable('Upcoming', upcomingVisits)}
        {renderTable('Completed', completedVisits)}
        {renderTable('Cancelled', cancelledVisits)}
        {renderTable('Archived', archivedVisits)}
      </div>
    );
  };

  return (
    <div className="visit-page-container">
      {renderSidebar('Visits')} {renderVisitsTables()}
    </div>
  );
}
