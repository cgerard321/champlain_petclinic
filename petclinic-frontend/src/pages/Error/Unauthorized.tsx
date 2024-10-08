import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import './ErrorPage.css';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';

const Unauthorized: React.FC = () => {
  const navigate = useNavigate();

  const handleGoHome = (): void => {
    navigate(AppRoutePaths.Home);
  };

  return (
    <div className="error-wrapper">
      <div className="error-container">
        <h1>401</h1>
        <p>You are not authorized to access this page or resource.</p>
        <button onClick={handleGoHome}>Go Back Home</button>
      </div>
    </div>
  );
};

export default Unauthorized;
