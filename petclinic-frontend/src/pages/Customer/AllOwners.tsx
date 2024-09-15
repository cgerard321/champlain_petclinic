import * as React from 'react';
import { useEffect, useState } from 'react';
import { getallOwners } from '@/features/customers/api/getallOwners.ts';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel.ts';
import './AllOwners.css';

const AllOwners: React.FC = (): JSX.Element => {

    const [owners, setOwners] = useState<OwnerResponseModel[]>([]);

    useEffect(() => {
        const fetchOwnersData = async (): Promise<void> => {
            try {
                const response = await getallOwners();
                const ownersData: OwnerResponseModel[] = response.data;
                setOwners(ownersData);
            } catch (error) {
                console.error('Error fetching owners data:', error);
            }
        };

        fetchOwnersData().catch(error =>
            console.error('Error in fetchOwnersData:', error)
        );
    }, []);

    return (
        <div className="owners-container">
            <h1>Owners</h1>
            <table>
                <thead>
                <tr>
                    <th>First Name</th>
                    <th>Last Name</th>
                    <th>Address</th>
                    <th>City</th>
                    <th>Province</th>
                    <th>Telephone</th>
                </tr>
                </thead>
                <tbody>
                {owners.map(owner => (
                    <tr key={owner.ownerId}>
                        <td>{owner.firstName}</td>
                        <td>{owner.lastName}</td>
                        <td>{owner.address}</td>
                        <td>{owner.city}</td>
                        <td>{owner.province}</td>
                        <td>{owner.telephone}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default AllOwners;