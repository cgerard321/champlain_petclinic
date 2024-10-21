import { useEffect, useState } from 'react';
import { GetAllReceivedEmails } from '@/features/Emailing/Api/GetAllReceivedEmails'; // Adjust the import based on your structure
import { ReceivedEmailModel } from '@/features/Emailing/Model/ReceivedEmailModel';

const EmailTable: React.FC = () => {
  const [emails, setEmails] = useState<ReceivedEmailModel[]>([]);
  const [selectedEmail, setSelectedEmail] = useState<ReceivedEmailModel | null>(
    null
  );
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect((): (() => void) => {
    const fetchEmails = async (): Promise<void> => {
      const receivedEmails = await GetAllReceivedEmails();
      const formattedEmails = receivedEmails.map(email => ({
        ...email,
        dateReceived: new Date(email.dateReceived), // Ensure dateReceived is a Date object
      }));
      setEmails(formattedEmails);
    };

    fetchEmails();

    // Poll for new emails every 5 seconds (adjust as needed)
    const intervalId = setInterval(fetchEmails, 5000);
    return () => clearInterval(intervalId); // Cleanup interval on component unmount
  }, []);

  const handleShowBody = (email: ReceivedEmailModel): void => {
    setSelectedEmail(email);
    setIsModalOpen(true);
  };

  const closeModal = (): void => {
    setIsModalOpen(false);
    setSelectedEmail(null);
  };

  return (
    <div>
      <h2>Received Emails</h2>
      <table>
        <thead>
          <tr>
            <th>From</th>
            <th>Subject</th>
            <th>Date Received</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {emails.map((email, index) => (
            <tr
              key={`${email.from}-${email.dateReceived?.toString() || index}`}
            >
              <td>{email.from}</td>
              <td>{email.subject}</td>
              <td>{email.dateReceived.toLocaleString()}</td>
              <td>
                <button onClick={() => handleShowBody(email)}>
                  Display Body
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {isModalOpen && selectedEmail && (
        <div className="modal">
          <div className="modal-content">
            <span className="close" onClick={closeModal}>
              &times;
            </span>
            <h3>Email Body</h3>
            <p>{selectedEmail.plainTextBody}</p>
          </div>
        </div>
      )}
    </div>
  );
};

export default EmailTable;
