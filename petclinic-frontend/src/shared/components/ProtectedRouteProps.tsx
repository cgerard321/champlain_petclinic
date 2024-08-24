// src/components/ProtectedRoute.tsx
import { ReactNode } from 'react';
import { useAuthRedirect } from '@/shared/hooks/useAuthRedirect';

interface ProtectedRouteProps {
  children: ReactNode;
}

export const ProtectedRoute = ({
  children,
}: ProtectedRouteProps): JSX.Element => {
  useAuthRedirect();
  return <>{children}</>;
};
