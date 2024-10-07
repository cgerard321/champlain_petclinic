import { FC } from 'react';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import ForgotPasswordForm from '@/features/users/components/ForgotPasswordForm.tsx';

const ForgotPassword: FC = (): JSX.Element => {
  return (
    <div>
      <NavBar />
      <ForgotPasswordForm />
    </div>
  );
};

export default ForgotPassword;
