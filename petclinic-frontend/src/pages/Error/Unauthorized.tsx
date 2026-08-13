import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import './ErrorPage.css';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';

const Unauthorized: React.FC = () => {
  const navigate = useNavigate();
  //checks if the error reason is the expired session
  const isSessionExpired = sessionStorage.getItem('sessionExpired') === 'true';

  const handleGoLogin = (): void => {
    navigate(AppRoutePaths.Login);
  };

  const handleGoHome = (): void => {
    navigate(AppRoutePaths.Home);
  };
  //clears local storage from the expired information
  if (isSessionExpired) {
    sessionStorage.removeItem('sessionExpired');
  }

  return (
    <div className="error-wrapper">
      <div className="error-container">
        <h1>401</h1>
        {isSessionExpired ? (
          <>
            <p>Your session has expired. Please log in again to continue.</p>
            <button onClick={handleGoLogin}>Go to Login</button>
          </>
        ) : (
          <>
            <p>You are not authorized to access this page or resource.</p>
            <button onClick={handleGoHome}>Go Back Home</button>
          </>
        )}
      </div>
    </div>
  );
};

export default Unauthorized;
