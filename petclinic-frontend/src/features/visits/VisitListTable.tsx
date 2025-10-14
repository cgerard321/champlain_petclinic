import { useEffect, useState } from 'react';
import { Visit } from './models/Visit';
import './VisitListTable.css';
import { useNavigate } from 'react-router-dom';

import { exportVisitsCSV } from './api/exportVisitsCSV';
import { getAllVisits } from './api/getAllVisits';
import { IsVet } from '@/context/UserContext';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { archiveVisit } from './api/archiveVisit';
import { cancelVisit } from './api/cancelVisit';

export default function VisitListTable(): JSX.Element {
  const [visitIdToDelete, setConfirmDeleteId] = useState<string | null>(null);
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
    await archiveVisit(visitId, updatedVisit => {
      // This should probably be removed once the visit list will be reactive
      setVisits(prev => {
        return prev.map(visit => {
          if (visit.visitId === visitId) return updatedVisit;
          return visit;
        });
      });
    });
  };

  // Handle canceling the visit
  const handleCancel = async (visitId: string): Promise<void> => {
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
  };

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
                  <a
                    className="icon"
                    onClick={() => navigate(`/visits/${visit.visitId}`)}
                    title="View"
                  >
                    <svg
                      viewBox="0 -3 24 24"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <circle cx="12" cy="12" r="3.5" fill="#212529" />
                      <path
                        d="M21 12C21 12 20 4 12 4C4 4 3 12 3 12"
                        stroke="#212529"
                        strokeWidth="1.2"
                      />
                    </svg>
                  </a>
                  {!isVet && (
                    <a
                      className="icon"
                      onClick={() => navigate(`/visits/${visit.visitId}/edit`)}
                      title="Edit"
                    >
                      <svg
                        viewBox="0 0 24 24"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <rect width="24" height="24" fill="none" />
                        <path
                          d="M15.6287 5.12132L4.31497 16.435M15.6287 5.12132L19.1642 8.65685M15.6287 5.12132L17.0429 3.70711C17.4334 3.31658 18.0666 3.31658 18.4571 3.70711L20.5784 5.82843C20.969 6.21895 20.969 6.85212 20.5784 7.24264L19.1642 8.65685M7.85051 19.9706L4.31497 16.435M7.85051 19.9706L19.1642 8.65685M7.85051 19.9706L3.25431 21.0312L4.31497 16.435"
                          stroke="#212529"
                          strokeLinecap="round"
                          strokeLinejoin="round"
                        />
                      </svg>
                    </a>
                  )}
                  {visit.status === 'COMPLETED' && !isVet && (
                    <a
                      className="icon"
                      onClick={() => handleArchive(visit.visitId)}
                      title="Archive"
                    >
                      <svg
                        viewBox="0 0 24 24"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          d="M16 14L12.5 17.5L9 14M4.5 7.5V20.5H20.5V7.5L18.5 4.5H6.5L4.5 7.5Z"
                          stroke="#212529"
                          strokeWidth="1.2"
                        />
                        <path
                          d="M12.5 10.5V17"
                          stroke="#212529"
                          strokeWidth="1.2"
                        />
                        <path
                          d="M4.5 7.5H20.5"
                          stroke="#212529"
                          strokeWidth="1.2"
                        />
                      </svg>
                    </a>
                  )}

                  {visit.status !== 'CANCELLED' &&
                    visit.status !== 'ARCHIVED' &&
                    visit.status !== 'COMPLETED' &&
                    !isVet && (
                      <a
                        className="icon"
                        onClick={() => handleCancel(visit.visitId)}
                        title="Cancel"
                      >
                        <svg
                          viewBox="0 -2 18 18"
                          fill="none"
                          xmlns="http://www.w3.org/2000/svg"
                        >
                          <path
                            fillRule="evenodd"
                            clipRule="evenodd"
                            d="M12.8536 2.85355C13.0488 2.65829 13.0488 2.34171 12.8536 2.14645C12.6583 1.95118 12.3417 1.95118 12.1464 2.14645L7.5 6.79289L2.85355 2.14645C2.65829 1.95118 2.34171 1.95118 2.14645 2.14645C1.95118 2.34171 1.95118 2.65829 2.14645 2.85355L6.79289 7.5L2.14645 12.1464C1.95118 12.3417 1.95118 12.6583 2.14645 12.8536C2.34171 13.0488 2.65829 13.0488 2.85355 12.8536L7.5 8.20711L12.1464 12.8536C12.3417 13.0488 12.6583 13.0488 12.8536 12.8536C13.0488 12.6583 13.0488 12.3417 12.8536 12.1464L8.20711 7.5L12.8536 2.85355Z"
                            fill="#212529"
                          />
                        </svg>
                      </a>
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
        {visitIdToDelete && (
          <div className="modal">
            <div className="modal-content">
              <h3>Confirm Deletion</h3>
              <p>
                Are you sure you want to delete emergency visit{' '}
                {visitIdToDelete}?
              </p>
              <div className="modal-buttons">
                <button onClick={() => setConfirmDeleteId(null)}>Cancel</button>
                <button
                  onClick={async () => {
                    try {
                      // Implement Deleting visit
                      setConfirmDeleteId(null);
                    } catch (error) {
                      console.error('Error deleting emergency visit:', error);
                      alert(
                        'Failed to delete emergency visit. Please try again.'
                      );
                    }
                  }}
                >
                  Confirm
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="visit-page-container">
      {renderSidebar('Visits')} {renderVisitsTables()}
    </div>
  );
}
