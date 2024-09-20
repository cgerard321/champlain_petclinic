import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import './ErrorPage.css';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';

const RequestTimeout: React.FC = () => {
  const navigate = useNavigate();

  const handleGoHome = (): void => {
    navigate(AppRoutePaths.Home);
  };

  return (
    <div className="error-wrapper">
      <div className="error-container">
        <h1>408</h1>
        <p>The server timed out waiting for the request.</p>
        <button onClick={handleGoHome}>Go Back Home</button>
      </div>
    </div>
  );
};

export default RequestTimeout;
