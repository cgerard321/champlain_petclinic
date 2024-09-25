import { FC } from 'react';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import CustomerDetails from "@/features/customers/components/CustomerDetails.tsx";

const CustomerDetailsPage: FC = (): JSX.Element => {
    return (
        <div>
            <NavBar/>
            <CustomerDetails/>
        </div>
    );
};

export default CustomerDetailsPage;
