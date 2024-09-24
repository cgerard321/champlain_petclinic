import * as React from 'react';
import UpdateCustomerForm from '@/features/customers/components/UpdateCustomerForm.tsx';
import { NavBar } from '@/layouts/AppNavBar.tsx';

const UpdateCustomer: React.FC = (): JSX.Element => {
    return (
        <div>
            <NavBar />
            <UpdateCustomerForm />
        </div>
    );
};

export default UpdateCustomer;
