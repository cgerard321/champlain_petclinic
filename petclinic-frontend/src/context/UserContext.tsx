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

interface UserContextType extends CurrentUser {
  user: CurrentUser;
  isAuthenticated: boolean;
  isLoading: boolean;
  checkSession: () => Promise<void>;
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

  const checkSession = useCallback(async (): Promise<void> => {
    try {
      const response = await axiosInstance.get<ValidateUserTokenResponse>(
        '/users/jwt',
        {
          useV2: false,
          handleLocally: true,
        }
      );

      const data = response.data;
      setUser({
        username: data.username,
        email: data.email,
        userId: data.userId,
        roles: new Set<string>(data.roles),
      });
      setIsAuthenticated(true);
    } catch (err) {
      console.error('[UserProvider] checkSession failed:', err);
      setUser(emptyUser);
      setIsAuthenticated(false);
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
    checkSession();
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
      checkSession: async () => {},
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

export const IsOwner = (): boolean => {
  const { roles } = useUser();
  return Array.from(roles).some(role => role === 'OWNER');
};

export const IsVet = (): boolean => {
  const { roles } = useUser();
  return Array.from(roles).some(role => role === 'VET');
};

export const IsInventoryManager = (): boolean => {
  const { roles } = useUser();
  return Array.from(roles).some(role => role === 'INVENTORY_MANAGER');
};
