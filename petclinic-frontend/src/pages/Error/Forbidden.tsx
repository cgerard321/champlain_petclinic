import React from 'react';
import { useNavigate } from 'react-router-dom';
import './ErrorPage.css';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';

const Forbidden: React.FC = () => {
  const navigate = useNavigate();

  const handleGoHome = () => {
    navigate(AppRoutePaths.Home);
  };

  return (
    <div className="error-wrapper">
      <div className="error-container">
        <h1>403</h1>
        <p>Sorry, you don't have permission to access this page.</p>
        <button onClick={handleGoHome}>Go Back Home</button>
      </div>
    </div>
  );
};

export default Forbidden;
