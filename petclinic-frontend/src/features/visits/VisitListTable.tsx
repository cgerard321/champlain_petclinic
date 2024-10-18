import { useEffect, useState } from 'react';
import { Visit } from './models/Visit';
import './VisitListTable.css';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { getAllEmergency } from './Emergency/Api/getAllEmergency';
import { EmergencyResponseDTO } from './Emergency/Model/EmergencyResponseDTO';
import { deleteEmergency } from './Emergency/Api/deleteEmergency';
import './Emergency.css';
import { exportVisitsCSV } from './api/exportVisitsCSV';

export default function VisitListTable(): JSX.Element {
  const [visitsList, setVisitsList] = useState<Visit[]>([]);
  const [visitsAll, setVisitsAll] = useState<Visit[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>(''); // Search term state
  const [emergencyList, setEmergencyList] = useState<EmergencyResponseDTO[]>(
    []
  );
  const [archivedVisits, setArchivedVisits] = useState<Visit[]>([]);

  //make tables collapsable
  const [confirmedCollapsed, setConfirmedCollapsed] = useState(false);
  const [upcomingCollapsed, setUpcomingCollapsed] = useState(false);
  const [completedCollapsed, setCompletedCollapsed] = useState(false);
  const [cancelledCollapsed, setCancelledCollapsed] = useState(false);
  const [archivedCollapsed, setArchivedCollapsed] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    const eventSource = new EventSource(
      'http://localhost:8080/api/v2/gateway/visits',
      {
        withCredentials: true,
      }
    );

    eventSource.onmessage = event => {
      try {
        const newVisit: Visit = JSON.parse(event.data);

        if (newVisit.status === 'ARCHIVED') {
          // Remove from visitsList if present
          setVisitsList(oldVisits =>
            oldVisits.filter(visit => visit.visitId !== newVisit.visitId)
          );
        } else {
          setArchivedVisits(oldArchived =>
            oldArchived.filter(visit => visit.visitId !== newVisit.visitId)
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
        }
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
  }, []);

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
  }, []);

  useEffect(() => {
    if (searchTerm) {
      //async (): Promise<void> => {
      //  try {
      //    console.log('searchTerm:', searchTerm);
      //   const list = await getAllVisits(searchTerm);
      //  setVisitsList(list);
      //   console.log('visitsList:', visitsList);
      //  } catch (error) {
      //    console.error('Error fetching visits:', error);
      // }
      //};
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

  const handleArchive = async (visitId: string): Promise<void> => {
    const confirmArchive = window.confirm(
      `Are you sure you want to archive visit with ID: ${visitId}?`
    );
    if (confirmArchive) {
      try {
        const requestBody = { status: 'ARCHIVED' };
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/visits/completed/${visitId}/archive`,
          {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify(requestBody),
          }
        );

        if (response.ok) {
          // Fetch the updated visit data from the backend
          const updatedVisitResponse = await fetch(
            `http://localhost:8080/api/v2/gateway/visits/${visitId}`,
            {
              method: 'GET',
              headers: {
                'Content-Type': 'application/json',
              },
              credentials: 'include',
            }
          );

          if (updatedVisitResponse.ok) {
            const updatedVisit = await updatedVisitResponse.json();
            setArchivedVisits(oldArchived => [...oldArchived, updatedVisit]);
            setVisitsList(prev =>
              prev.filter(visit => visit.visitId !== visitId)
            );
            alert('Visit archived successfully!');
          } else {
            console.error('Failed to fetch the updated visit.');
            alert('Failed to fetch the updated visit.');
          }
        } else {
          const errorData = await response.json();
          console.error('Failed to archive the visit:', errorData);
          alert(`Failed to archive the visit: ${errorData.message}`);
        }
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

    if (confirmCancel) {
      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/visits/${visitId}/CANCELLED`,
          {
            method: 'PATCH',
            credentials: 'include',
            headers: {
              'Content-Type': 'application/json',
            },
          }
        );

        if (!response.ok) {
          throw new Error('Failed to cancel the visit');
        }

        // Update the visit list after cancellation
        setVisitsList(prevVisits =>
          prevVisits.map(visit =>
            visit.visitId === visitId
              ? { ...visit, status: 'CANCELLED' }
              : visit
          )
        );
      } catch (error) {
        console.error('Error canceling visit:', error);
        alert('Error canceling visit.');
      }
    }
  };

  // Render table for emergencies
  const renderEmergencyTable = (
    title: string,
    emergencies: EmergencyResponseDTO[]
  ): JSX.Element => (
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
                <button
                  className="btn btn-warning"
                  onClick={() => {
                    navigate(`/visits/emergency/${emergency.visitEmergencyId}`);
                  }}
                  title="Edit"
                >
                  Edit
                </button>
                <button
                  className="btn btn-danger"
                  onClick={async () => {
                    await handleDeleteEmergency(emergency.visitEmergencyId);
                  }}
                  title="Delete"
                >
                  Delete
                </button>
                <button
                  className="btn btn-dark"
                  onClick={() =>
                    navigate(`/visits/emergency/${emergency.visitEmergencyId}`)
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
  );
  const renderTable = (
    title: string,
    visits: Visit[],
    collapsed: boolean,
    setCollapsed: React.Dispatch<React.SetStateAction<boolean>>,
    allowArchive: boolean = false
  ): JSX.Element => (
    <div className="visit-table-section">
      <h2
        onClick={() => setCollapsed(!collapsed)}
        style={{ cursor: 'pointer' }}
      >
        {title} {collapsed ? '(Show)' : '(Hide)'}
      </h2>
      {!collapsed && (
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
                  <button
                    className="btn btn-warning"
                    onClick={() => navigate(`/visits/${visit.visitId}/edit`)}
                    title="Edit"
                  >
                    Edit
                  </button>
                  {allowArchive && (
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
                    visit.status !== 'COMPLETED' && (
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
      )}
    </div>
  );
  return (
    <div>
      <div className="visit-actions">
        <button
          className="btn btn-warning"
          onClick={() => navigate('/forms')}
          title="Leave a Review"
        >
          Leave a Review
        </button>
        <button
          className="btn btn-dark"
          onClick={() => navigate('/reviews')}
          title="View Reviews"
        >
          View Reviews
        </button>
        <button
          className="btn btn-dark"
          onClick={() => navigate('/visits/emergency')}
          title="Create emergency visit"
        >
          Create Emergency visit
        </button>
        <button
          className="btn btn-warning"
          onClick={() => navigate(AppRoutePaths.AddVisit)}
          title="Make a Visit"
        >
          Make a Visit
        </button>

        <button
          className="btn btn-primary"
          onClick={exportVisitsCSV}
          title="Download Visits CSV"
        >
          Download Visits CSV
        </button>
      </div>

      {/* Emergency Table below buttons, but above visit tables */}
      {renderEmergencyTable('Emergency Visits', emergencyList)}
      {/* Search bar for filtering visits */}
      <div className="search-bar">
        <input
          type="text"
          placeholder="Search by visit description"
          value={searchTerm}
          onChange={e => setSearchTerm(e.target.value)} // Update the search term when input changes
        />
      </div>
      {renderTable(
        'Confirmed Visits',
        confirmedVisits,
        confirmedCollapsed,
        setConfirmedCollapsed
      )}
      {renderTable(
        'Upcoming Visits',
        upcomingVisits,
        upcomingCollapsed,
        setUpcomingCollapsed
      )}
      {renderTable(
        'Completed Visits',
        completedVisits,
        completedCollapsed,
        setCompletedCollapsed,
        true
      )}
      {renderTable(
        'Cancelled Visits',
        cancelledVisits,
        cancelledCollapsed,
        setCancelledCollapsed
      )}
      {renderTable(
        'Archived Visits',
        archivedVisits,
        archivedCollapsed,
        setArchivedCollapsed
      )}
    </div>
  );
}
