import { FC } from 'react';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import ResetPasswordForm from '@/features/users/components/ResetPasswordForm.tsx';

const ResetPassword: FC = (): JSX.Element => {
  return (
    <div>
      <NavBar />
      <ResetPasswordForm />
    </div>
  );
};

export default ResetPassword;
