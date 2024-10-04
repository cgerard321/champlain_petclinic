import { FC } from 'react';
import UpdatePetForm from '@/features/customers/components/UpdatePetForm.tsx';
import { NavBar } from '@/layouts/AppNavBar.tsx';

const UpdateOwnerPetPage: FC = (): JSX.Element => {
  return (
    <div>
      <NavBar />
      <UpdatePetForm />
    </div>
  );
};
export default UpdateOwnerPetPage;
