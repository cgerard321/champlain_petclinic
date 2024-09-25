import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './CustomerDetails.css';

const CustomerDetails: React.FC = () => {
    const { ownerId } = useParams<{ ownerId: string }>();
    const navigate = useNavigate();

    const handleEditClick = () => {
        navigate(`/customers/${ownerId}/edit`);
    };

    return (
        <div className="customer-details-card">
            <h2>Customer Details</h2>
            <p><strong>First Name: </strong></p>
            <p><strong>Last Name: </strong></p>
            <p><strong>Address: </strong></p>
            <p><strong>City: </strong></p>
            <p><strong>Province: </strong></p>
            <p><strong>Telephone: </strong></p>
            <div className="customer-details-buttons">
                <button className="customer-details-button" onClick={handleEditClick}>Edit Customer</button>
            </div>
        </div>
    );
};

export default CustomerDetails;
