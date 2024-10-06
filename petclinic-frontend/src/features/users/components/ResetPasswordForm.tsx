import { useState, useEffect, FC } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { processPasswordReset } from '@/features/users/api/processPasswordReset';
import './ResetPasswordForm.css';
import { UserPasswordAndTokenRequestModel } from '@/features/users/model/UserPasswordAndTokenRequestModel.ts';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';

const ResetPasswordForm: FC = (): JSX.Element => {
  const [password, setPassword] = useState<string>('');
  const [confirmPassword, setConfirmPassword] = useState<string>('');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [successMessage, setSuccessMessage] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [passwordStrength, setPasswordStrength] = useState<number>(0);
  const [countdown, setCountdown] = useState<number>(5);

  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();

  useEffect(() => {
    if (successMessage) {
      const timer = setInterval(() => {
        setCountdown(prevCountdown => {
          if (prevCountdown === 1) {
            clearInterval(timer);
            navigate(AppRoutePaths.Login);
          }
          return prevCountdown - 1;
        });
      }, 1000);
      return () => clearInterval(timer);
    }
  }, [successMessage, navigate]);

  useEffect(() => {
    if (countdown > 0 && successMessage) {
      setSuccessMessage(
        `Password reset successfully. Redirecting to login page in ${countdown} seconds...`
      );
    }
  }, [countdown]);

  const validatePassword = (password: string): boolean => {
    const passwordRegex =
      /^(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,}$/;
    return passwordRegex.test(password);
  };

  const calculatePasswordStrength = (password: string): number => {
    let strength = 0;
    if (/[A-Z]/.test(password)) strength++;
    if (/\d/.test(password)) strength++;
    if (/[!@#$%^&*]/.test(password)) strength++;
    if (password.length >= 8) strength++;
    return strength;
  };

  const handlePasswordChange = (
    e: React.ChangeEvent<HTMLInputElement>
  ): void => {
    const newPassword = e.target.value;
    setPassword(newPassword);
    setPasswordStrength(calculatePasswordStrength(newPassword));
  };

  const handleSubmit = async (
    event: React.FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    setErrorMessage('');
    setSuccessMessage('');

    if (!validatePassword(password)) {
      setErrorMessage(
        'Password must have at least one capital letter, one number, one symbol, and be at least 8 characters long.'
      );
      return;
    }
    if (password !== confirmPassword) {
      setErrorMessage('Passwords do not match.');
      return;
    }

    setIsLoading(true);

    const userPasswordAndTokenRequestModel: UserPasswordAndTokenRequestModel = {
      password: password,
      token: token || '',
    };

    try {
      await processPasswordReset(userPasswordAndTokenRequestModel);
      setSuccessMessage(
        `Password reset successfully. Redirecting to login page in ${countdown} seconds...`
      );
    } catch (error) {
      setErrorMessage('An error occurred. Please try again.');
    } finally {
      setIsLoading(false);
    }
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
            <input
              type="password"
              id="password"
              className="reset-password-form-control"
              value={password}
              onChange={handlePasswordChange}
              required
              placeholder="Enter your new password"
            />
            <div
              className={`password-strength-bar strength-${passwordStrength}`}
            ></div>
          </div>

          <div className="reset-password-form-group">
            <label
              htmlFor="confirmPassword"
              className="reset-password-form-label"
            >
              Confirm Password
            </label>
            <input
              type="password"
              id="confirmPassword"
              className="reset-password-form-control"
              value={confirmPassword}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                setConfirmPassword(e.target.value)
              }
              required
              placeholder="Confirm your new password"
            />
          </div>

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
