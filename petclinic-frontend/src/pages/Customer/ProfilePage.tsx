import * as React from 'react';
import CustomerProfilePage from '@/features/customers/components/CustomerProfilePage.tsx';
import UpdateCustomerForm from '@/features/customers/components/NewUpdateCustomerForm.tsx';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import './ProfilePage.css';

const ProfilePage: React.FC = (): JSX.Element => {
  const [edit, setEdit] = React.useState<boolean>(false);

  const changeToProfilePage = (): void => {
    setEdit(false);
  };

  return (
    <div>
      <NavBar />
      {edit ? (
        <UpdateCustomerForm onBackButtonClick={changeToProfilePage} />
      ) : (
        <CustomerProfilePage />
      )}
    </div>
  );
};

export default ProfilePage;
