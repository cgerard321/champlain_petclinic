import { FC, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel';
import { Bill } from '@/features/bills/models/Bill';
import './CustomerDetails.css';

const CustomerDetails: FC = () => {
  const { ownerId } = useParams<{ ownerId: string }>();
  const navigate = useNavigate();

  const [owner, setOwner] = useState<OwnerResponseModel | null>(null);
  const [bills, setBills] = useState<Bill[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchOwnerDetails = async (): Promise<void> => {
      try {
        const ownerResponse = await axios.get(
          `http://localhost:8080/api/v2/gateway/owners/${ownerId}`,
          { withCredentials: true }
        );
        setOwner(ownerResponse.data);

        const billsResponse = await axios.get(
          `http://localhost:8080/api/v2/gateway/bills/customer/${ownerId}`,
          { withCredentials: true }
        );

        const billsData: Bill[] = [];
        const data = billsResponse.data;

        if (typeof data === 'string') {
          const pieces = data.split('\n').filter(Boolean);
          for (const piece of pieces) {
            if (piece.startsWith('data:')) {
              const billData = piece.slice(5).trim();
              try {
                const bill: Bill = JSON.parse(billData);
                billsData.push(bill);
              } catch (error) {
                console.error('Error parsing bill data:', error);
              }
            }
          }
        } else if (Array.isArray(data)) {
          billsData.push(...data);
        } else {
          console.error('Unexpected bills response format:', data);
        }

        setBills(billsData);
        setError(null);
      } catch (err) {
        console.error('Error fetching owner or bills:', err);
        setError('Failed to fetch owner details. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    if (ownerId) {
      fetchOwnerDetails();
    }
  }, [ownerId]);

  const handleEditClick = (): void => {
    navigate(`/customers/${ownerId}/edit`);
  };

  const handleBackClick = (): void => {
    navigate('/customers');
  };

  const handleDelete = async (ownerId: string): Promise<void> => {
    const confirmDelete = window.confirm(
      'Are you sure you want to delete this owner?'
    );

    if (confirmDelete) {
      try {
        await axios.delete(
          `http://localhost:8080/api/v2/gateway/owners/${ownerId}`,
          {
            withCredentials: true,
          }
        );

        alert('Owner deleted successfully.');
        navigate('/customers');
      } catch (error) {
        console.error('Error deleting owner:', error);
        alert('Error deleting owner. Please try again.');
      }
    } else {
      alert('Owner deletion canceled.');
    }
  };

  if (loading) {
    return <p>Loading...</p>;
  }

  if (error) {
    return <p>Error: {error}</p>;
  }

  if (!owner) {
    return <p>No owner found.</p>;
  }

  const petTypeMapping: { [key: string]: string } = {
    '1': 'Cat',
    '2': 'Dog',
    '3': 'Lizard',
    '4': 'Snake',
    '5': 'Bird',
    '6': 'Hamster',
  };

  const calculateAge = (birthDate: Date): number => {
    const birth = new Date(birthDate);
    const ageDiffMs = Date.now() - birth.getTime();
    const ageDate = new Date(ageDiffMs);
    return Math.abs(ageDate.getUTCFullYear() - 1970);
  };

  return (
    <div className="customer-details-card">
      <h2>
        {' '}
        Customer Details for {owner.firstName} {owner.lastName}{' '}
      </h2>

      <div className="customer-details-container">
        {/* Owner Info */}
        <div className="section owner-info">
          <h3>Owner Info</h3>
          <p>
            <strong>First Name: </strong>
            {owner.firstName}
          </p>
          <p>
            <strong>Last Name: </strong>
            {owner.lastName}
          </p>
          <p>
            <strong>Address: </strong>
            {owner.address}
          </p>
          <p>
            <strong>City: </strong>
            {owner.city}
          </p>
          <p>
            <strong>Province: </strong>
            {owner.province}
          </p>
          <p>
            <strong>Telephone: </strong>
            {owner.telephone}
          </p>
        </div>

        {/* Owner Pets */}
        <div className="section owner-pets">
          <h3>Owner Pets</h3>
          {owner.pets && owner.pets.length > 0 ? (
            <ul>
              {owner.pets.map(pet => (
                <li key={pet.petId}>
                  <strong>Name: </strong>
                  {pet.name}, <strong>Type: </strong>
                  {petTypeMapping[pet.petTypeId] || 'Unknown'},{' '}
                  <strong>Weight: </strong>
                  {pet.weight}kg,<strong> Age: </strong>
                  {calculateAge(pet.birthDate)}
                </li>
              ))}
            </ul>
          ) : (
            <p>No pets found.</p>
          )}
        </div>

        {/* Owner Bills */}
        <div className="section owner-bills">
          <h3>Owner Bills</h3>
          {Array.isArray(bills) && bills.length > 0 ? (
            <ul>
              {bills.map(bill => (
                <li key={bill.billId}>
                  <strong>Bill ID: </strong>
                  {bill.billId}, <strong>Amount: </strong>
                  {bill.amount}, <strong>Date: </strong>
                  {bill.date}
                </li>
              ))}
            </ul>
          ) : (
            <p>No bills found.</p>
          )}
        </div>
      </div>

      <div className="customer-details-buttons">
        <button className="customer-details-button" onClick={handleEditClick}>
          Edit Customer
        </button>
        <button className="customer-details-button" onClick={handleBackClick}>
          Back to All Owners
        </button>
        <button
          className="btn btn-danger"
          onClick={() => handleDelete(owner.ownerId)}
          title="Delete"
          style={{ backgroundColor: 'red', color: 'white' }}
        >
          Delete Owner
        </button>
      </div>
    </div>
  );
};

export default CustomerDetails;
