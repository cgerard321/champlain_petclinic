import { useEffect, useState } from 'react';
import { Visit } from './models/Visit';
import './VisitListTable.css';
import { useNavigate } from 'react-router-dom';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { getAllEmergency } from './Emergency/Api/getAllEmergency';
import { EmergencyResponseDTO } from './Emergency/Model/EmergencyResponseDTO';
import { deleteEmergency } from './Emergency/Api/deleteEmergency';
import './Emergency.css';

export default function VisitListTable(): JSX.Element {
  const [visitsList, setVisitsList] = useState<Visit[]>([]);
  const [emergencyList, setEmergencyList] = useState<EmergencyResponseDTO[]>(
    []
  );
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
        setVisitsList(oldVisits => {
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

  const confirmedVisits = visitsList.filter(
    visit => visit.status === 'CONFIRMED'
  );
  const upcomingVisits = visitsList.filter(
    visit => visit.status === 'UPCOMING'
  );
  const completedVisits = visitsList.filter(
    visit => visit.status === 'COMPLETED'
  );
  const handleDelete = async (visitId: string): Promise<void> => {
    const confirmDelete = window.confirm(
      `Are you sure you want to delete visit with ID: ${visitId}?`
    );
    if (confirmDelete) {
      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/visits/completed/${visitId}`,
          {
            method: 'DELETE',
            credentials: 'include',
          }
        );
        if (response.ok) {
          setVisitsList(prev =>
            prev.filter(visit => visit.visitId !== visitId)
          );
          alert('Visit deleted successfully!');
        } else {
          console.error('Failed to delete the visit.');
          alert('Failed to delete the visit.');
        }
      } catch (error) {
        console.error('Error deleting visit:', error);
        alert('Error deleting visit.');
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
            <th>Pet Name</th>
            <th>Urgency Level</th>
            <th>Emergency Type</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {emergencies.map(emergency => (
            <tr key={emergency.visitEmergencyId}>
              <td>{emergency.visitEmergencyId}</td>
              <td>{new Date(emergency.visitDate).toLocaleString()}</td>
              <td>{emergency.description}</td>
              <td>{emergency.petName}</td>
              <td>{emergency.urgencyLevel}</td>
              <td>{emergency.emergencyType}</td>
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
    allowDelete: boolean = false
  ): JSX.Element => (
    <div className="visit-table-section">
      <h2>{title}</h2>
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
                        : visit.status === 'COMPLETED'
                          ? 'blue'
                          : 'inherit',
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
                {allowDelete && (
                  <button
                    className="btn btn-danger"
                    onClick={() => handleDelete(visit.visitId)}
                    title="Delete"
                  >
                    Delete
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
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
      </div>

      {/* Emergency Table below buttons, but above visit tables */}
      {renderEmergencyTable('Emergency Visits', emergencyList)}

      {renderTable('Confirmed Visits', confirmedVisits)}
      {renderTable('Upcoming Visits', upcomingVisits)}
      {renderTable('Completed Visits', completedVisits, true)}
    </div>
  );
}
