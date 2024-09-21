// src/hooks/useAuthRedirect.ts
import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUser } from '@/context/UserContext';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';

export const useAuthRedirect = (): void => {
  const { user } = useUser();
  const navigate = useNavigate();
  // const setUser = useContext(UserContext)?.setUser;

  useEffect(() => {
    if (user.userId === '') {
      navigate(AppRoutePaths.login);
    }
  }, [user, navigate]);
};
