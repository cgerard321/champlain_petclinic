import { useState, useEffect } from 'react';
import { EmailModelResponseDTO } from '@/features/Emailing/Model/EmailResponse.ts';
import { getAllEmails } from '@/features/Emailing/Api/GetAllEmails.tsx';
import { GetAllFalseVisits } from '../visits/api/GetAllFalseVisits.ts';
import { GetAllTrueVisits } from '../visits/api/GetAllTrueVisits.ts';
import { VisitResponseModel } from '@/features/visits/models/VisitResponseModel.ts';
export default function EmailListTable(): JSX.Element {
  const [emails, setEmails] = useState<EmailModelResponseDTO[]>([]);
  const [selectedEmailBody, setSelectedEmailBody] = useState<string | null>(
    null
  );
  const [falseVisits, setFalseVisits] = useState<VisitResponseModel[]>([]); // Correct type for visits
  const [trueVisits, setTrueVisits] = useState<VisitResponseModel[]>([]); // Correct type for visits
  const [isPopupOpen, setIsPopupOpen] = useState(false); // To control the popup

  // Fetching Emails
  const fetchEmails = async (): Promise<void> => {
    try {
      const data = await getAllEmails();
      setEmails(data);
    } catch (error) {
      console.error('Error fetching emails:', error);
      setEmails([]); // Default to an empty array on error
    }
  };

  // Fetching Visits
  const fetchVisits = async (): Promise<void> => {
    try {
      const fetchedFalseVisits = await GetAllFalseVisits();
      setFalseVisits(fetchedFalseVisits); // Update state with fetched false visits

      const fetchedTrueVisits = await GetAllTrueVisits();
      setTrueVisits(fetchedTrueVisits); // Update state with fetched true visits
    } catch (error) {
      console.error('Error fetching visits:', error);
    }
  };

  // useEffect hook to fetch emails and visits when the component mounts
  useEffect(() => {
    fetchEmails(); // Call the async function to fetch emails
    fetchVisits(); // Call the async function to fetch visits
  }, []);

  const openPopup = (htmlBody: string): void => {
    setSelectedEmailBody(htmlBody);
  };

  const closePopup = (): void => {
    setSelectedEmailBody(null);
  };

  // Rendering Emails
  const renderTable = (emails: EmailModelResponseDTO[]): JSX.Element => (
    <table>
      <thead>
        <tr>
          <th>ID</th>
          <th>Email</th>
          <th>Subject</th>
          <th>Status</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>
        {emails.map(email => (
          <tr key={email.id}>
            <td>{email.id}</td>
            <td>{email.email}</td>
            <td>{email.subject}</td>
            <td>{email.emailStatus}</td>
            <td>
              <button
                className="btn btn-dark"
                onClick={() => openPopup(email.body)}
              >
                Show Body
              </button>
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );

  // Rendering Visits
  const renderVisits = (
    visits: VisitResponseModel[],
    title: string
  ): JSX.Element => (
    <div>
      <h3>{title}</h3>
      <ul>
        {visits.length > 0 ? (
          visits.map((visit, index) => (
            <li key={index}>
              <strong>Pet Name:</strong> {visit.petName} <br />
              <strong>Visit Date:</strong> {visit.visitDate} <br />
              <strong>Veterinarian:</strong> {visit.vetFirstName}{' '}
              {visit.vetLastName} <br />
              <strong>Status:</strong> {visit.status} <br />
              <strong>Reminder:</strong> {visit.reminder ? 'Yes' : 'No'} <br />
              <strong>Visit End Date:</strong> {visit.visitEndDate} <br />
              <strong>Description:</strong> {visit.description} <br />
            </li>
          ))
        ) : (
          <li>No visits to display</li>
        )}
      </ul>
    </div>
  );

  return (
    <div>
      <button className="btn btn-primary" onClick={() => setIsPopupOpen(true)}>
        Show Email List
      </button>

      {isPopupOpen && (
        <div className="popup-overlay">
          <div className="popup-content">
            <button className="close-btn" onClick={() => setIsPopupOpen(false)}>
              Close
            </button>
            <h1>Sent Emails</h1>
            <button className="refresh-button" onClick={() => fetchEmails()}>
              Refresh
            </button>
            {renderTable(emails)}
            {selectedEmailBody && (
              <div className="email-body-popup">
                <button className="close-btn" onClick={closePopup}>
                  Close Email Body
                </button>
                <div dangerouslySetInnerHTML={{ __html: selectedEmailBody }} />
              </div>
            )}
      {renderTable(emails)}
      {renderVisits(falseVisits, 'False Visits')}
      {renderVisits(trueVisits, 'True Visits')}
      {selectedEmailBody && (
        <div className="popup">
          <div className="popup-content">
            <button className="close-btn" onClick={closePopup}>
              Close
            </button>
            <div dangerouslySetInnerHTML={{ __html: selectedEmailBody }} />
          </div>
        </div>
      )}
    </div>
  );
}
