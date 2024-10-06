import { useState, FC } from 'react';
import { processPasswordReset } from '@/features/users/api/processPasswordReset';
import { UserPasswordAndTokenRequestModel } from '@/features/users/model/UserPasswordAndTokenRequestModel.ts';
import { useParams } from 'react-router-dom';
import './ResetPasswordForm.css';

const ResetPasswordForm: FC = (): JSX.Element => {
  const { token } = useParams<{ token: string }>();
  const [password, setPassword] = useState<string>('');
  const [confirmPassword, setConfirmPassword] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [passwordError, setPasswordError] = useState<string>('');
  const [showPassword, setShowPassword] = useState<boolean>(false);

  const validatePasswordStrength = (password: string): boolean => {
    const passwordRegex =
      /^(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/;
    return passwordRegex.test(password);
  };

  const handleSubmit = async (
    event: React.FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    setIsLoading(true);
    setSuccessMessage('');
    setErrorMessage('');
    setPasswordError('');

    if (password !== confirmPassword) {
      setPasswordError('Passwords do not match');
      setIsLoading(false);
      return;
    }

    if (!validatePasswordStrength(password)) {
      setPasswordError(
        'Password must be at least 8 characters long, contain one capital letter, one number, and one symbol.'
      );
      setIsLoading(false);
      return;
    }

    const userPasswordAndTokenRequestModel: UserPasswordAndTokenRequestModel = {
      password,
      token: token || '', // Use the token from the URL; fallback to an empty string if undefined
    };

    try {
      await processPasswordReset(userPasswordAndTokenRequestModel);
      setSuccessMessage('Password has been reset successfully.');
    } catch (error) {
      setErrorMessage('Failed to reset password. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const togglePasswordVisibility = (): void => {
    setShowPassword(prevState => !prevState);
  };

  return (
    <div className="reset-password-form-container">
      <div className="reset-password-form-card">
        <h2 className="reset-password-form-title">Reset Password</h2>
        <form onSubmit={handleSubmit}>
          <div className="reset-password-form-group">
            <label htmlFor="password" className="reset-password-form-label">
              New Password
            </label>
            <div className="reset-password-form-input-container">
              <input
                type={showPassword ? 'text' : 'password'}
                id="password"
                className="reset-password-form-control"
                value={password}
                onChange={(e: React.ChangeEvent<HTMLInputElement>): void =>
                  setPassword(e.target.value)
                }
                required
                disabled={isLoading}
                placeholder="Enter new password"
              />
              <button
                type="button"
                className="reset-password-form-toggle-visibility"
                onClick={togglePasswordVisibility}
                disabled={isLoading}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? '🙈' : '👁️'}
              </button>
            </div>
          </div>
          <div className="reset-password-form-group">
            <label
              htmlFor="confirmPassword"
              className="reset-password-form-label"
            >
              Confirm Password
            </label>
            <div className="reset-password-form-input-container">
              <input
                type={showPassword ? 'text' : 'password'}
                id="confirmPassword"
                className="reset-password-form-control"
                value={confirmPassword}
                onChange={(e: React.ChangeEvent<HTMLInputElement>): void =>
                  setConfirmPassword(e.target.value)
                }
                required
                disabled={isLoading}
                placeholder="Confirm new password"
              />
              <button
                type="button"
                className="reset-password-form-toggle-visibility"
                onClick={togglePasswordVisibility}
                disabled={isLoading}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? '🙈' : '👁️'}
              </button>
            </div>
          </div>
          {passwordError && (
            <div className="reset-password-form-alert-error">
              {passwordError}
            </div>
          )}
          {errorMessage && (
            <div className="reset-password-form-alert-error">
              {errorMessage}
            </div>
          )}
          {successMessage && (
            <div className="reset-password-form-alert-success">
              {successMessage}
            </div>
          )}
          <button
            type="submit"
            className="reset-password-form-btn"
            disabled={isLoading}
          >
            {isLoading ? 'Resetting...' : 'Reset Password'}
          </button>
        </form>
      </div>
    </div>
  );
};

export default ResetPasswordForm;
