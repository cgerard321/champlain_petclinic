import { useState, useEffect } from 'react';
import { EmailModelResponseDTO } from '@/features/Emailing/Model/EmailResponse.ts';
import { getAllEmails } from '@/features/Emailing/Api/GetAllEmails.tsx';
import './EmailingListTable.css';

export default function EmailListTable(): JSX.Element {
  const [emails, setEmails] = useState<EmailModelResponseDTO[]>([]);
  const [selectedEmailBody, setSelectedEmailBody] = useState<string | null>(
    null
  );
  const [isPopupOpen, setIsPopupOpen] = useState(false); // To control the popup

  const fetchEmails = async (): Promise<void> => {
    try {
      const data = await getAllEmails();
      setEmails(data);
    } catch (error) {
      console.error('Error fetching emails:', error);
      setEmails([]); // Default to an empty array on error
    }
  };

  useEffect(() => {
    fetchEmails(); // Call the async function to fetch emails
  }, []);

  const openPopup = (htmlBody: string): void => {
    setSelectedEmailBody(htmlBody);
  };

  const closePopup = (): void => {
    setSelectedEmailBody(null);
  };

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
          </div>
        </div>
      )}
    </div>
  );
}
