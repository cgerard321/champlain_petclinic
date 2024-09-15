import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import './ErrorPage.css';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';

const ServiceUnavailable: React.FC = () => {
  const navigate = useNavigate();

  const handleGoHome = (): void => {
    navigate(AppRoutePaths.Home);
  };

  return (
    <div className="error-wrapper">
      <div className="error-container">
        <h1>503</h1>
        <p>The server is currently unavailable. Please try again later.</p>
        <button onClick={handleGoHome}>Go Back Home</button>
      </div>
    </div>
  );
};

export default ServiceUnavailable;