import { FC, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
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
import UpdatePetForm from './UpdatePetForm';
import AddPetModal from './AddPetModal';
import defaultProfile from '@/assets/Customers/defaultProfilePicture.png';

const CustomerDetails: FC = () => {
  const { t } = useTranslation('customers');
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
  const [selectedPetId, setSelectedPetId] = useState<string>('');

  useEffect(() => {
    const fetchCustomerDetails = async (): Promise<void> => {
      //customerId can't be undefied here so it is ok to assert it.
      const customerResponse = await getCustomer(customerId!, true);
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
      t('customerDetails.messages.confirmDelete')
    );

    if (confirmDelete) {
      await deleteCustomer(customerId);
      alert(t('customerDetails.messages.deleteSuccess'));
      navigate('/customers');
    } else {
      alert(t('customerDetails.messages.deleteCanceled'));
    }
  };

  if (loading) {
    return <p>{t('customerDetails.loading')}</p>;
  }

  if (!customer) {
    return <p>{t('customerDetails.notFound')}</p>;
  }

  const calculateAge = (birthDate: Date): number => {
    const birth = new Date(birthDate);
    const ageDiffMs = Date.now() - birth.getTime();
    const ageDate = new Date(ageDiffMs);
    return Math.abs(ageDate.getUTCFullYear() - 1970);
  };

  const handleDisableEnable = async (): Promise<void> => {
    const confirmAction = window.confirm(
      isDisabled
        ? t('customerDetails.messages.confirmEnable')
        : t('customerDetails.messages.confirmDisable')
    );

    if (confirmAction) {
      if (isDisabled) {
        await axiosInstance.patch(`/users/${customerId}/enable`, {
          useV2: true,
        });
        alert(t('customerDetails.messages.enableSuccess'));
      } else {
        await axiosInstance.patch(`/users/${customerId}/disable`, {
          useV2: true,
        });
        alert(t('customerDetails.messages.disableSuccess'));
      }
      setIsDisabled(!isDisabled);
    }
  };

  const handleEditPetClick = (petId: string): void => {
    setSelectedPetId(petId);
  };

  const handleCloseEditPetForm = (): void => {
    setSelectedPetId('');
  };

  //eliminated code duplication
  const fetchCustomerDetail = async (): Promise<void> => {
    if (!customerId) return;

    try {
      const customerResponse = await getCustomer(customerId, true);
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
    setSelectedPetId('');
    fetchCustomerDetail();
  };

  const handlePetDeleted = (): void => {
    fetchCustomerDetail();
  };

  return (
    <div className="customer-details-card">
      <h2>
        {t('customerDetails.heading', {
          firstName: customer.firstName,
          lastName: customer.lastName,
        })}
      </h2>

      <div className="customer-details-container">
        {/* Customer Info */}
        <div className="section customer-info">
          <h3>{t('customerDetails.infoTitle')}</h3>
          <img
            src={
              customer.photo?.fileData
                ? `data:${customer.photo.fileType};base64,${customer.photo.fileData}`
                : defaultProfile
            }
            alt={t('customerDetails.pets.photoAlt', {
              name: `${customer.firstName} ${customer.lastName}`,
            })}
            className="customer-profile-picture"
          />
          <p>
            <strong>{t('fields.username')} </strong>
            {userDetails?.username || t('customerDetails.loading')}
          </p>
          <p>
            <strong>{t('fields.firstName')} </strong>
            {customer.firstName}
          </p>
          <p>
            <strong>{t('fields.lastName')} </strong>
            {customer.lastName}
          </p>
          <p>
            <strong>{t('fields.address')} </strong>
            {customer.address}
          </p>
          <p>
            <strong>{t('fields.city')} </strong>
            {customer.city}
          </p>
          <p>
            <strong>{t('fields.province')} </strong>
            {customer.province}
          </p>
          <p>
            <strong>{t('fields.telephone')} </strong>
            {customer.telephone}
          </p>
        </div>

        {/* Customer Pets */}
        <div className="section customer-pets">
          <h3>{t('customerDetails.pets.title')}</h3>
          {pets && pets.length > 0 ? (
            <ul>
              {pets.map(pet => (
                <li key={pet.petId} className="pet-item">
                  <img
                    src={petImageUrls[pet.petId] || defaultProfile}
                    alt={t('customerDetails.pets.photoAlt', { name: pet.name })}
                    className="pet-profile-picture"
                  />
                  <div className="pet-details">
                    <div className="pet-info">
                      <span className="pet-id">
                        {t('customerDetails.pets.petId')} {pet.petId}
                      </span>
                    </div>
                    <div className="pet-main-info">
                      <span className="pet-name">
                        <strong>{t('customerDetails.pets.name')}</strong>{' '}
                        {pet.name}
                      </span>
                      <span className="pet-type">
                        <strong>{t('customerDetails.pets.type')}</strong>{' '}
                        {getPetTypeName(pet.petTypeId, petTypes)}
                      </span>
                      <span className="pet-weight">
                        <strong>{t('customerDetails.pets.weight')}</strong>{' '}
                        {pet.weight}kg
                      </span>
                      <span className="pet-age">
                        <strong>{t('customerDetails.pets.age')}</strong>{' '}
                        {t('customerDetails.pets.years', {
                          count: calculateAge(pet.birthDate),
                        })}
                      </span>
                    </div>
                    <div className="pet-actions">
                      <button
                        className="edit-pet-button"
                        onClick={() => handleEditPetClick(pet.petId)}
                      >
                        {t('customerDetails.pets.editPet')}
                      </button>
                    </div>

                    {selectedPetId === pet.petId && (
                      <UpdatePetForm
                        pet={pet}
                        customerId={customerId || ''}
                        petTypes={petTypes}
                        onCancel={handleCloseEditPetForm}
                        onPetUpdated={handlePetUpdated}
                        onPetDeleted={handlePetDeleted}
                      />
                    )}
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <p>{t('customerDetails.pets.none')}</p>
          )}
        </div>

        {/* Customer Bills */}
        <div className="section customer-bills">
          <h3>{t('customerDetails.bills.title')}</h3>
          {Array.isArray(bills) && bills.length > 0 ? (
            <ul>
              {bills.map(bill => (
                <li key={bill.billId}>
                  <strong>{t('customerDetails.bills.billId')} </strong>
                  {bill.billId},{' '}
                  <strong>{t('customerDetails.bills.amount')} </strong>
                  {bill.amount},{' '}
                  <strong>{t('customerDetails.bills.date')} </strong>
                  {bill.date}
                </li>
              ))}
            </ul>
          ) : (
            <p>{t('customerDetails.bills.none')}</p>
          )}
        </div>
      </div>

      <div className="customer-details-buttons">
        <button className="customer-details-button" onClick={handleEditClick}>
          {t('customerDetails.buttons.edit')}
        </button>
        <button className="customer-details-button" onClick={handleBackClick}>
          {t('customerDetails.buttons.back')}
        </button>
        <button className="add-pet-button" onClick={handleAddPet}>
          {t('customerDetails.buttons.addPet')}
        </button>
        {!isVet && (
          <button
            className="btn btn-danger"
            onClick={() => handleDelete(customer.customerId)}
            title={t('customerDetails.buttons.deleteTitle')}
            style={{ backgroundColor: 'red', color: 'white' }}
          >
            {t('customerDetails.buttons.delete')}
          </button>
        )}
        {userDetails && (
          <button
            className={`btn ${isDisabled ? 'btn-success' : 'btn-warning'}`}
            onClick={handleDisableEnable}
          >
            {isDisabled
              ? t('customerDetails.buttons.enable')
              : t('customerDetails.buttons.disable')}
          </button>
        )}
      </div>

      <AddPetModal
        customerId={customerId || ''}
        isOpen={isAddPetModalOpen}
        onClose={handleCloseAddPetModal}
        onPetAdded={handlePetAdded}
      />
    </div>
  );
};

export default CustomerDetails;
