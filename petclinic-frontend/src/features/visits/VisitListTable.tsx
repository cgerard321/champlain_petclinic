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
import axiosInstance from '@/shared/api/axiosInstance';
import { getAllVisits } from '@/features/visits/api/getAllVisits.ts';

export default function VisitListTable(): JSX.Element {
  const [allVisits, setAllVisits] = useState<Visit[]>([]);
  const [emergencyList, setEmergencyList] = useState<EmergencyResponseDTO[]>(
      []
  );

  //make tables collapsable
  const [searchTerm, setSearchTerm] = useState<string>(''); // Search term state
  const [confirmedCollapsed, setConfirmedCollapsed] = useState(false);
  const [upcomingCollapsed, setUpcomingCollapsed] = useState(false);
  const [completedCollapsed, setCompletedCollapsed] = useState(false);
  const [cancelledCollapsed, setCancelledCollapsed] = useState(false);
  const [archivedCollapsed, setArchivedCollapsed] = useState(false);

  const navigate = useNavigate();

  const filteredVisits = searchTerm
      ? allVisits.filter(visit =>
          visit.description.toLowerCase().includes(searchTerm.toLowerCase())
      )
      : allVisits;

  // Filter visits based on status
  const confirmedVisits = filteredVisits.filter(
      visit => visit.status === 'CONFIRMED'
  );
  const upcomingVisits = filteredVisits.filter(
      visit => visit.status === 'UPCOMING'
  );
  const completedVisits = filteredVisits.filter(
      visit => visit.status === 'COMPLETED'
  );
  const cancelledVisits = filteredVisits.filter(
      visit => visit.status === 'CANCELLED'
  );
  const archivedVisits = filteredVisits.filter(
      visit => visit.status === 'ARCHIVED'
  );

  useEffect(() => {
    const loadInitialData = async (): Promise<void> => {
      try {
        const [visits, emergencies] = await Promise.all([
          getAllVisits(),
          getAllEmergency(),
        ]);
        setAllVisits(visits);
        setEmergencyList(emergencies);
      } catch (error) {
        console.error('Error loading initial data:', error);
      }
    };
    loadInitialData();
  }, []);

  useEffect(() => {
    const eventSource = new EventSource('/visits');

    eventSource.onmessage = event => {
      try {
        const updatedVisit: Visit = JSON.parse(event.data);

        setAllVisits(prevVisits => {
          const index = prevVisits.findIndex(
              v => v.visitId === updatedVisit.visitId
          );
          if (index !== -1) {
            return prevVisits.map((v, i) => (i === index ? updatedVisit : v));
          } else {
            return [...prevVisits, updatedVisit];
          }
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

  const handleArchive = async (visitId: string): Promise<void> => {
    const confirmArchive = window.confirm(
        `Are you sure you want to archive visit with ID: ${visitId}?`
    );
    if (!confirmArchive) return;

    // TODO Make this use V1
    try {
      const requestBody = { status: 'ARCHIVED' };
      await axiosInstance.put(
          `/visits/completed/${visitId}/archive`,
          requestBody,
          { useV2: true }
      );

      const updatedVisitResponse = await axiosInstance.get<Visit>(
          `/visits/${visitId}`,
          { useV2: false }
      );

      const updatedVisit = updatedVisitResponse.data;
      setAllVisits(prevVisits =>
          prevVisits.map(visit =>
              visit.visitId === visitId ? updatedVisit : visit
          )
      );
      alert('Visit archived successfully!');
    } catch (error) {
      console.error('Error archiving visit:', error);
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
      setAllVisits(prevVisits =>
          prevVisits.map(visit =>
              visit.visitId === visitId ? { ...visit, status: 'CANCELLED' } : visit
          )
      );
    } catch (error) {
      console.error('Error canceling visit:', error);
      alert('Error canceling visit.');
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
            <th>PetId</th>
            <th>Pet Birthdate </th>
            <th>Pet Name</th>
            <th>PractitionnerId</th>
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
                        navigate(
                            `/visits/emergency/${emergency.visitEmergencyId}/edit`
                        );
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
              //TODO This has to be fixed. We should create a new endpoint like /reviews/add
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
          {/*<button*/}
          {/*  className="btn btn-dark"*/}
          {/*  onClick={() => navigate('/visits/emergency')}*/}
          {/*  title="Create emergency visit"*/}
          {/*>*/}
          {/*  Create Emergency visit*/}
          {/*</button>*/}
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
