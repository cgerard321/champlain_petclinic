import './VisitListTable.css';
import { useEffect, useState } from 'react';
import { Visit } from './models/Visit';

import { useNavigate } from 'react-router-dom';

import { exportVisitsCSV } from './api/exportVisitsCSV';
import { getAllVisits } from './api/getAllVisits';
import { IsVet } from '@/context/UserContext';
// import { AppRoutePaths } from '@/shared/models/path.routes';
import { archiveVisit } from './api/archiveVisit';
import { cancelVisit } from './api/cancelVisit';

import { Category } from './models/Category';

import AddingVisit from './components/AddingVisit';

import BasicModal from '@/shared/components/BasicModal';
import VisitDetails from '@/features/visits/components/VisitDetails';
import EditingVisit from './components/EditingVisit';
import Sidebar from './components/Sidebar';
import SidebarItem from './components/SidebarItem';
import SvgIcon from '@/shared/components/SvgIcon';

export default function VisitListTable(): JSX.Element {
  const isVet = IsVet();
  // full list fetched from backend
  const [visits, setVisits] = useState<Visit[]>([]);
  // list currently shown in the UI (filtered by search term / tabs)
  const [displayedVisits, setDisplayedVisits] = useState<Visit[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>(''); // Search term state

  //use sidebar to select which table is shown
  const [currentTab, setCurrentTab] = useState<string>('All');

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

  const categories: Category[] = [
    { name: 'All', list: displayedVisits },
    { name: 'Emergencies', emergency: true, list: emergencyVisits },
    { name: 'Confirmed', list: confirmedVisits },
    { name: 'Upcoming', list: upcomingVisits },
    { name: 'Completed', list: completedVisits },
    { name: 'Cancelled', list: cancelledVisits },
    { name: 'Archived', list: archivedVisits },
  ];

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

  // Update the displayed list whenever the search term or the full visits list changes.
  // This avoids refetching from the API and preserves the full list in `visits`.
  useEffect(() => {
    const term = searchTerm.trim().toLowerCase();

    const handler = setTimeout(() => {
      if (term.length > 0) {
        const filtered = visits.filter(v =>
          (v.description || '').toLowerCase().includes(term)
        );
        setDisplayedVisits(sortVisits(filtered));
      } else {
        setDisplayedVisits(sortVisits(visits));
      }
    }, 300);

    return () => {
      clearTimeout(handler);
    };
  }, [searchTerm, visits]);

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
    <a title="Cancel">
      <SvgIcon id="xcross" className="icon-visits" />
    </a>
  );

  const renderArchiveButton = (): JSX.Element => (
    <a title="Archive">
      <SvgIcon id="archive" className="icon-visits" />
    </a>
  );

  const renderEditButton = (): JSX.Element => (
    <a title="Edit">
      <SvgIcon id="pencil" className="icon-visits" />
    </a>
  );

  const renderViewButton = (): JSX.Element => (
    <a title="View">
      <SvgIcon id="eye" className="icon-visits" />
    </a>
  );

  // Unified table renderer for all visits
  const renderTable = (title: string, visits: Visit[]): JSX.Element =>
    currentTab === title ? (
      <div className="visit-table-section">
        {visits.length > 0 ? (
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
                    )}

                    {visit.status !== 'CANCELLED' &&
                      visit.status !== 'ARCHIVED' &&
                      visit.status !== 'COMPLETED' &&
                      !isVet && (
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
                      )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div>No visits here!</div>
        )}
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
        {categories.map(category => renderTable(category.name, category.list))}
      </div>
    );
  };

  const renderSidebarItem = (
    name: string,
    emergency?: boolean
  ): JSX.Element => {
    return (
      <SidebarItem
        itemName={name}
        currentTab={currentTab}
        onClick={setCurrentTab}
        emergency={emergency}
      />
    );
  };

  const renderSidebar = (tit: string): JSX.Element => {
    return (
      <Sidebar title={tit}>
        <li>
          <AddingVisit
            showButton={
              <button className="btn btn-primary" title="Create">
                <SvgIcon id="pen-to-square" />
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
            <SvgIcon id="star-empty" />
            Reviews
          </button>
        </li>
        {categories.map(category =>
          renderSidebarItem(category.name, category.emergency)
        )}
      </Sidebar>
    );
  };

  return (
    <div className="visit-page-container">
      {renderSidebar('Visits')}
      {renderVisitsTables()}
    </div>
  );
}
