// src/context/UserContext.tsx
import { createContext, useContext, useState, ReactNode } from 'react';
import { UserResponseModel } from '@/shared/models/UserResponseModel';
import router from '@/router';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { Role } from '@/shared/models/Role.ts';

interface UserContextType {
  user: UserResponseModel;
  setUser: (user: UserResponseModel) => void;
}

export const UserContext = createContext<UserContextType | undefined>(
  undefined
);

export const UserProvider = ({
  children,
}: {
  children: ReactNode;
}): JSX.Element => {
  const [user, setUserState] = useState<UserResponseModel>(() => {
    // Load the initial user from localStorage, if available
    const storedUser = localStorage.getItem('user');
    return storedUser
      ? JSON.parse(storedUser)
      : {
          email: '',
          roles: new Set<Role>(),
          userId: '',
          username: '',
        };
  });

  const setUser = (newUser: UserResponseModel): void => {
    setUserState(newUser);
    // Save the user to localStorage
    localStorage.setItem('user', JSON.stringify(newUser));
  };
  return (
    <UserContext.Provider value={{ user, setUser }}>
      {children}
    </UserContext.Provider>
  );
};

export const useUser = (): UserContextType => {
  const context = useContext(UserContext);

  if (!context) {
    router.navigate(AppRoutePaths.login);
    return {
      user: { email: '', roles: new Set<Role>(), userId: '', username: '' },
      setUser: () => {},
    };
  }
  return context;
};

export const useSetUser = (): ((user: UserResponseModel) => void) => {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error('useSetUser must be used within a UserProvider');
  }

  return (user: UserResponseModel) => {
    context.setUser(user);
    localStorage.setItem('user', JSON.stringify(user)); // Sync with localStorage
  };
};

export const IsAdmin = (): boolean => {
  const context = useUser();
  return (
    context.user?.roles !== undefined &&
    Array.from(context.user.roles).some((role: Role) => role.name === 'ADMIN')
  );
};

export const IsOwner = (): boolean => {
  const context = useUser();
  return (
    context.user?.roles !== undefined &&
    Array.from(context.user.roles).some((role: Role) => role.name === 'OWNER')
  );
};

export const IsVet = (): boolean => {
  const context = useUser();
  return (
    context.user?.roles !== undefined &&
    Array.from(context.user.roles).some((role: Role) => role.name === 'VET')
  );
};

export const IsInventoryManager = (): boolean => {
  const context = useUser();
  return (
    context.user?.roles !== undefined &&
    Array.from(context.user.roles).some(
      (role: Role) => role.name === 'INVENTORY_MANAGER'
    )
  );
};
