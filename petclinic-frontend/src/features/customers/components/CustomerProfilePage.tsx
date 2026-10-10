import { useState } from 'react';
import { CustomerResponseModel } from '@/features/customers/models/CustomerResponseModel';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel';
import { PetTypeModel } from '@/features/customers/models/PetTypeModel';
import { UserDetailsModel } from '@/features/customers/models/UserDetailsModel';
import { useUser } from '@/context/UserContext';
import AddPetModal from '@/features/customers/components/AddPetModal';
import UploadPhotoModal from '@/features/customers/components/UploadPhotoModal';
import UploadPetPhotoModal from '@/features/customers/components/UploadPetPhotoModal';
import { deletePet } from '@/features/customers/api/deletePet';
import { deleteCustomerPhoto } from '@/features/customers/api/deleteCustomerPhoto';
import { deletePetPhoto } from '@/features/customers/api/deletePetPhoto';
import {
  getPetTypeName,
  getPetTypeImage,
} from '@/features/customers/utils/petTypeMapping';

import { useConfirmModal } from '@/shared/hooks/useConfirmModal';
import defaultProfile from '@/assets/Customers/defaultProfilePicture.png';
// eslint-disable-next-line import/default
import PropTypes from 'prop-types';
import UpdatePetForm from './UpdatePetForm';

interface CustomerProfilePageProps {
  customer: CustomerResponseModel;
  userDetails: UserDetailsModel;
  petTypes: PetTypeModel[];
  profilePicUrl: string;
  petImageUrls: Record<string, string>;
  pets: PetResponseModel[];
  setCustomer: React.Dispatch<
    React.SetStateAction<CustomerResponseModel | null>
  >;
  setPetImageUrls: React.Dispatch<React.SetStateAction<Record<string, string>>>;
  setProfilePicUrl: React.Dispatch<React.SetStateAction<string>>;
  onUpdateProfileClick: () => void;
  onProfilePhotoUploaded: () => Promise<void>;
  onPetPhotoUploaded: (petId: string) => Promise<void>;
}

const CustomerProfilePage: React.FC<CustomerProfilePageProps> = ({
  customer,
  userDetails,
  petTypes,
  profilePicUrl,
  petImageUrls,
  pets,
  setCustomer,
  setPetImageUrls,
  setProfilePicUrl,
  onUpdateProfileClick,
  onProfilePhotoUploaded,
  onPetPhotoUploaded,
}): JSX.Element => {
  const { user } = useUser();

  const [error, setError] = useState<string | null>(null);
  const [isAddPetModalOpen, setIsAddPetModalOpen] = useState<boolean>(false);
  const [isUploadPhotoModalOpen, setIsUploadPhotoModalOpen] =
    useState<boolean>(false);
  const [isUploadPetPhotoModalOpen, setIsUploadPetPhotoModalOpen] =
    useState<boolean>(false);
  const [selectedPetIdForPhoto, setSelectedPetIdForPhoto] =
    useState<string>('');
  const [selectedPetId, setSelectedPetId] = useState<string>('');
  const [isDeletePhotoModalOpen, setIsDeletePhotoModalOpen] =
    useState<boolean>(false);

  const { confirm, ConfirmModal } = useConfirmModal();

  const calculateAge = (birthDate: Date): number => {
    const birth = new Date(birthDate);
    const ageDiffMs = Date.now() - birth.getTime();
    const ageDate = new Date(ageDiffMs);
    return Math.abs(ageDate.getUTCFullYear() - 1970);
  };

  const handleAddPet = (): void => {
    setIsAddPetModalOpen(true);
  };

  const handleCloseAddPetModal = (): void => {
    setIsAddPetModalOpen(false);
  };

  const handlePetAdded = (newPet: PetResponseModel): void => {
    setCustomer(prevCustomer => {
      if (!prevCustomer) return prevCustomer;
      return {
        ...prevCustomer,
        pets: [...(prevCustomer.pets || []), newPet],
      };
    });
  };

  const handleEditPet = (petId: string): void => {
    setSelectedPetId(petId);
  };

  const handleCloseEditPet = (): void => {
    setSelectedPetId('');
  };

  const handlePetUpdated = (updatedPet?: PetResponseModel): void => {
    if (!updatedPet) return;

    setCustomer(prevCustomer => {
      if (!prevCustomer?.pets) return prevCustomer;
      return {
        ...prevCustomer,
        pets: prevCustomer.pets.map(pet =>
          pet.petId === updatedPet.petId
            ? {
                ...updatedPet,
                birthDate: updatedPet.birthDate
                  ? new Date(updatedPet.birthDate)
                  : new Date(),
              }
            : pet
        ),
      };
    });
  };

  const handlePetDeleted = (): void => {
    setCustomer(prevCustomer => {
      if (!prevCustomer || !prevCustomer.pets) return prevCustomer;

      return {
        ...prevCustomer,
        pets: prevCustomer.pets.filter(pet => pet.petId !== selectedPetId),
      };
    });

    setSelectedPetId('');
  };

  const handleDeletePet = async (petId: string): Promise<void> => {
    const confirmed = window.confirm(
      'Are you sure you want to delete this pet? This action cannot be undone.'
    );

    if (!confirmed) return;

    try {
      await deletePet(petId);
      if (customer) {
        setCustomer({
          ...customer,
          pets: customer.pets?.filter(pet => pet.petId !== petId) || [],
        });
      }
    } catch (error) {
      console.error('Error deleting pet:', error);
      alert('Failed to delete pet. Please try again.');
    }
  };

  const handleOpenUploadPhotoModal = (): void => {
    setIsUploadPhotoModalOpen(true);
  };

  const handleCloseUploadPhotoModal = (): void => {
    setIsUploadPhotoModalOpen(false);
  };

  const handlePhotoUploaded = async (): Promise<void> => {
    await onProfilePhotoUploaded();
  };

  const handleDeletePhoto = async (): Promise<void> => {
    try {
      await deleteCustomerPhoto(user.userId);
      setProfilePicUrl('');
      setIsDeletePhotoModalOpen(false);
    } catch (error) {
      console.error('Error deleting profile photo:', error);
      alert('Failed to delete profile photo. Please try again.');
    }
  };

  const handleOpenUploadPetPhotoModal = (petId: string): void => {
    setSelectedPetIdForPhoto(petId);
    setIsUploadPetPhotoModalOpen(true);
  };

  const handleCloseUploadPetPhotoModal = (): void => {
    setIsUploadPetPhotoModalOpen(false);
    setSelectedPetIdForPhoto('');
  };

  const handlePetPhotoUploaded = async (petId: string): Promise<void> => {
    await onPetPhotoUploaded(petId);
  };

  const handleDeletePetPhoto = async (petId: string): Promise<void> => {
    if (!user.userId) return;

    const pet = customer?.pets?.find(p => p.petId === petId);
    if (!pet) return;

    const confirmed = await confirm({
      title: 'Delete Pet Photo',
      message:
        "Are you sure you want to delete this pet's photo? This action cannot be undone.",
      confirmText: 'Delete',
      cancelText: 'Cancel',
      variant: 'danger',
      destructive: true,
    });

    if (!confirmed) return;

    try {
      await deletePetPhoto(petId);
      setPetImageUrls(prev => ({
        ...prev,
        [petId]: getPetTypeImage(pet.petTypeId, petTypes),
      }));

      if (customer) {
        const updatedPets = customer.pets.map(p =>
          p.petId === petId ? { ...p, photo: undefined } : p
        );
        setCustomer({ ...customer, pets: updatedPets });
      }
    } catch (error) {
      console.error('Error deleting pet photo:', error);
      setError('Failed to delete pet photo. Please try again.');
    }
  };

  if (error) {
    return <p>{error}</p>;
  }

  return (
    <div>
      <div className="customers-page customers-container-profile">
        <div className="customers-profile-card shadow-lg p-5 mb-5 bg-white rounded">
          <div
            className="customers-profile-header"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '1rem',
            }}
          >
            <div
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '8px',
              }}
            >
              <img
                src={profilePicUrl || defaultProfile}
                alt="Profile Picture"
                className="profile-picture"
              />

              <div className="photo-buttons-container">
                <button
                  onClick={handleOpenUploadPhotoModal}
                  className="pet-photo-change"
                >
                  Upload Photo
                </button>

                {profilePicUrl && (
                  <button
                    onClick={() => setIsDeletePhotoModalOpen(true)}
                    className="photo-button delete"
                  >
                    Delete Photo
                  </button>
                )}
              </div>
            </div>

            <h1>
              {customer.firstName} {customer.lastName}&apos;s Profile
            </h1>
          </div>

          <div className="customers-profile-info">
            <p>
              <strong>Username:</strong> {userDetails.username || 'Loading...'}
            </p>
            <p>
              <strong>First Name:</strong> {customer.firstName}
            </p>
            <p>
              <strong>Last Name:</strong> {customer.lastName}
            </p>
            <p>
              <strong>Address:</strong> {customer.address}
            </p>
            <p>
              <strong>City:</strong> {customer.city}
            </p>
            <p>
              <strong>Province:</strong> {customer.province}
            </p>
            <p>
              <strong>Telephone:</strong> {customer.telephone}
            </p>
          </div>

          <div className="customers-pets-section">
            <div className="customers-pets-header">
              <h3>Customer Pets</h3>
              <button
                className="customers-add-pet-button"
                onClick={handleAddPet}
              >
                Add Pet
              </button>
            </div>

            {pets && pets.length > 0 ? (
              <div className="customers-pets-list">
                {pets.map((pet: PetResponseModel) =>
                  selectedPetId === pet.petId ? (
                    <UpdatePetForm
                      key={pet.petId}
                      pet={pet}
                      customerId={user.userId}
                      petTypes={petTypes}
                      onCancel={handleCloseEditPet}
                      onPetUpdated={handlePetUpdated}
                      onPetDeleted={handlePetDeleted}
                    />
                  ) : (
                    <div key={pet.petId} className="customers-pet-card">
                      <div className="customers-pet-card-content">
                        <img
                          src={
                            petImageUrls[pet.petId] ||
                            getPetTypeImage(pet.petTypeId, petTypes)
                          }
                          alt={`${pet.name} profile`}
                          className="pet-profile-picture"
                        />

                        <div className="customers-pet-info">
                          <h4 className="customers-pet-name">{pet.name}</h4>
                          <div className="customers-pet-details">
                            <span className="customers-pet-detail">
                              <strong>Type:</strong>{' '}
                              {getPetTypeName(pet.petTypeId, petTypes)}
                            </span>
                            <span className="customers-pet-detail">
                              <strong>Weight:</strong> {pet.weight}kg
                            </span>
                            <span className="customers-pet-detail">
                              <strong>Age:</strong>{' '}
                              {calculateAge(pet.birthDate)} years
                            </span>
                          </div>
                        </div>
                      </div>

                      <div className="customers-pet-actions">
                        <button
                          className="customers-edit-pet-button"
                          onClick={() => handleEditPet(pet.petId)}
                        >
                          Edit Pet
                        </button>

                        {(!petImageUrls[pet.petId] ||
                          petImageUrls[pet.petId] ===
                            getPetTypeImage(pet.petTypeId, petTypes)) && (
                          <button
                            className="pet-photo-change"
                            onClick={() =>
                              handleOpenUploadPetPhotoModal(pet.petId)
                            }
                          >
                            Add Photo
                          </button>
                        )}

                        {petImageUrls[pet.petId] &&
                          petImageUrls[pet.petId] !==
                            getPetTypeImage(pet.petTypeId, petTypes) && (
                            <button
                              className="customers-delete-photo-button"
                              onClick={() => handleDeletePetPhoto(pet.petId)}
                            >
                              Delete Photo
                            </button>
                          )}

                        <button
                          className="customers-delete-pet-button"
                          onClick={() => handleDeletePet(pet.petId)}
                        >
                          Delete Pet
                        </button>
                      </div>
                    </div>
                  )
                )}
              </div>
            ) : (
              <div className="customers-no-pets">
                <p>No pets found.</p>
                <p className="customers-no-pets-subtitle">Add your first pet</p>
              </div>
            )}
          </div>

          <button
            className="customers-updateButton"
            onClick={onUpdateProfileClick}
          >
            Update Profile
          </button>
        </div>
      </div>

      <AddPetModal
        customerId={user.userId}
        isOpen={isAddPetModalOpen}
        onClose={handleCloseAddPetModal}
        onPetAdded={handlePetAdded}
      />

      <UploadPhotoModal
        isOpen={isUploadPhotoModalOpen}
        onClose={handleCloseUploadPhotoModal}
        customerId={user.userId}
        onPhotoUploaded={handlePhotoUploaded}
      />

      <UploadPetPhotoModal
        isOpen={isUploadPetPhotoModalOpen}
        onClose={handleCloseUploadPetPhotoModal}
        petId={selectedPetIdForPhoto}
        onPhotoUploaded={handlePetPhotoUploaded}
      />

      {isDeletePhotoModalOpen && (
        <div
          className="modal-overlay"
          onClick={() => setIsDeletePhotoModalOpen(false)}
        >
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <h2>Delete Profile Photo</h2>
              <button
                className="close-button"
                onClick={() => setIsDeletePhotoModalOpen(false)}
              >
                ×
              </button>
            </div>
            <div className="modal-body">
              <p>Are you sure you want to delete your profile photo?</p>
              <div className="modal-footer">
                <button
                  onClick={() => setIsDeletePhotoModalOpen(false)}
                  className="cancel-button"
                >
                  Cancel
                </button>
                <button onClick={handleDeletePhoto} className="delete-button">
                  Delete Photo
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      <ConfirmModal />
    </div>
  );
};
CustomerProfilePage.propTypes = {
  customer: PropTypes.object
    .isRequired as PropTypes.Validator<CustomerResponseModel>,
  userDetails: PropTypes.object
    .isRequired as PropTypes.Validator<UserDetailsModel>,
  petTypes: PropTypes.array.isRequired,
  profilePicUrl: PropTypes.string.isRequired,
  petImageUrls: PropTypes.object.isRequired as PropTypes.Validator<
    Record<string, string>
  >,
  pets: PropTypes.array.isRequired,
  setCustomer: PropTypes.func.isRequired,
  setPetImageUrls: PropTypes.func.isRequired,
  setProfilePicUrl: PropTypes.func.isRequired,
  onUpdateProfileClick: PropTypes.func.isRequired,
  onProfilePhotoUploaded: PropTypes.func.isRequired,
  onPetPhotoUploaded: PropTypes.func.isRequired,
};
export default CustomerProfilePage;
