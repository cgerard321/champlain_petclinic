import { useCallback } from 'react';
import { useUser } from '@/context/UserContext.tsx';

export const useHasRequiredRole = (roles?: string[]): boolean => {
  const { user } = useUser();

  const hasRequiredRole = useCallback((): boolean => {
    if (!roles) return true; // If no role is specified, everyone can access it
    return roles.some(role =>
      Array.from(user.roles).some(userRole => userRole.name === role)
    );
  }, [roles, user.roles]);

  return hasRequiredRole();
};
