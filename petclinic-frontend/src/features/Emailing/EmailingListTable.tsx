import { FC, useEffect, useState } from 'react';
import './EmailingListTable.css';
// Example interface for EmailModelResponseDTO
interface EmailModelResponseDTO {
  id: number;
  email: string;
  subject: string;
  body: string; // HTML body
  emailStatus: string;
}

const EmailingListTable: FC = () => {
  const [emails, setEmails] = useState<EmailModelResponseDTO[]>([]);
  const [selectedBody, setSelectedBody] = useState<string | null>(null);

  // Placeholder fetch function to get data from an endpoint
  useEffect(() => {
    const fetchEmails = async (): Promise<void> => {
      try {
        const response = await fetch(
          'http://localhost:8080/api/v2/gateway/emailing'
        );
        const data: EmailModelResponseDTO[] = await response.json();
        setEmails(data);
      } catch (error) {
        console.error('Failed to fetch emails:', error);
      }
    };

    fetchEmails();
  }, []);
  const handleShowBody = (body: string): void => {
    setSelectedBody(body);
  };
  const handleClosePopup = (): void => {
    setSelectedBody(null);
  };
  return (
    <div>
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
                <button onClick={() => handleShowBody(email.body)}>Show</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Popup for displaying the HTML body */}
      {selectedBody && (
        <div className="popup">
          <div className="popup-content">
            <button onClick={handleClosePopup}>Close</button>
            <div dangerouslySetInnerHTML={{ __html: selectedBody }} />
          </div>
        </div>
      )}
    </div>
  );
};

export default EmailingListTable;
