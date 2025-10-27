import './VisitListTable.css';
import { useEffect, useState, useRef, createRef } from 'react';
import { Visit } from './models/Visit';

import { useNavigate } from 'react-router-dom';

import { exportVisitsCSV } from './api/exportVisitsCSV';
import { getAllVisits } from './api/getAllVisits';
import { IsVet, IsAdmin, IsReceptionist } from '@/context/UserContext';
import { archiveVisit } from './api/archiveVisit';
import { cancelVisit } from './api/cancelVisit';

import { Category } from './models/Category';

import AddingVisit from './components/AddingVisit';

import BasicModal from '@/shared/components/BasicModal';
import VisitDetails from '@/features/visits/components/VisitDetails';
import EditingVisit from './components/EditingVisit';

import { AppRoutePaths } from '@/shared/models/path.routes';
import Sidebar from './components/Sidebar';
import SidebarItem from './components/SidebarItem';
import SvgIcon from '@/shared/components/SvgIcon';
import { FaCalendarAlt } from 'react-icons/fa';

interface EditingVisitHandle {
  openCreateBill: () => void;
  openPrescription: () => void;
}

export default function VisitListTable(): JSX.Element {
  // refs for each EditingVisit instance, keyed by visitId
  const editingRefs = useRef<
    Record<string, React.RefObject<EditingVisitHandle>>
  >({});
  const [openMenuId, setOpenMenuId] = useState<string | null>(null);

  const toggleMenu = (id: string): void => {
    setOpenMenuId(prev => (prev === id ? null : id));
  };

  const isVet = IsVet();
  const isAdmin = IsAdmin();
  const isReceptionist = IsReceptionist();
  const isStaffMember = isVet || isAdmin || isReceptionist;
  // full list fetched from backend
  const [visits, setVisits] = useState<Visit[]>([]);
  // list currently shown in the UI (filtered by search term / tabs)
  const [displayedVisits, setDisplayedVisits] = useState<Visit[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>(''); // Search term state

  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [showSuccessMessage, setShowSuccessMessage] = useState<boolean>(false);
  const [currentTab, setCurrentTab] = useState<string>('All');

  const navigate = useNavigate();

  function showSuccessAndReload(message: string, delay = 3000): void {
    setSuccessMessage(message);
    setShowSuccessMessage(true);

    setTimeout(() => {
      setShowSuccessMessage(false);
      setSuccessMessage('');
    }, delay);
  }

  useEffect(() => {
    const getVisits = async (): Promise<void> => {
      try {
        const fetchedVisits = await getAllVisits();
        setVisits(fetchedVisits);
        setDisplayedVisits(fetchedVisits);
      } catch (error) {
        console.error('Error fetching visits:', error);
        const message =
          error instanceof Error
            ? error.message
            : 'An unexpected error occurred while fetching visits.';
        setError(`Failed to fetch visits: ${message}`);
      }
    };
    getVisits();
  }, []);

  // Sort visits: emergency visits first, then by VisitDate
  const sortVisits = (visitsList: Visit[]): Visit[] => {
    return [...visitsList].sort((a, b) => {
      if (a.isEmergency && !b.isEmergency) return -1;
      if (!a.isEmergency && b.isEmergency) return 1;

      return new Date(b.visitDate).getTime() - new Date(a.visitDate).getTime();
    });
  };
  useEffect(() => {
    const term = searchTerm.trim().toLowerCase();

    let baseList = visits;

    if (isStaffMember && currentTab !== 'Cancelled') {
      baseList = visits.filter(v => v.status !== 'CANCELLED');
    }

    if (term.length > 0) {
      const filtered = baseList.filter(v =>
        (v.description || '').toLowerCase().includes(term)
      );
      setDisplayedVisits(sortVisits(filtered));
    } else {
      setDisplayedVisits(sortVisits(baseList));
    }
  }, [searchTerm, visits, currentTab, isStaffMember]);

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

  // Handle archiving the visit
  const handleArchive = async (visitId: string): Promise<void> => {
    try {
      await archiveVisit(visitId, updatedVisit => {
        setVisits(prev => {
          return prev.map(visit => {
            if (visit.visitId === visitId) return updatedVisit;
            return visit;
          });
        });
      });

      showSuccessAndReload('Visit archived successfully!');
    } catch (error) {
      return;
    }
  };

  const handleCancel = async (visitId: string): Promise<void> => {
    try {
      await cancelVisit(visitId, updatedVisit => {
        setVisits(prev => {
          return prev.map(visit => {
            if (visit.visitId === visitId) return updatedVisit;
            return visit;
          });
        });
      });

      showSuccessAndReload('Visit cancelled successfully!');
    } catch (error) {
      return;
    }
  };

  const renderCancelButton = (): JSX.Element => (
    <a
      title="Cancel"
      className="icon-visits"
      role="button"
      aria-label="Cancel"
      style={{ color: 'black' }}
    >
      <SvgIcon id="xcross" className="icon-visits" />
    </a>
  );

  const renderArchiveButton = (): JSX.Element => (
    <a
      title="Archive"
      className="icon-visits"
      role="button"
      aria-label="Archive"
      style={{ color: 'black' }}
    >
      <SvgIcon id="archive" className="icon-visits" />
    </a>
  );

  const renderEditButton = (): JSX.Element => (
    <a
      title="Edit"
      className="icon-visits"
      role="button"
      aria-label="Edit"
      style={{ color: 'black' }}
    >
      <SvgIcon id="pencil" className="icon-visits" />
    </a>
  );

  const renderViewButton = (): JSX.Element => (
    <a
      title="View"
      className="icon-visits"
      role="button"
      aria-label="View"
      style={{ color: 'black' }}
    >
      <SvgIcon id="eye" className="icon-visits" />
    </a>
  );

  const renderBillButton = (onClick?: () => void): JSX.Element => (
    <a
      title="Create Bill"
      onClick={onClick}
      className="icon-visits"
      style={{ color: 'black' }}
    >
      <SvgIcon id="visit-bill" className="icon-visits" />
    </a>
  );

  const renderPrescriptionButton = (onClick?: () => void): JSX.Element => (
    <a
      title="Create Prescription"
      onClick={onClick}
      className="icon-visits"
      style={{ color: 'black' }}
    >
      <SvgIcon id="visit-prescription" className="icon-visits" />
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
                    <div className="action-menu">
                      <a
                        href="#"
                        className="icon-button menu-button icon-visits-anchor"
                        onClick={e => {
                          e.preventDefault();
                          toggleMenu(visit.visitId);
                        }}
                        aria-expanded={openMenuId === visit.visitId}
                        aria-label="Open actions"
                        title="Actions"
                        style={{ color: 'black' }}
                      >
                        <SvgIcon id="menu" className="icon-visits" />
                      </a>

                      {openMenuId === visit.visitId && (
                        <div className="action-popover" role="menu">
                          <div className="action-icons-grid">
                            <BasicModal
                              title="Visit Details"
                              showButton={renderViewButton()}
                            >
                              <VisitDetails visitId={visit.visitId} />
                            </BasicModal>

                            {(() => {
                              const editRef =
                                editingRefs.current[visit.visitId] ??
                                (editingRefs.current[visit.visitId] =
                                  createRef());

                              return (
                                <>
                                  <EditingVisit
                                    ref={editRef}
                                    showButton={renderEditButton()}
                                    visitId={visit.visitId}
                                  />

                                  {visit.status === 'COMPLETED' && (
                                    <>
                                      {renderBillButton(() =>
                                        editRef.current?.openCreateBill?.()
                                      )}

                                      {renderPrescriptionButton(() =>
                                        editRef.current?.openPrescription?.()
                                      )}

                                      {!isVet && (
                                        <BasicModal
                                          title="Archive Visit"
                                          showButton={renderArchiveButton()}
                                          onConfirm={() =>
                                            handleArchive(visit.visitId)
                                          }
                                        >
                                          <div>
                                            This will set the status of this
                                            visit to Archived.
                                          </div>
                                          <div>Do you wish to proceed?</div>
                                          {showSuccessMessage && (
                                            <div
                                              className="visit-success-message"
                                              role="status"
                                              aria-live="polite"
                                            >
                                              {successMessage}
                                            </div>
                                          )}
                                        </BasicModal>
                                      )}
                                    </>
                                  )}

                                  {visit.status !== 'CANCELLED' &&
                                    visit.status !== 'ARCHIVED' &&
                                    visit.status !== 'COMPLETED' &&
                                    !isVet && (
                                      <BasicModal
                                        title="Cancel Visit"
                                        showButton={renderCancelButton()}
                                        onConfirm={() =>
                                          handleCancel(visit.visitId)
                                        }
                                      >
                                        <div>
                                          This will set the status of this visit
                                          to Canceled.
                                        </div>
                                        <div>Do you wish to proceed?</div>
                                      </BasicModal>
                                    )}
                                </>
                              );
                            })()}
                          </div>
                        </div>
                      )}
                    </div>
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
          <div className="search-bar">
            <input
              type="text"
              placeholder="Search by description"
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
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
        {showSuccessMessage && (
          <div
            className="visit-success-message"
            role="status"
            aria-live="polite"
            style={{ margin: '8px 0' }}
          >
            {successMessage}
          </div>
        )}
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

  const renderSidebar = (title: string): JSX.Element => {
    return (
      <Sidebar title={tit}>
        {!isAdmin && (
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
        )}
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
        <li>
          <button
            className="btn btn-primary"
            onClick={() => navigate(AppRoutePaths.VisitsCalendar)}
            title="Calendar View"
          >
            <FaCalendarAlt className="me-2" />
            Calendar
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
      {error ? <p>{error}</p> : <>{renderVisitsTables()}</>}
    </div>
  );
}
