import React from 'react';
import { useNavigate } from 'react-router-dom';
import './PageNotFound.css'
import { AppRoutePaths } from '@/shared/models/path.routes.ts';

const NotFound: React.FC = () => {
  const navigate = useNavigate();

  const handleGoHome = () => {
    navigate(AppRoutePaths.Home);
  };

  return (
    <div className="not-found-wrapper">
      <div className="not-found-container">
        <h1>404</h1>
        <p>Oops! The page you're looking for doesn't exist.</p>
        <button onClick={handleGoHome}>Go Back Home</button>
      </div>
    </div>
  );
};

export default NotFound;