import { ReactNode, useEffect } from 'react';
import { useAuthRedirect } from '@/shared/hooks/useAuthRedirect';
import { useHasRequiredRole } from '@/shared/hooks/useHasRequiredRole';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { useNavigate } from 'react-router-dom';

interface ProtectedRouteProps {
  children: ReactNode;
  roles?: string[];
}

export const ProtectedRoute = ({
  children,
  roles,
}: ProtectedRouteProps): JSX.Element => {
  const navigate = useNavigate();
  const hasRequiredRole = useHasRequiredRole(roles);

  useEffect(() => {
    if (!hasRequiredRole) {
      navigate(AppRoutePaths.Forbidden);
    }
  }, [hasRequiredRole, navigate]);

  useAuthRedirect();

  return <>{children}</>;
};
