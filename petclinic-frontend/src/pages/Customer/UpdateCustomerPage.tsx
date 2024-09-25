import { FC } from 'react';
import AdminUpdateCustomerForm from '@/features/customers/components/AdminUpdateCustomerForm.tsx';
import { NavBar } from '@/layouts/AppNavBar.tsx';

const UpdateCustomerPage: FC = (): JSX.Element => {
    return (
        <div>
            <NavBar/>
            <AdminUpdateCustomerForm/>
        </div>
    );
};

export default UpdateCustomerPage;
