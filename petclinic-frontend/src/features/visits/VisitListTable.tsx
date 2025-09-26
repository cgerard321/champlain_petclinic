import { useEffect, useState } from 'react';
import { Visit } from './models/Visit';
import './VisitListTable.css';
import { useNavigate } from 'react-router-dom';
// import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { getAllEmergency } from './Emergency/Api/getAllEmergency';
import { EmergencyResponseDTO } from './Emergency/Model/EmergencyResponseDTO';
import { deleteEmergency } from './Emergency/Api/deleteEmergency';
import './Emergency.css';
import { exportVisitsCSV } from './api/exportVisitsCSV';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { getAllVisits } from './api/getAllVisits';
import { IsOwner, IsVet } from '@/context/UserContext';
import { AppRoutePaths } from '@/shared/models/path.routes';

export default function VisitListTable(): JSX.Element {
  const [visitIdToDelete, setConfirmDeleteId] = useState<string | null>(null);
  const isVet = IsVet();
  const [visitsList, setVisitsList] = useState<Visit[]>([]);
  const [visitsAll, setVisitsAll] = useState<Visit[]>([]);
  const [archivedVisits, setArchivedVisits] = useState<Visit[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>(''); // Search term state
  const canLeaveReview = IsOwner() || IsVet();
  const [emergencyList, setEmergencyList] = useState<EmergencyResponseDTO[]>(
    []
  );

  //use sidebar to select which table is shown
  const [currentTab, setCurrentTab] = useState<string | null>('Emergencies');

  const navigate = useNavigate();

  useEffect(() => {
    const loadInitialData = async (): Promise<void> => {
      try {
        const [visits, emergencies] = await Promise.all([
          getAllVisits(searchTerm),
          getAllEmergency(),
        ]);
        setVisitsList(visits);
        setEmergencyList(emergencies);
      } catch (error) {
        console.error('Error loading initial data:', error);
      }
    };
    loadInitialData();
  }, [searchTerm]);

  useEffect(() => {
    // Skip EventSource setup for VET role - backend endpoints are ADMIN-only
    // VETs should not reach this component due to route-level restrictions
    if (isVet) {
      return;
    }

    const eventSource = new EventSource('/visits');

    eventSource.onmessage = event => {
      try {
        const newVisit: Visit = JSON.parse(event.data);

        setVisitsList(oldVisits =>
          oldVisits.filter(visit => visit.visitId !== newVisit.visitId)
        );

        setVisitsList(oldVisits => {
          const index = oldVisits.findIndex(
            visit => visit.visitId === newVisit.visitId
          );
          if (index !== -1) {
            // Update existing visit
            const newVisits = [...oldVisits];
            newVisits[index] = newVisit;
            return newVisits;
          } else {
            // Add new visit
            return [...oldVisits, newVisit];
          }
        });

        setVisitsList(oldVisits => {
          if (!oldVisits.some(visit => visit.visitId === newVisit.visitId)) {
            return [...oldVisits, newVisit];
          }
          return oldVisits;
        });
        setVisitsAll(oldVisits => {
          if (!oldVisits.some(visit => visit.visitId === newVisit.visitId)) {
            return [...oldVisits, newVisit];
          }
          return oldVisits;
        });
      } catch (error) {
        console.error('Error parsing SSE data:', error);
      }
    };

    eventSource.onerror = error => {
      console.error('EventSource error:', error);
      eventSource.close();
    };

    return () => {
      eventSource.close();
    };
  }, [isVet]);

  useEffect(() => {
    // Fetch emergency visits
    async function fetchEmergencies(): Promise<void> {
      try {
        const emergencies = await getAllEmergency();
        setEmergencyList(emergencies); // Set emergency data to state
      } catch (error) {
        console.error('Error fetching emergencies:', error);
      }
    }
    fetchEmergencies();
  }, []);

  const handleDeleteEmergency = async (
    visitEmergencyId: string
  ): Promise<void> => {
    try {
      await deleteEmergency(visitEmergencyId);
      setEmergencyList(prevEmergencies =>
        prevEmergencies.filter(
          emergency => emergency.visitEmergencyId !== visitEmergencyId
        )
      );
    } catch (error) {
      console.error(
        `Error deleting emergency with ID ${visitEmergencyId}:`,
        error
      );
    }
  };

  useEffect(() => {
    // Skip EventSource setup for VET role - backend endpoints are ADMIN-only
    // VETs should not reach this component due to route-level restrictions
    if (isVet) {
      return;
    }

    const archivedEventSource = new EventSource(
      'http://localhost:8080/api/v2/gateway/visits/archived',
      {
        withCredentials: true,
      }
    );

    archivedEventSource.onmessage = event => {
      try {
        const newArchivedVisit: Visit = JSON.parse(event.data);

        setArchivedVisits(oldArchived => {
          if (
            !oldArchived.some(
              visit => visit.visitId === newArchivedVisit.visitId
            )
          ) {
            return [...oldArchived, newArchivedVisit];
          } else {
            // Update existing archived visit
            return oldArchived.map(visit =>
              visit.visitId === newArchivedVisit.visitId
                ? newArchivedVisit
                : visit
            );
          }
        });
      } catch (error) {
        console.error('Error parsing SSE data for archived visits:', error);
      }
    };

    archivedEventSource.onerror = error => {
      console.error('Archived EventSource error:', error);
      archivedEventSource.close();
    };

    return () => {
      archivedEventSource.close();
    };
  }, [isVet]);

  useEffect(() => {
    if (searchTerm) {
      setVisitsList(
        visitsAll.filter(visit =>
          visit.description.toLowerCase().includes(searchTerm.toLowerCase())
        )
      );
    } else {
      return;
    }
  }, [searchTerm, visitsAll, visitsList]);

  // Filter visits based on status
  const confirmedVisits = visitsList.filter(
    visit => visit.status === 'CONFIRMED'
  );
  const upcomingVisits = visitsList.filter(
    visit => visit.status === 'UPCOMING'
  );
  const completedVisits = visitsList.filter(
    visit => visit.status === 'COMPLETED'
  );
  const cancelledVisits = visitsList.filter(
    visit => visit.status === 'CANCELLED'
  );
  // Use the archivedVisits state for archived visits

  const handleArchive = async (visitId: string): Promise<void> => {
    const confirmArchive = window.confirm(
      `Are you sure you want to archive visit with ID: ${visitId}?`
    );
    if (confirmArchive) {
      try {
        const requestBody = { status: 'ARCHIVED' };
        await axiosInstance.put(
          `/visits/completed/${visitId}/archive`,
          requestBody,
          { useV2: true }
        );

        // Fetch the updated visit data from the backend
        const updatedVisitResponse = await axiosInstance.get<Visit>(
          `/visits/${visitId}`,
          {
            useV2: false,
          }
        );

        const updatedVisit = await updatedVisitResponse.data;
        setVisitsList(prev =>
          prev.filter(visit =>
            visit.visitId === visitId ? updatedVisit : visit
          )
        );
        alert('Visit archived successfully!');
      } catch (error) {
        console.error('Error archiving visit:', error);
        alert('Error archiving visit.');
      }
    }
  };

  // Handle canceling the visit
  const handleCancel = async (visitId: string): Promise<void> => {
    const confirmCancel = window.confirm(
      'Do you confirm you want to cancel the reservation?'
    );

    if (!confirmCancel) return;
    try {
      await axiosInstance.patch(`/visits/${visitId}/CANCELLED`, {
        useV2: false,
      });
      // Update the visit list after cancellation
      setVisitsAll(prevVisits =>
        prevVisits.map(visit =>
          visit.visitId === visitId ? { ...visit, status: 'CANCELLED' } : visit
        )
      );
    } catch (error) {
      console.error('Error canceling visit:', error);
      alert('Error canceling visit.');
    }
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

  // Render table for emergencies
  const renderEmergencyTable = (
    title: string,
    emergencies: EmergencyResponseDTO[]
  ): JSX.Element =>
    currentTab == title || currentTab == 'All' ? (
      <div className="visit-table-section-red">
        <h2>{title}</h2>
        <table>
          <thead>
            <tr>
              <th>Visit Emergency Id</th>
              <th>Visit Date</th>
              <th>Description</th>
              <th> PetId</th>
              <th>Pet Birthdate </th>
              <th>Pet Name</th>
              <th> PractitionnerId</th>
              <th>vetFirstName</th>
              <th>vetLastName</th>
              <th>Email</th>
              <th>Phone Number</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {emergencies.map(emergency => (
              <tr key={emergency.visitEmergencyId}>
                <td>{emergency.visitEmergencyId}</td>
                <td>{new Date(emergency.visitDate).toLocaleString()}</td>
                <td>{emergency.description}</td>
                <td> {emergency.petId}</td>
                <td> {new Date(emergency.vetBirthDate).toLocaleString()}</td>
                <td>{emergency.petName}</td>
                <td>{emergency.practitionerId}</td>
                <td>{emergency.vetFirstName}</td>
                <td>{emergency.vetLastName}</td>
                <td>{emergency.vetEmail}</td>
                <td>{emergency.vetPhoneNumber}</td>
                <td>
                  {!isVet && (
                    <button
                      className="btn btn-warning"
                      onClick={() => {
                        navigate(
                          `/visits/emergency/${emergency.visitEmergencyId}`
                        );
                      }}
                      title="Edit"
                    >
                      Edit
                    </button>
                  )}
                  {!isVet && (
                    <button
                      className="btn btn-danger"
                      onClick={async () =>
                        setConfirmDeleteId(emergency.visitEmergencyId)
                      }
                      title="Delete"
                    >
                      Delete
                    </button>
                  )}
                  <button
                    className="btn btn-dark"
                    onClick={() =>
                      navigate(
                        `/visits/emergency/${emergency.visitEmergencyId}`
                      )
                    }
                    title="View"
                  >
                    View
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    ) : (
      <></>
    );
  const renderTable = (
    title: string,
    visits: Visit[],
    allowArchive: boolean = false
  ): JSX.Element =>
    currentTab == title || currentTab == 'All' ? (
      <div className="visit-table-section">
        <h2>{title}</h2>
        {
          <table>
            <thead>
              <tr>
                <th>Visit Id</th>
                <th>Visit Date</th>
                <th>Description</th>
                <th>Pet Name</th>
                <th>Vet First Name</th>
                <th>Vet Last Name</th>
                <th>Vet Email</th>
                <th>Visit End Date</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {visits.map(visit => (
                <tr key={visit.visitId}>
                  <td>{visit.visitId}</td>
                  <td>{new Date(visit.visitDate).toLocaleString()}</td>
                  <td>{visit.description}</td>
                  <td>{visit.petName}</td>
                  <td>{visit.vetFirstName}</td>
                  <td>{visit.vetLastName}</td>
                  <td>{visit.vetEmail}</td>
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
                  <td>
                    <button
                      className="btn btn-dark"
                      onClick={() => navigate(`/visits/${visit.visitId}`)}
                      title="View"
                    >
                      View
                    </button>
                    {!isVet && (
                      <button
                        className="btn btn-warning"
                        onClick={() =>
                          navigate(`/visits/${visit.visitId}/edit`)
                        }
                        title="Edit"
                      >
                        Edit
                      </button>
                    )}
                    {allowArchive && !isVet && (
                      <button
                        className="btn btn-secondary"
                        onClick={() => handleArchive(visit.visitId)}
                        title="Archive"
                      >
                        Archive
                      </button>
                    )}

                    {visit.status !== 'CANCELLED' &&
                      visit.status !== 'ARCHIVED' &&
                      visit.status !== 'COMPLETED' &&
                      !isVet && (
                        <button
                          className="btn btn-danger"
                          onClick={() => handleCancel(visit.visitId)}
                        >
                          Cancel Visit
                        </button>
                      )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        }
      </div>
    ) : (
      <></>
    );

  const renderVisitsTables = (): JSX.Element => {
    return (
      <div>
        <div className="visit-actions">
          {/* Search bar for filtering visits */}
          <div className="search-bar">
            <input
              type="text"
              placeholder="Search by visit description"
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)} // Update the search term when input changes
            />
          </div>

          <button
            className="btn btn-primary"
            onClick={exportVisitsCSV}
            title="Download CSV"
          >
            Download CSV
          </button>
        </div>
        {/* Emergency Table below buttons, but above visit tables */}
        {renderEmergencyTable('Emergencies', emergencyList)}

        {renderTable('Confirmed', confirmedVisits)}
        {renderTable('Upcoming', upcomingVisits)}
        {renderTable('Completed', completedVisits, true)}
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
                      await handleDeleteEmergency(visitIdToDelete);
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
    <div className="page-container">
      {renderSidebar('Visits')} {renderVisitsTables()}
    </div>
  );
}
