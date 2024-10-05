import * as React from 'react';
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllEmergency } from './Api/getAllEmergency';
import { EmergencyResponseDTO } from './Model/EmergencyResponseDTO';

const EmergencyList: React.FC = (): JSX.Element => {
  const [emergencyList, setEmergencyList] = useState<EmergencyResponseDTO[]>(
    []
  );
  const [showConfirmDialog, setShowConfirmDialog] = useState<boolean>(false);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchEmergencyData = async (): Promise<void> => {
      try {
        const response = await getAllEmergency();
        // console.log('Fetched emergency data:', response); // Log the response
        if (Array.isArray(response)) {
          setEmergencyList(response);
        } else {
          console.error('Fetched data is not an array:', response);
        }
      } catch (error) {
        console.error('Error fetching emergencies:', error);
      }
    };

    fetchEmergencyData().catch(error =>
      console.error('Error in fetchEmergencyData:', error)
    );
  }, []);

  const handleDeleteAllEmergencies = (confirm: boolean): void => {
    if (confirm) {
      // Logic to delete all emergencies (e.g., API call to delete)
      setEmergencyList([]);
      setShowConfirmDialog(false);
    } else {
      setShowConfirmDialog(false);
    }
  };

  return (
    <div className="emergency-container">
      <h1>Emergencies</h1>
      <table className="emergency-table">
        <thead>
          <tr>
            <th>Pet Name</th>
            <th>Description</th>
            <th>Urgency Level</th>
            <th>Is Critical</th>
            <th>Date</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {emergencyList.length > 0 ? (
            emergencyList.map(emergency => (
              <tr key={emergency.visitEmergencyId}>
                <td>{emergency.petName}</td>
                <td>{emergency.description}</td>
                <td>{emergency.urgencyLevel}</td>
                <td>{new Date(emergency.visitDate).toLocaleDateString()}</td>
                <td>
                  <button className="btn btn-warning" title="Edit">
                    Edit
                  </button>
                  <button
                    className="btn btn-danger"
                    onClick={() => {
                      // Add logic to delete a single emergency
                    }}
                    title="Delete"
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan={6} className="text-center">
                No emergencies available
              </td>
            </tr>
          )}
        </tbody>
      </table>

      <button
        className="btn btn-warning"
        onClick={() => navigate('/visits')}
        title="Return to visits"
      >
        Return to Visits
      </button>

      <button
        className="delete-all-emergencies-button btn btn-success"
        onClick={() => setShowConfirmDialog(true)}
      >
        Delete All Emergencies
      </button>

      {showConfirmDialog && (
        <>
          <div
            className="overlay"
            onClick={() => setShowConfirmDialog(false)}
          ></div>
          <div className="confirm-dialog">
            <p>Are you sure you want to delete all emergencies?</p>
            <button
              className="btn-danger mx-1"
              onClick={() => handleDeleteAllEmergencies(true)}
            >
              Yes
            </button>
            <button
              className="btn-warning mx-1"
              onClick={() => setShowConfirmDialog(false)}
            >
              No
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default EmergencyList;
