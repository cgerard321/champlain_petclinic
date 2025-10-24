import { FC, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '@/shared/api/axiosInstance';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel';
import { PetTypeModel } from '@/features/customers/models/PetTypeModel';
import { UserDetailsModel } from '@/features/customers/models/UserDetailsModel';
import { Bill } from '@/features/bills/models/Bill';
import { getOwner } from '../api/getOwner';
import { getPetTypes } from '../api/getPetTypes';
import { getPetTypeName } from '../utils/petTypeMapping';
import './CustomerDetails.css';
import { deleteOwner } from '../api/deleteOwner';
import { IsVet } from '@/context/UserContext';
import EditPetModal from './EditPetModal';
import AddPetModal from './AddPetModal';
import defaultPetPicture from '@/assets/Owners/defaultProfilePicture.png';

const CustomerDetails: FC = () => {
  const { ownerId } = useParams<{ ownerId: string }>();
  const navigate = useNavigate();
  const isVet = IsVet();

  const [isDisabled, setIsDisabled] = useState<boolean>(false);
  const [owner, setOwner] = useState<OwnerResponseModel | null>(null);
  const [userDetails, setUserDetails] = useState<UserDetailsModel | null>(null);
  const [pets, setPets] = useState<PetResponseModel[]>([]);
  const [petTypes, setPetTypes] = useState<PetTypeModel[]>([]);
  const [bills, setBills] = useState<Bill[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [isAddPetModalOpen, setIsAddPetModalOpen] = useState<boolean>(false);
  const [isEditPetModalOpen, setIsEditPetModalOpen] = useState<boolean>(false);
  const [selectedPetId, setSelectedPetId] = useState<string>('');

  useEffect(() => {
    const fetchOwnerDetails = async (): Promise<void> => {
      //ownerId can't be undefied here so it is ok to assert it.
      const ownerResponse = await getOwner(ownerId!);
      setOwner(ownerResponse.data);

      const userResponse = await axiosInstance.get(`/users/${ownerId}`, {
        useV2: false,
      });
      setUserDetails(userResponse.data);
      setIsDisabled(userResponse.data.disabled);

      // Fetch pets by owner ID
      const petsResponse = await axiosInstance.get(
        `/pets/owners/${ownerId}/pets`,
        {
          useV2: false,
          params: { includePhoto: true },
        }
      );
      setPets(petsResponse.data);

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

      const petTypesData = await getPetTypes();
      setPetTypes(petTypesData);

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
    setSelectedPetId(petId);
    setIsEditPetModalOpen(true);
  };

  const handleCloseEditPetModal = (): void => {
    setIsEditPetModalOpen(false);
    setSelectedPetId('');
  };

  //eliminated code duplication
  const fetchOwnerDetails = async (): Promise<void> => {
    if (!ownerId) return;

    try {
      const ownerResponse = await getOwner(ownerId);
      setOwner(ownerResponse.data);

      const userResponse = await axiosInstance.get(`/users/${ownerId}`, {
        useV2: true,
      });
      setIsDisabled(userResponse.data.disabled);

      const petsResponse = await axiosInstance.get(
        `/pets/owners/${ownerId}/pets`,
        {
          useV2: false,
          params: { includePhoto: true },
        }
      );
      setPets(petsResponse.data);

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
    } catch (error) {
      console.error('Error fetching owner details:', error);
      setLoading(false);
    }
  };

  const handleAddPet = (): void => {
    setIsAddPetModalOpen(true);
  };

  const handleCloseAddPetModal = (): void => {
    setIsAddPetModalOpen(false);
  };

  const handlePetAdded = (newPet: PetResponseModel): void => {
    setPets(prevPets => [...prevPets, newPet]);
  };

  const handlePetUpdated = (): void => {
    fetchOwnerDetails();
  };

  const handlePetDeleted = (): void => {
    fetchOwnerDetails();
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
            <strong>Username: </strong>
            {userDetails?.username || 'Loading...'}
          </p>
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
                <li key={pet.petId} className="pet-item">
                  <img
                    src={
                      pet.photo?.data
                        ? `data:${pet.photo.contentType};base64,${pet.photo.data}`
                        : defaultPetPicture
                    }
                    alt={`${pet.name}'s photo`}
                    className="pet-photo-thumbnail"
                    style={{
                      width: '60px',
                      height: '60px',
                      borderRadius: '50%',
                      objectFit: 'cover',
                      marginRight: '15px',
                      border: '1px solid #ddd',
                    }}
                  />
                  <div className="pet-details">
                    <div className="pet-info">
                      <span className="pet-id">Pet ID: {pet.petId}</span>
                    </div>
                    <div className="pet-main-info">
                      <span className="pet-name">
                        <strong>Name:</strong> {pet.name}
                      </span>
                      <span className="pet-type">
                        <strong>Type:</strong>{' '}
                        {getPetTypeName(pet.petTypeId, petTypes)}
                      </span>
                      <span className="pet-weight">
                        <strong>Weight:</strong> {pet.weight}kg
                      </span>
                      <span className="pet-age">
                        <strong>Age:</strong> {calculateAge(pet.birthDate)}{' '}
                        years
                      </span>
                    </div>
                    <div className="pet-actions">
                      <button
                        className="edit-pet-button"
                        onClick={() => handleEditPetClick(pet.petId)}
                      >
                        Edit Pet
                      </button>
                    </div>
                  </div>
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
        <button className="add-pet-button" onClick={handleAddPet}>
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

      <AddPetModal
        ownerId={ownerId || ''}
        isOpen={isAddPetModalOpen}
        onClose={handleCloseAddPetModal}
        onPetAdded={handlePetAdded}
      />

      <EditPetModal
        isOpen={isEditPetModalOpen}
        onClose={handleCloseEditPetModal}
        petId={selectedPetId}
        ownerId={ownerId || ''}
        onPetUpdated={handlePetUpdated}
        onPetDeleted={handlePetDeleted}
      />
    </div>
  );
};

export default CustomerDetails;
