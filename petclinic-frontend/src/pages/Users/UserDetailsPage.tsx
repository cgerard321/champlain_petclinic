import { FC } from 'react';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import UserDetails from '@/features/users/components/UserDetails';

const UserDetailsPage: FC = (): JSX.Element => {
  return (
    <div>
      <NavBar />
      <UserDetails />
    </div>
  );
};

export default UserDetailsPage;
