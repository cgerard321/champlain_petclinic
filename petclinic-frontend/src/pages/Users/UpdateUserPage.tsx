import { FC } from 'react';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import UpdateUserForm from '@/features/users/components/UpdateUserForm';

const UpdateUserPage: FC = (): JSX.Element => {
  return (
    <div>
      <NavBar />
      <UpdateUserForm />
    </div>
  );
};
export default UpdateUserPage;
