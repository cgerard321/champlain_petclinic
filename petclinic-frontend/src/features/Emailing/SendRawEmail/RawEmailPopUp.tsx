import { useState } from 'react';
import { RawEmailModelDTO } from '@/features/Emailing/Model/RawEmailModel.ts';
import { SendRawEmail } from '@/features/Emailing/Api/SendRawEmail.tsx';
import './RawEmailCSS.css';

const PopupWithForm: React.FC = () => {
  const [isPopupOpen, setIsPopupOpen] = useState(false);
  const [email, setEmail] = useState('');
  const [emailTitle, setEmailTitle] = useState('');
  const [body, setBody] = useState('');
  const [statusCode, setStatusCode] = useState<number | null>(null);
  const [statusMessage, setStatusMessage] = useState('');

  // Function to open the popup
  const openPopup = (): void => {
    setIsPopupOpen(true);
  };

  // Function to close the popup
  const closePopup = (): void => {
    setIsPopupOpen(false);
    // Reset status on closing
    setStatusCode(null);
    setStatusMessage('');
  };

  // Function to handle the body field and include "Enter" as a newline
  const handleBodyChange = (
    e: React.ChangeEvent<HTMLTextAreaElement>
  ): void => {
    setBody(e.target.value);
  };

  // Function to handle form submission
  const handleSubmit = async (
    e: React.FormEvent<HTMLFormElement>
  ): Promise<void> => {
    e.preventDefault();

    // Prepare payload, converting newlines to <br />
    const payload: RawEmailModelDTO = {
      emailToSendTo: email,
      emailTitle,
      body: body.replace(/\n/g, '<br />'), // Replace newlines with <br />
    };

    // Call the SendRawEmail function
    try {
      const response = await SendRawEmail(payload);
      setStatusCode(response.status);
      setStatusMessage(response.message);
    } catch (error) {
      console.error('Failed to send email:', error);
      setStatusMessage('Failed to send email');
    }
  };

  return (
    <div>
      {/* Button to trigger popup */}
      <button onClick={openPopup}>Send Raw Email</button>

      {/* Conditionally render the popup */}
      {isPopupOpen && (
        <div className="popup-overlay">
          <div className="popup-content">
            <h2>Send Email</h2>
            <form onSubmit={handleSubmit}>
              {/* Email Field */}
              <div>
                <label>Email:</label>
                <input
                  type="email"
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  required
                />
              </div>

              {/* Email Title Field */}
              <div>
                <label>Email Title:</label>
                <input
                  type="text"
                  value={emailTitle}
                  onChange={e => setEmailTitle(e.target.value)}
                  required
                />
              </div>

              {/* Body Field */}
              <div>
                <label>Body:</label>
                <textarea
                  value={body}
                  onChange={handleBodyChange}
                  rows={6} // To make the body field larger
                  required
                />
              </div>

              {/* Submit button */}
              <button type="submit">Submit</button>
            </form>

            {/* Display the status of the email send */}
            {statusCode !== null && (
              <div className="status-message">
                <p>Status Code: {statusCode}</p>
                <p>Message: {statusMessage}</p>
              </div>
            )}

            {/* Button to close the popup */}
            <button onClick={closePopup}>Close</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default PopupWithForm;
