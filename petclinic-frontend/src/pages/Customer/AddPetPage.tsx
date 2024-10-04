import { FC } from 'react';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import AddPetForm from '@/features/customers/components/AddPetForm.tsx';

const AddPetPage : FC = (): JSX.Element => {
  return (
    <div>
      <NavBar />
      <AddPetForm />
    </div>
  );
};
export default AddPetPage;
