import React, { useEffect, useState } from 'react';
import { getOwner } from '@/features/customers/api/updateOwner.ts';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel.ts';
import { useUser } from '@/context/UserContext';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import './ProfilePage.css';
import {AppRoutePaths} from "@/shared/models/path.routes.ts";
import {useNavigate} from "react-router-dom";


const ProfilePage: React.FC = (): JSX.Element => {
    const { user } = useUser();
    const [owner, setOwner] = useState<OwnerResponseModel | null>(null);
    const [error, setError] = useState<string | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchOwnerData = async (): Promise<void> => {
            try {
                const response = await getOwner(user.userId);
                setOwner(response.data);
            } catch (error) {
                setError('Error fetching owner data');
                console.error('Error fetching owner data:', error);
            }
        };



        fetchOwnerData();
    }, [user.userId]);

    const handleUpdateClick = () => {
        navigate(AppRoutePaths.CustomerProfileEdit); // Redirect to ProfileEdit page
    };

    if (error) {
        return <p>{error}</p>;
    }

    if (!owner) {
        return <p>Loading...</p>;
    }

    return (
        <div>
            <NavBar />
            <div className="profile-page container-profile">
                <div className="profile-card shadow-lg p-5 mb-5 bg-white rounded">
                    <h1>{owner.firstName} {owner.lastName}'s Profile</h1>
                    <div className="profile-info">
                        <p><strong>First Name:</strong> {owner.firstName}</p>
                        <p><strong>Last Name:</strong> {owner.lastName}</p>
                        <p><strong>Address:</strong> {owner.address}</p>
                        <p><strong>City:</strong> {owner.city}</p>
                        <p><strong>Province:</strong> {owner.province}</p>
                        <p><strong>Telephone:</strong> {owner.telephone}</p> 
                    </div>
                    <button
                        className="updateButton"
                        onClick={handleUpdateClick}
                    >
                        Update Profile
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ProfilePage;
