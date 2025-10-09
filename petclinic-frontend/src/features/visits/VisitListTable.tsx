import { JSXElementConstructor, useEffect, useState } from 'react';
import { Visit } from './models/Visit';
import './VisitListTable.css';
import './Emergency.css';
import { useNavigate } from 'react-router-dom';
import { getAllEmergency } from './Emergency/Api/getAllEmergency';
import { EmergencyResponseDTO } from './Emergency/Model/EmergencyResponseDTO';
import { deleteEmergency } from './Emergency/Api/deleteEmergency';

import { exportVisitsCSV } from './api/exportVisitsCSV';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { getAllVisits } from './api/getAllVisits';
import { IsVet } from '@/context/UserContext';
import { AppRoutePaths } from '@/shared/models/path.routes';
import ConfirmationModal from '@/shared/components/ConfirmationModal';

import eyeIcon from '@/assets/Icons/eyeDark.svg';
import pencilIcon from '@/assets/Icons/pencilDark.svg';
import trashIcon from '@/assets/Icons/trashDark.svg';
import archiveIcon from '@/assets/Icons/archiveDark.svg';
import xcrossIcon from '@/assets/Icons/xcrossDark.svg';
import pentosquareIcon from '@/assets/Icons/pentosquareLight.svg';
import starIcon from '@/assets/Icons/starEmptyLight.svg';

export default function VisitListTable(): JSX.Element {
  const isVet = IsVet();
  const [visitsList, setVisitsList] = useState<Visit[]>([]);
  //visits all used for search bar filtering
  const [visitsAll, setVisitsAll] = useState<Visit[]>([]);
  const [archivedVisits, setArchivedVisits] = useState<Visit[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>(''); // Search term state
  const [emergencyList, setEmergencyList] = useState<EmergencyResponseDTO[]>(
    []
  );
  //emergency all used for search bar filtering
  const [emergencyAll, setEmergencyAll] = useState<EmergencyResponseDTO[]>([]);

  //use sidebar to select which table is shown
  const [currentTab, setCurrentTab] = useState<string | null>('All');

  const navigate = useNavigate();

  useEffect(() => {
    const loadInitialData = async (): Promise<void> => {
      try {
        const [visitsRes, emergenciesRes] = await Promise.allSettled([
          getAllVisits(searchTerm),
          getAllEmergency(),
        ]);

        if (visitsRes.status === 'fulfilled') {
          setVisitsList(visitsRes.value);
          setVisitsAll(visitsRes.value);
        }
        if (emergenciesRes.status === 'fulfilled') {
          setEmergencyList(emergenciesRes.value);
          setEmergencyAll(emergenciesRes.value);
        }
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

    // const eventSource = new EventSource('/visits');
    const API_BASE =
      import.meta.env.VITE_BFF_BASE_URL ?? 'http://localhost:8080';
    const eventSource = new EventSource(`${API_BASE}/api/gateway/visits`, {
      withCredentials: true,
    });

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

  useEffect(() => {
    // Skip EventSource setup for VET role - backend endpoints are ADMIN-only
    // VETs should not reach this component due to route-level restrictions
    if (isVet) {
      return;
    }

    const archivedEventSource = new EventSource(
      `${import.meta.env.VITE_BACKEND_URL}gateway/visits/archived`,
      { withCredentials: true }
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

      setEmergencyList(
        emergencyAll.filter(visit =>
          visit.description.toLowerCase().includes(searchTerm.toLowerCase())
        )
      );
    } else {
      return;
    }
  }, [searchTerm, visitsList, visitsAll, emergencyList, emergencyAll]);

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

  // const tryDeleteEmergency = async (
  //   visitEmergencyId: string
  // ): Promise<void> => {
  //   async () => {
  //     try {
  //       await handleDeleteEmergency(visitEmergencyId);
  //     } catch (error) {
  //       console.error('Error deleting emergency visit:', error);
  //       alert('Failed to delete emergency visit. Please try again.');
  //     }
  //   };
  // };

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
      alert('Emergency visit deleted successfully!');
    } catch (error) {
      console.error(
        `Error deleting emergency with ID ${visitEmergencyId}:`,
        error
      );
      alert('Error deleting emergency visit.');
    }
  };

  const handleArchive = async (visitId: string): Promise<void> => {
    // const confirmArchive = window.confirm(
    //   `Are you sure you want to archive visit with ID: ${visitId}?`
    // );
    // if !(confirmArchive) return;
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
        prev.filter(visit => (visit.visitId === visitId ? updatedVisit : visit))
      );
      alert('Visit archived successfully!');
    } catch (error) {
      console.error('Error archiving visit:', error);
      alert('Error archiving visit.');
    }
  };

  // Handle canceling the visit
  const handleCancel = async (visitId: string): Promise<void> => {
    // const confirmCancel = window.confirm(
    //   'Do you confirm you want to cancel the reservation?'
    // );

    // if (!confirmCancel) return;
    try {
      await axiosInstance.patch(`/visits/${visitId}/CANCELLED`, {
        useV2: false,
      });
      // Fetch the updated visit data from the backend
      const updatedVisitResponse = await axiosInstance.get<Visit>(
        `/visits/${visitId}`,
        {
          useV2: false,
        }
      );

      const updatedVisit = await updatedVisitResponse.data;
      // Update the visit list after cancellation
      setVisitsList(prev =>
        prev.filter(visit => (visit.visitId === visitId ? updatedVisit : visit))
      );
    } catch (error) {
      console.error('Error canceling visit:', error);
      alert('Error canceling visit.');
    }
  };

  // RENDERING

  // Buttons
  const renderTrashButton = (): JSX.Element => (
    <img className="icon" src={trashIcon} title="Delete" />
  );

  const renderCancelButton = (): JSX.Element => (
    <img className="icon" src={xcrossIcon} title="Cancel" />
  );

  const renderArchiveButton = (): JSX.Element => (
    <img className="icon" src={archiveIcon} title="Archive" />
  );

  const renderEditButton = (): JSX.Element => (
    <img className="icon" src={pencilIcon} title="Edit" />
  );

  const renderViewButton = (): JSX.Element => (
    <img className="icon" src={eyeIcon} title="View" />
  );

  // Sidebar

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
            <img src={pentosquareIcon} />
            Create
          </button>
        </li>
        <li>
          <button
            className="btn btn-primary"
            onClick={() => navigate('/reviews')}
            title="Reviews"
          >
            <img src={starIcon} />
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
      <div className="visit-table-section emergency">
        <table>
          <thead>
            <tr>
              <th>Emergency Id</th>
              <th>Pet Id</th>
              <th>Pet Name</th>
              <th>Description</th>
              <th>Pet Birthdate </th>
              <th>Practitionner Id</th>
              <th>Veterinarian</th>
              <th>Vet Email</th>
              <th>Vet Phone Number</th>
              <th>Date</th>
              <th className="action-column"></th>
            </tr>
          </thead>
          <tbody>
            {emergencies.map(emergency => (
              <tr key={emergency.visitEmergencyId}>
                <td>{emergency.visitEmergencyId}</td>
                <td> {emergency.petId}</td>
                <td>{emergency.petName}</td>
                <td>{emergency.description}</td>
                <td> {new Date(emergency.petBirthDate).toLocaleString()}</td>
                <td>{emergency.practitionerId}</td>
                <td>
                  {emergency.vetFirstName} {emergency.vetLastName}
                </td>
                <td>{emergency.vetEmail}</td>
                <td>{emergency.vetPhoneNumber}</td>
                <td>{new Date(emergency.visitDate).toLocaleString()}</td>
                <td className="action-column">
                  <a
                    className="icon"
                    onClick={() =>
                      navigate(
                        `/visits/emergency/${emergency.visitEmergencyId}`
                      )
                    }
                    title="View"
                  >
                    {renderViewButton()}
                  </a>
                  {!isVet && (
                    <a
                      className="icon"
                      onClick={() => {
                        navigate(
                          `/visits/emergency/${emergency.visitEmergencyId}/edit`
                        );
                      }}
                      title="Edit"
                    >
                      {renderEditButton()}
                    </a>
                  )}
                  {!isVet && (
                    <a className="icon" title="Delete">
                      <ConfirmationModal
                        title="Confirm Deletion"
                        showButton={renderTrashButton()}
                        onConfirm={() =>
                          handleDeleteEmergency(emergency.visitEmergencyId)
                        }
                      >
                        <div>hi</div>
                      </ConfirmationModal>
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
  const renderTable = (title: string, visits: Visit[]): JSX.Element =>
    currentTab == title ? (
      <div className="visit-table-section">
        {
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
                <tr key={visit.visitId}>
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
                      {renderViewButton()}
                    </a>
                    {!isVet && (
                      <a
                        className="icon"
                        onClick={() =>
                          navigate(`/visits/${visit.visitId}/edit`)
                        }
                        title="Edit"
                      >
                        {renderEditButton()}
                      </a>
                    )}
                    {visit.status === 'COMPLETED' && !isVet && (
                      // <a
                      //   className="icon"
                      //   onClick={() => handleArchive(visit.visitId)}
                      //   title="Archive"
                      // >
                      //   {renderArchiveButton()}
                      // </a>
                      <ConfirmationModal
                        title="Archive Visit"
                        showButton={renderArchiveButton()}
                        onConfirm={() => handleArchive(visit.visitId)}
                      >
                        <div>Archive Visit?</div>
                      </ConfirmationModal>
                      // </a>
                    )}

                    {visit.status !== 'CANCELLED' &&
                      visit.status !== 'ARCHIVED' &&
                      visit.status !== 'COMPLETED' &&
                      !isVet && (
                        // <a
                        //   className="icon"
                        //   onClick={() => handleCancel(visit.visitId)}
                        //   title="Cancel"
                        // >
                        //   <img src={xcrossIcon} />
                        // </a>
                        // <a className="icon" title="Delete">
                        <ConfirmationModal
                          title="Cancel Visit"
                          showButton={renderCancelButton()}
                          onConfirm={() => handleCancel(visit.visitId)}
                        >
                          <div>Cancel Visit?</div>
                        </ConfirmationModal>
                        // </a>
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
        {/* Emergency Table below buttons, but above visit tables */}
        {renderEmergencyTable('Emergencies', emergencyList)}
        {renderTable('All', visitsList)}
        {renderTable('Confirmed', confirmedVisits)}
        {renderTable('Upcoming', upcomingVisits)}
        {renderTable('Completed', completedVisits)}
        {renderTable('Cancelled', cancelledVisits)}
        {renderTable('Archived', archivedVisits)}
      </div>
    );
  };

  return (
    <div className="visit-page-container">
      {renderSidebar('Visits')} {renderVisitsTables()}
    </div>
  );
}
