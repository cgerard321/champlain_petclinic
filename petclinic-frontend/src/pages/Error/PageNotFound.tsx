import React from 'react';
import { useNavigate } from 'react-router-dom';
import './ErrorPage.css'
import { AppRoutePaths } from '@/shared/models/path.routes.ts';

const NotFound: React.FC = () => {
  const navigate = useNavigate();

  const handleGoHome = () => {
    navigate(AppRoutePaths.Home);
  };

  return (
    <div className='error-wrapper'>
      <div className='error-container'>
        <h1>404</h1>
        <p>Oops! The page you're looking for doesn't exist.</p>
        <button onClick={handleGoHome}>Go Back Home</button>
      </div>
    </div>
  );
};

export default NotFound;