import { useState, FC } from 'react';
import { sendForgotPasswordEmail } from '@/features/users/api/sendForgotPasswordEmail';
import { UserRequestEmailModel } from '@/features/users/model/UserRequestEmailModel.ts';

const ForgotPasswordForm: FC = (): JSX.Element => {
  const [email, setEmail] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [successMessage, setSuccessMessage] = useState<string>('');

  const handleSubmit = async (
    event: React.FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    setIsLoading(true);
    setSuccessMessage('');

    const userRequestEmailModel: UserRequestEmailModel = {
      email: email,
      url: 'http://localhost:3000/users/reset-password/',
    };

    try {
      await sendForgotPasswordEmail(userRequestEmailModel);
      setSuccessMessage('Password reset email sent successfully.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="forgot-password-form-container">
      <div className="forgot-password-form-card">
        <h2 className="forgot-password-form-title">Forgot Password</h2>
        <form onSubmit={handleSubmit}>
          <div className="forgot-password-form-group">
            <label htmlFor="email" className="forgot-password-form-label">
              Email
            </label>
            <input
              type="email"
              id="email"
              className="forgot-password-form-control"
              value={email}
              onChange={(e: React.ChangeEvent<HTMLInputElement>): void =>
                setEmail(e.target.value)
              }
              required
              disabled={isLoading}
              placeholder="Enter your email address"
            />
          </div>
          {successMessage && (
            <div className="forgot-password-form-alert-success">
              {successMessage}
            </div>
          )}
          <button
            type="submit"
            className="forgot-password-form-btn"
            disabled={isLoading}
          >
            {isLoading ? 'Sending...' : 'Send Email'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default ForgotPasswordForm;
