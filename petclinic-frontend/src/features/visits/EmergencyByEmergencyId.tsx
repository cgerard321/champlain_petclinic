import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { getEmergencyById } from './Emergency/Api/getEmergencyById';
import { EmergencyResponseDTO } from './Emergency/Model/EmergencyResponseDTO';

export default function EmergencyDetails(): JSX.Element {
  const { visitEmergencyId } = useParams<{ visitEmergencyId: string }>(); // Updated line// Extract emergencyId from URL parameters
  const [emergency, setEmergency] = useState<EmergencyResponseDTO | null>(null); // State for the emergency
  const navigate = useNavigate();

  useEffect(() => {
    if (visitEmergencyId) {
      getEmergencyById(visitEmergencyId) // Use emergencyId here
        .then(response => {
          setEmergency(response);
        })
        .catch(error => {
          console.error('Error fetching emergency:', error);
        });
    }
  }, [visitEmergencyId]);

  if (!emergency) {
    return <div>Loadingsssssss...</div>;
  }

  return (
    <div className="emergency-details-container">
      <h1 className="emergency-details-title">Emergency Details</h1>
      <table className="emergency-details-table">
        <tbody>
          <tr>
            <th>Field</th>
            <th>Value</th>
          </tr>
          <tr>
            <td>Visit Emergency ID</td>
            <td>{emergency.visitEmergencyId}</td>
          </tr>
          <tr>
            <td>Visit Date</td>
            <td>{new Date(emergency.visitDate).toLocaleString()}</td>
          </tr>
          <tr>
            <td>Description</td>
            <td>{emergency.description}</td>
          </tr>
          <tr>
            <td>Pet ID</td>
            <td>{emergency.petId}</td>
          </tr>
          <tr>
            <td>Pet Name</td>
            <td>{emergency.petName}</td>
          </tr>
          <tr>
            <td>Vet First Name</td>
            <td>{emergency.vetFirstName}</td>
          </tr>
          <tr>
            <td>Vet Last Name</td>
            <td>{emergency.vetLastName}</td>
          </tr>
          <tr>
            <td>Vet Email</td>
            <td>{emergency.vetEmail}</td>
          </tr>
          <tr>
            <td>Vet Phone Number</td>
            <td>{emergency.vetPhoneNumber}</td>
          </tr>
        </tbody>
      </table>
      <button
        className="btn btn-warning"
        onClick={() => navigate('/visits')}
        title="Return to visits"
      >
        Return to Emergencies
      </button>
    </div>
  );
}
