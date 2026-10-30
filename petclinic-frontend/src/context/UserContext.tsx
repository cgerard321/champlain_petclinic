import {
  createContext,
  ReactNode,
  useCallback,
  useContext,
  useEffect,
  useState,
} from 'react';
import router from '@/router';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import axiosInstance from '@/shared/api/axiosInstance';
import { redirectToEmployeePortal } from '@/utils/employeePortal';

export interface ValidateUserTokenResponse {
  username: string;
  userId: string;
  email: string;
  roles: string[];
}
interface CurrentUser {
  username: string;
  email: string;
  userId: string;
  roles: Set<string>;
}

// Whether the user is authenticated, anonymous or authenticated but cannot log into this specific portal
export type SessionStatus = 'authenticated' | 'anonymous' | 'blocked';

interface UserContextType extends CurrentUser {
  user: CurrentUser;
  isAuthenticated: boolean;
  isLoading: boolean;
  checkSession: () => Promise<SessionStatus>;
  logout: () => Promise<void>;
}

const emptyUser: CurrentUser = {
  username: '',
  email: '',
  userId: '',
  roles: new Set<string>(),
};

export const UserContext = createContext<UserContextType | undefined>(
  undefined
);

export const UserProvider = ({
  children,
}: {
  children: ReactNode;
}): JSX.Element => {
  const [user, setUser] = useState<CurrentUser>(emptyUser);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  const checkSession = useCallback(async (): Promise<SessionStatus> => {
    try {
      const response = await axiosInstance.get<ValidateUserTokenResponse>(
        '/users/jwt',
        {
          useV2: false,
          handleLocally: true,
        }
      );

      const data = response.data;

      // Sets Inventory Managers as 'blocked'
      if (data.roles.includes('INVENTORY_MANAGER')) {
        setUser(emptyUser);
        setIsAuthenticated(false);
        return 'blocked';
      }

      setUser({
        username: data.username,
        email: data.email,
        userId: data.userId,
        roles: new Set<string>(data.roles),
      });
      setIsAuthenticated(true);
      return 'authenticated';
    } catch (err) {
      console.error('[UserProvider] checkSession failed:', err);
      setUser(emptyUser);
      setIsAuthenticated(false);
      return 'anonymous';
    } finally {
      setIsLoading(false);
    }
  }, []);

  const logout = useCallback(async (): Promise<void> => {
    try {
      await axiosInstance.post(
        '/users/logout',
        {},
        {
          useV2: false,
          handleLocally: true,
        }
      );
    } catch (err) {
      console.error('[UserProvider] logout failed:', err);
    } finally {
      setUser(emptyUser);
      setIsAuthenticated(false);
    }
  }, []);

  useEffect(() => {
    let cancelled = false;

    const verifySession = async (): Promise<void> => {
      try {
        const status = await checkSession();
        // Redirects blocked users to the Employee Portal
        if (!cancelled && status === 'blocked') {
          redirectToEmployeePortal('/home');
        }
      } catch (err) {
        console.error('[UserProvider] session check failed:', err);
      }
    };

    verifySession();

    return () => {
      cancelled = true;
    };
  }, [checkSession]);

  return (
    <UserContext.Provider
      value={{
        user,
        ...user,
        isAuthenticated,
        isLoading,
        checkSession,
        logout,
      }}
    >
      {children}
    </UserContext.Provider>
  );
};

export const useUser = (): UserContextType => {
  const context = useContext(UserContext);

  if (!context) {
    router.navigate(AppRoutePaths.Login);
    return {
      user: emptyUser,
      ...emptyUser,
      isAuthenticated: false,
      isLoading: false,
      checkSession: async () => 'anonymous',
      logout: async () => {},
    };
  }
  return context;
};

export const IsAdmin = (): boolean => {
  const { roles } = useUser();
  return Array.from(roles).some(role => role === 'ADMIN');
};

export const IsReceptionist = (): boolean => {
  const { roles } = useUser();
  return Array.from(roles).some(role => role === 'RECEPTIONIST');
};

export const IsCustomer = (): boolean => {
  const { roles } = useUser();
  return Array.from(roles).some(role => role === 'CUST');
};

export const IsVet = (): boolean => {
  const { roles } = useUser();
  return Array.from(roles).some(role => role === 'VET');
};

export const IsInventoryManager = (): boolean => {
  const { roles } = useUser();
  return Array.from(roles).some(role => role === 'INVENTORY_MANAGER');
};
