import './VisitListTable.css';
import { useEffect, useState } from 'react';
import { useUser, IsVet } from '@/context/UserContext';
import { Visit } from '@/features/visits/models/Visit.ts';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { getAllOwnerVisits } from './api/getAllOwnerVisits';
import { getAllVetVisits } from './api/getAllVetVisits';
import axios from 'axios';
import { downloadPrescription } from '@/features/visits/Prescription/api/downloadPrescription';
import './CustomerVisitListTable.css';
import BasicModal from '@/shared/components/BasicModal';
import '@/shared/components/BasicModal.css';
import SidebarItem from './components/SidebarItem';
import Sidebar from './components/Sidebar';
import SvgIcon from '@/shared/components/SvgIcon';
import { Category } from './models/Category';

export default function CustomerVisitListTable(): JSX.Element {
  const { user } = useUser();
  const isVet = IsVet();
  const [visits, setVisits] = useState<Visit[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [showErrorDialog, setShowErrorDialog] = useState(false);
  const [errorDialogMessage, setErrorDialogMessage] = useState<string | null>(
    null
  );
  const [displayedVisits, setDisplayedVisits] = useState<Visit[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>(''); // Search term state
  //use sidebar to select which table is shown
  const [currentTab, setCurrentTab] = useState<string>('All');

  const navigate = useNavigate();

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

  useEffect(() => {
    if (!user.userId) return;

    const fetchVisits = async (): Promise<void> => {
      try {
        let visitData;
        if (isVet) {
          visitData = await getAllVetVisits(user.userId);
        } else {
          visitData = await getAllOwnerVisits(user.userId);
        }

        if (Array.isArray(visitData)) {
          setVisits(visitData);
          setDisplayedVisits(visitData);
        } else {
          console.error('Fetched data is not an array', visitData);
        }
      } catch (err) {
        if (err instanceof Error) {
          setError(`Failed to fetch visits: ${err.message}`);
        } else {
          setError('Failed to fetch visits');
        }
      }
    };

    fetchVisits();
  }, [user.userId, isVet]);

  const handleDownloadPrescription = async (
    visitId: string,
    downloadName?: string
  ): Promise<void> => {
    try {
      const blob = await downloadPrescription(visitId);

      if (!(blob instanceof Blob) || blob.size === 0) {
        setErrorDialogMessage('No prescription is associated with this visit.');
        setShowErrorDialog(true);
        return;
      }

      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = downloadName || `prescription-${visitId}.pdf`;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (e) {
      if (axios.isAxiosError(e) && e.response?.status === 404) {
        setErrorDialogMessage('No prescription is associated with this visit.');
        setShowErrorDialog(true);
        return;
      }
      setErrorDialogMessage('An unexpected error occurred.');
      setShowErrorDialog(true);
    }
  };

  useEffect(() => {
    if (showErrorDialog) {
      const trigger = document.getElementById('error-modal-trigger');
      trigger?.click();
    }
  }, [showErrorDialog]);

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

  const renderVisitsTables = (): JSX.Element => {
    return (
      <div className="page-container">
        <div className="visit-actions">
          <div className="search-bar">
            <input
              type="text"
              placeholder="Search by description"
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)} // Update the search term when input changes
            />
          </div>
        </div>
        {categories.map(category => renderTable(category.name, category.list))}
      </div>
    );
  };

  const renderTable = (title: string, visits: Visit[]): JSX.Element =>
    currentTab === title ? (
      <div className="visit-table-section">
        {error ? (
          <p>{error}</p>
        ) : visits.length > 0 ? (
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
                <th>Status</th>
                <th>Download Prescription</th>
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
                    {['CONFIRMED', 'UPCOMING', 'COMPLETED'].includes(
                      visit.status
                    ) && (
                      <a
                        onClick={async ev => {
                          ev.preventDefault();
                          ev.stopPropagation();

                          try {
                            await handleDownloadPrescription(
                              visit.visitId,
                              visit.prescriptionFile?.fileName ||
                                `prescription-${visit.visitId}.pdf`
                            );
                          } catch {}
                        }}
                      >
                        <SvgIcon id="download" className="icon-visits" />
                      </a>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div>No visits here!</div>
        )}
        {showErrorDialog && (
          <BasicModal
            title="No Prescription Available"
            confirmText="OK"
            onConfirm={async () => {
              setShowErrorDialog(false);
              navigate(AppRoutePaths.CustomerVisits);
            }}
            showButton={
              <button id="error-modal-trigger" style={{ display: 'none' }} />
            }
          >
            <p className="basic-modal-body">
              {errorDialogMessage ??
                'An error occurred while downloading the prescription.'}
            </p>
          </BasicModal>
        )}
      </div>
    ) : (
      <></>
    );

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
          <button
            className="btn btn-primary"
            onClick={() => navigate(AppRoutePaths.OwnerBookAppointment)}
            title="Create"
          >
            <SvgIcon id="pen-to-square" />
            Create
          </button>
        </li>
        <li>
          <button
            className="btn btn-primary"
            onClick={() => navigate(AppRoutePaths.CustomerReviews)}
            title="Reviews"
          >
            <SvgIcon id="star-empty" />
            Reviews
          </button>
        </li>
        <li>
          <button
            className="btn btn-primary"
            onClick={() => navigate(AppRoutePaths.CustomerAddReview)}
            title="Write a Review"
          >
            <SvgIcon id="star-full" />
            Write Review
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
      {renderSidebar('My visits')}
      {error ? <p>{error}</p> : <>{renderVisitsTables()}</>}
    </div>
  );
}
