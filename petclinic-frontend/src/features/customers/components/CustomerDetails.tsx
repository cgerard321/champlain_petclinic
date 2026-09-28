import { FC, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axiosInstance from '@/shared/api/axiosInstance';
import { CustomerResponseModel } from '@/features/customers/models/CustomerResponseModel.ts';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel';
import { PetTypeModel } from '@/features/customers/models/PetTypeModel';
import { UserDetailsModel } from '@/features/customers/models/UserDetailsModel';
import { Bill } from '@/features/bills/models/Bill';
import { getCustomer } from '../api/getCustomer.ts';
import { getPetTypes } from '../api/getPetTypes';
import { getPetTypeName } from '../utils/petTypeMapping';
import './CustomerDetails.css';
import { deleteCustomer } from '../api/deleteCustomer.ts';
import { IsVet } from '@/context/UserContext';
import EditPetModal from './EditPetModal';
import AddPetModal from './AddPetModal';
import defaultProfile from '@/assets/Customers/defaultProfilePicture.png';

const CustomerDetails: FC = () => {
  const { customerId } = useParams<{ customerId: string }>();
  const navigate = useNavigate();
  const isVet = IsVet();

  const [isDisabled, setIsDisabled] = useState<boolean>(false);
  const [customer, setCustomer] = useState<CustomerResponseModel | null>(null);
  const [userDetails, setUserDetails] = useState<UserDetailsModel | null>(null);
  const [pets, setPets] = useState<PetResponseModel[]>([]);
  const [petImageUrls, setPetImageUrls] = useState<Record<string, string>>({});
  const [petTypes, setPetTypes] = useState<PetTypeModel[]>([]);
  const [bills, setBills] = useState<Bill[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [isAddPetModalOpen, setIsAddPetModalOpen] = useState<boolean>(false);
  const [isEditPetModalOpen, setIsEditPetModalOpen] = useState<boolean>(false);
  const [selectedPetId, setSelectedPetId] = useState<string>('');

  useEffect(() => {
    const fetchCustomerDetails = async (): Promise<void> => {
      //customerId can't be undefied here so it is ok to assert it.
      const customerResponse = await getCustomer(customerId!);
      setCustomer(customerResponse.data);

      try {
        const userResponse = await axiosInstance.get(`/users/${customerId}`, {
          useV2: false,
        });
        setUserDetails(userResponse.data);
        setIsDisabled(userResponse.data.disabled);
      } catch (error) {
        setUserDetails(null);
        setIsDisabled(false);
      }

      // Fetch pets by customer ID
      const petsResponse = await axiosInstance.get(
        `/pets/customers/${customerId}/pets`,
        {
          useV2: false,
        }
      );

      let petsData: PetResponseModel[] = [];
      if (typeof petsResponse.data === 'string') {
        const pieces = petsResponse.data.split('\n').filter(Boolean);
        for (const piece of pieces) {
          if (piece.startsWith('data:')) {
            const petData = piece.slice(5).trim();
            try {
              const pet: PetResponseModel = JSON.parse(petData);
              petsData.push(pet);
            } catch (parseError) {
              console.error('Error parsing pet data:', parseError);
            }
          }
        }
      } else if (Array.isArray(petsResponse.data)) {
        petsData = petsResponse.data;
      }

      const newPetImageUrls: Record<string, string> = {};
      for (const pet of petsData) {
        newPetImageUrls[pet.petId] = await fetchPetPhotoUrl(
          pet.petId,
          pet.name
        );
      }

      setPetImageUrls(newPetImageUrls);
      setPets(petsData);

      const billsResponse = await axiosInstance.get(
        `/bills/customer/${customerId}`,
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

    if (customerId) {
      fetchCustomerDetails();
    }
  }, [customerId]);

  const handleEditClick = (): void => {
    navigate(`/customers/${customerId}/edit`);
  };

  const handleBackClick = (): void => {
    navigate('/customers');
  };

  const fetchPetPhotoUrl = async (
    petId: string,
    petName: string
  ): Promise<string> => {
    try {
      const response = await axiosInstance.get(`/pets/${petId}`, {
        useV2: false,
        params: { includePhoto: true },
      });
      const petData = response.data;

      if (
        petData.photo &&
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        (petData.photo.data || (petData.photo as any).fileData)
      ) {
        const base64Data =
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          petData.photo.data || (petData.photo as any).fileData;
        const contentType =
          petData.photo.contentType ||
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          (petData.photo as any).fileType ||
          'image/png';
        const byteCharacters = atob(base64Data);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i);
        }
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: contentType });
        return URL.createObjectURL(blob);
      } else {
        return defaultProfile;
      }
    } catch (error) {
      console.error(`Error fetching photo for ${petName} (${petId}):`, error);
      return defaultProfile;
    }
  };

  const handleDelete = async (customerId: string): Promise<void> => {
    const confirmDelete = window.confirm(
      'Are you sure you want to delete this customer?'
    );

    if (confirmDelete) {
      await deleteCustomer(customerId);
      alert('Customer deleted successfully.');
      navigate('/customers');
    } else {
      alert('Customer deletion canceled.');
    }
  };

  if (loading) {
    return <p>Loading...</p>;
  }

  if (!customer) {
    return <p>No customer found.</p>;
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
        await axiosInstance.patch(`/users/${customerId}/enable`, {
          useV2: true,
        });
        alert('User account enabled successfully.');
      } else {
        await axiosInstance.patch(`/users/${customerId}/disable`, {
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
  const fetchCustomerDetail = async (): Promise<void> => {
    if (!customerId) return;

    try {
      const customerResponse = await getCustomer(customerId);
      setCustomer(customerResponse.data);

      try {
        const userResponse = await axiosInstance.get(`/users/${customerId}`, {
          useV2: false,
        });
        setIsDisabled(userResponse.data.disabled);
      } catch (error) {
        setIsDisabled(false);
      }

      const petsResponse = await axiosInstance.get(
        `/pets/customers/${customerId}/pets`,
        {
          useV2: false,
        }
      );

      let petsData: PetResponseModel[] = [];
      if (typeof petsResponse.data === 'string') {
        const pieces = petsResponse.data.split('\n').filter(Boolean);
        for (const piece of pieces) {
          if (piece.startsWith('data:')) {
            const petData = piece.slice(5).trim();
            try {
              const pet: PetResponseModel = JSON.parse(petData);
              petsData.push(pet);
            } catch (parseError) {
              console.error('Error parsing pet data:', parseError);
            }
          }
        }
      } else if (Array.isArray(petsResponse.data)) {
        petsData = petsResponse.data;
      }

      const newPetImageUrls: Record<string, string> = {};
      for (const pet of petsData) {
        newPetImageUrls[pet.petId] = await fetchPetPhotoUrl(
          pet.petId,
          pet.name
        );
      }

      setPetImageUrls(newPetImageUrls);
      setPets(petsData);

      const billsResponse = await axiosInstance.get(
        `/bills/customer/${customerId}`,
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
      console.error('Error fetching customer details:', error);
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
    fetchCustomerDetail();
  };

  const handlePetDeleted = (): void => {
    fetchCustomerDetail();
  };

  return (
    <div className="customer-details-card">
      <h2>
        {' '}
        Customer Details for {customer.firstName} {customer.lastName}{' '}
      </h2>

      <div className="customer-details-container">
        {/* Customer Info */}
        <div className="section customer-info">
          <h3>Customer Info</h3>
          <p>
            <strong>Username: </strong>
            {userDetails?.username || 'Loading...'}
          </p>
          <p>
            <strong>First Name: </strong>
            {customer.firstName}
          </p>
          <p>
            <strong>Last Name: </strong>
            {customer.lastName}
          </p>
          <p>
            <strong>Address: </strong>
            {customer.address}
          </p>
          <p>
            <strong>City: </strong>
            {customer.city}
          </p>
          <p>
            <strong>Province: </strong>
            {customer.province}
          </p>
          <p>
            <strong>Telephone: </strong>
            {customer.telephone}
          </p>
        </div>

        {/* Customer Pets */}
        <div className="section customer-pets">
          <h3>Customer Pets</h3>
          {pets && pets.length > 0 ? (
            <ul>
              {pets.map(pet => (
                <li key={pet.petId} className="pet-item">
                  <img
                    src={petImageUrls[pet.petId] || defaultProfile}
                    alt={`${pet.name} profile`}
                    className="pet-profile-picture"
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

        {/* Customer Bills */}
        <div className="section customer-bills">
          <h3>Customer Bills</h3>
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
          Back to All Customers
        </button>
        <button className="add-pet-button" onClick={handleAddPet}>
          Add New Pet
        </button>
        {!isVet && (
          <button
            className="btn btn-danger"
            onClick={() => handleDelete(customer.customerId)}
            title="Delete"
            style={{ backgroundColor: 'red', color: 'white' }}
          >
            Delete Customer
          </button>
        )}
        {userDetails && (
          <button
            className={`btn ${isDisabled ? 'btn-success' : 'btn-warning'}`}
            onClick={handleDisableEnable}
          >
            {isDisabled ? 'Enable Account' : 'Disable Account'}
          </button>
        )}
      </div>

      <AddPetModal
        customerId={customerId || ''}
        isOpen={isAddPetModalOpen}
        onClose={handleCloseAddPetModal}
        onPetAdded={handlePetAdded}
      />

      <EditPetModal
        isOpen={isEditPetModalOpen}
        onClose={handleCloseEditPetModal}
        petId={selectedPetId}
        customerId={customerId || ''}
        onPetUpdated={handlePetUpdated}
        onPetDeleted={handlePetDeleted}
      />
    </div>
  );
};

export default CustomerDetails;
