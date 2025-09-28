import { FC, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '@/shared/api/axiosInstance';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel'; // Import the PetResponseModel
import { Bill } from '@/features/bills/models/Bill';
import { getOwner } from '../api/getOwner';
import './CustomerDetails.css';
import { deleteOwner } from '../api/deleteOwner';
import { IsVet } from '@/context/UserContext';

const CustomerDetails: FC = () => {
  const { ownerId } = useParams<{ ownerId: string }>();
  const navigate = useNavigate();
  const isVet = IsVet();

  const [isDisabled, setIsDisabled] = useState<boolean>(false);
  const [owner, setOwner] = useState<OwnerResponseModel | null>(null);
  const [pets, setPets] = useState<PetResponseModel[]>([]); // State for pets
  const [bills, setBills] = useState<Bill[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchOwnerDetails = async (): Promise<void> => {
      //ownerId can't be undefied here so it is ok to assert it.
      const ownerResponse = await getOwner(ownerId!);
      setOwner(ownerResponse.data);

      const userResponse = await axiosInstance.get(`/users/${ownerId}`, {
        useV2: true,
      });
      setIsDisabled(userResponse.data.disabled);

      // Fetch pets by owner ID
      const petsResponse = await axiosInstance.get(
        `/pets/owners/${ownerId}/pets`,
        {
          useV2: true,
        }
      );
      setPets(petsResponse.data); // Set the pets state

      const billsResponse = await axiosInstance.get(
        `/bills/customer/${ownerId}`,
        { useV2: false }
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
      setLoading(false);
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
      await deleteOwner(ownerId);
      alert('Owner deleted successfully.');
      navigate('/customers');
    } else {
      alert('Owner deletion canceled.');
    }
  };

  if (loading) {
    return <p>Loading...</p>;
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

  const handleDisableEnable = async (): Promise<void> => {
    const confirmAction = window.confirm(
      `Are you sure you want to ${isDisabled ? 'enable' : 'disable'} this user's account?`
    );

    if (confirmAction) {
      if (isDisabled) {
        await axiosInstance.patch(`/users/${ownerId}/enable`, {
          useV2: true,
        });
        alert('User account enabled successfully.');
      } else {
        await axiosInstance.patch(`/users/${ownerId}/disable`, {
          useV2: true,
        });
        alert('User account disabled successfully.');
      }
      setIsDisabled(!isDisabled);
    }
  };

  const handleEditPetClick = (petId: string): void => {
    navigate(`/owners/${ownerId}/pets/${petId}/edit`);
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
          {pets && pets.length > 0 ? (
            <ul>
              {pets.map(pet => (
                <li key={pet.petId}>
                  <strong>Pet ID: </strong>
                  {pet.petId}
                  <strong>Name: </strong>
                  {pet.name}, <strong>Type: </strong>
                  {petTypeMapping[pet.petTypeId] || 'Unknown'},{' '}
                  <strong>Weight: </strong>
                  {pet.weight}kg,<strong> Age: </strong>
                  {calculateAge(pet.birthDate)}
                  <button
                    className="edit-pet-button"
                    onClick={() => handleEditPetClick(pet.petId)}
                    style={{ marginLeft: '10px' }}
                  >
                    Edit Pet
                  </button>
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
          className="add-pet-button"
          onClick={() => navigate(`/owners/${ownerId}/pets/new`)}
        >
          Add New Pet
        </button>
        {!isVet && (
          <button
            className="btn btn-danger"
            onClick={() => handleDelete(owner.ownerId)}
            title="Delete"
            style={{ backgroundColor: 'red', color: 'white' }}
          >
            Delete Owner
          </button>
        )}
        <button
          className={`btn ${isDisabled ? 'btn-success' : 'btn-warning'}`}
          onClick={handleDisableEnable}
        >
          {isDisabled ? 'Enable Account' : 'Disable Account'}
        </button>
      </div>
    </div>
  );
};

export default CustomerDetails;
