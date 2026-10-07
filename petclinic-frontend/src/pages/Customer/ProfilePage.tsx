import { useEffect, useState } from 'react';
import { getCustomer } from '@/features/customers/api/getCustomer.ts';
import { getPetTypes } from '@/features/customers/api/getPetTypes';
import { getUserDetails } from '@/features/customers/api/getUserDetails';
import { CustomerResponseModel } from '@/features/customers/models/CustomerResponseModel.ts';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel.ts';
import { PetTypeModel } from '@/features/customers/models/PetTypeModel';
import { UserDetailsModel } from '@/features/customers/models/UserDetailsModel';
import { useUser } from '@/context/UserContext';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import AddPetModal from '@/features/customers/components/AddPetModal';
import EditPetModal from '@/features/customers/components/EditPetModal';
import UploadPhotoModal from '@/features/customers/components/UploadPhotoModal';
import UploadPetPhotoModal from '@/features/customers/components/UploadPetPhotoModal';
import './ProfilePage.css';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '@/shared/api/axiosInstance';
import {
  getPetTypeName,
  getPetTypeImage, // ✅ added
} from '@/features/customers/utils/petTypeMapping';
import { deletePet } from '@/features/customers/api/deletePet';
import defaultProfile from '@/assets/Customers/defaultProfilePicture.png';
import { deleteCustomerPhoto } from '@/features/customers/api/deleteCustomerPhoto.ts';
import { deletePetPhoto } from '@/features/customers/api/deletePetPhoto';
import { useConfirmModal } from '@/shared/hooks/useConfirmModal';

const ProfilePage = (): JSX.Element => {
  const [profilePicUrl, setProfilePicUrl] = useState<string>('');
  const { user } = useUser();
  const [petImageUrls, setPetImageUrls] = useState<Record<string, string>>({});
  const [customer, setCustomer] = useState<CustomerResponseModel | null>(null);
  const [userDetails, setUserDetails] = useState<UserDetailsModel | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [isAddPetModalOpen, setIsAddPetModalOpen] = useState<boolean>(false);
  const [isEditPetModalOpen, setIsEditPetModalOpen] = useState<boolean>(false);
  const [isUploadPhotoModalOpen, setIsUploadPhotoModalOpen] =
    useState<boolean>(false);

  const [isUploadPetPhotoModalOpen, setIsUploadPetPhotoModalOpen] =
    useState<boolean>(false);
  const [selectedPetIdForPhoto, setSelectedPetIdForPhoto] =
    useState<string>('');

  const [selectedPetId, setSelectedPetId] = useState<string>('');
  const [petTypes, setPetTypes] = useState<PetTypeModel[]>([]);
  const navigate = useNavigate();

  const [isDeletePhotoModalOpen, setIsDeletePhotoModalOpen] =
    useState<boolean>(false);

  const { confirm, ConfirmModal } = useConfirmModal();
  useEffect(() => {
    const fetchPetTypes = async (): Promise<void> => {
      try {
        const petTypesData = await getPetTypes();
        setPetTypes(petTypesData);
      } catch (error) {
        console.error('Error fetching pet types:', error);
        setPetTypes([]);
      }
    };

    fetchPetTypes();
  }, []);

  useEffect(() => {
    let isMounted = true;

    const fetchUserData = async (): Promise<void> => {
      if (!user.userId) return;
      try {
        const userDetailsResponse = await getUserDetails(user.userId);
        if (isMounted) {
          setUserDetails(userDetailsResponse.data);
        }
      } catch (error) {
        console.error('Error fetching user details:', error);
        if (isMounted) {
          setUserDetails({
            userId: user.userId,
            username: 'Unknown',
            email: '',
            roles: [],
            verified: false,
            disabled: false,
          });
        }
      }
    };

    fetchUserData();

    return () => {
      isMounted = false;
    };
  }, [user.userId]);

  useEffect(() => {
    let isMounted = true;
    let objectUrl: string | null = null;

    const fetchProfilePic = async (): Promise<void> => {
      if (!user.userId) return;

      try {
        const customerResponse = await getCustomer(user.userId, true);
        const customerData = customerResponse.data;

        if (customerData.photo && customerData.photo.fileData) {
          const base64Data = customerData.photo.fileData;
          const contentType = customerData.photo.fileType || 'image/png';
          const byteCharacters = atob(base64Data);
          const byteNumbers = new Array(byteCharacters.length);
          for (let i = 0; i < byteCharacters.length; i++) {
            byteNumbers[i] = byteCharacters.charCodeAt(i);
          }
          const byteArray = new Uint8Array(byteNumbers);
          const blob = new Blob([byteArray], { type: contentType });
          objectUrl = URL.createObjectURL(blob);
          if (isMounted) {
            setProfilePicUrl(objectUrl);
          }
        } else {
          if (isMounted) {
            setProfilePicUrl('');
          }
        }
      } catch (err) {
        console.warn(
          'Failed to fetch customer profile picture, using local default',
          err
        );
        if (isMounted) {
          setProfilePicUrl('');
        }
      }
    };

    fetchProfilePic();

    return () => {
      isMounted = false;
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
      }
    };
  }, [user.userId]);

  const fetchPetPhotoUrl = async (
    petId: string,
    petName: string,
    petTypeId: string // ✅ added param
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
        // ✅ type-based fallback
        return getPetTypeImage(petTypeId, petTypes);
      }
    } catch (error) {
      console.error(`Error fetching photo for ${petName} (${petId}):`, error);
      return getPetTypeImage(petTypeId, petTypes);
    }
  };
  useEffect(() => {
    let isMounted = true;

    const fetchCustomerData = async (): Promise<void> => {
      if (!user.userId) return;
      try {
        const customerResponse = await getCustomer(user.userId);
        const customerData = customerResponse.data;

        try {
          const petsResponse = await axiosInstance.get(
            `/pets/customers/${user.userId}/pets`,
            { useV2: false }
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
              pet.name,
              pet.petTypeId // ✅ now uses the added param
            );
          }

          if (isMounted) {
            setPetImageUrls(newPetImageUrls);
            setCustomer({
              ...customerData,
              pets: petsData,
            });
            setError(null);
          }
        } catch (petsError) {
          console.warn(
            'Error fetching pets, setting customer without pets:',
            petsError
          );
          if (isMounted) {
            setCustomer({
              ...customerData,
              pets: [],
            });
            setError(null);
          }
        }
      } catch (error) {
        if (isMounted) {
          setError('Error fetching customer data');
        }
        console.error('Error fetching customer data:', error);
      }
    };

    fetchCustomerData();

    return () => {
      isMounted = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user.userId, petTypes]);

  const handleUpdateClick = (): void => {
    navigate(AppRoutePaths.CustomerProfileEdit);
  };

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
    if (customer) {
      setCustomer({
        ...customer,
        pets: [...(customer.pets || []), newPet],
      });
    }
  };

  const handleOpenUploadPhotoModal = (): void => {
    setIsUploadPhotoModalOpen(true);
  };

  const handleCloseUploadPhotoModal = (): void => {
    setIsUploadPhotoModalOpen(false);
  };

  const handlePhotoUploaded = async (): Promise<void> => {
    if (!user.userId) return;

    try {
      const customerResponse = await getCustomer(user.userId, true);
      const customerData = customerResponse.data;

      if (customerData.photo && customerData.photo.fileData) {
        const base64Data = customerData.photo.fileData;
        const contentType = customerData.photo.fileType || 'image/png';
        const byteCharacters = atob(base64Data);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i);
        }
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: contentType });
        const objectUrl = URL.createObjectURL(blob);

        if (profilePicUrl) URL.revokeObjectURL(profilePicUrl);
        setProfilePicUrl(objectUrl);
      } else {
        if (profilePicUrl) URL.revokeObjectURL(profilePicUrl);
        setProfilePicUrl('');
      }
    } catch (error) {
      console.error('Error refreshing profile picture:', error);
    }
  };

  const handleDeletePhoto = async (): Promise<void> => {
    try {
      await deleteCustomerPhoto(user.userId);
      if (profilePicUrl) URL.revokeObjectURL(profilePicUrl);
      setProfilePicUrl('');
      setIsDeletePhotoModalOpen(false);
    } catch (error) {
      console.error('Error deleting profile photo:', error);
      alert('Failed to delete profile photo. Please try again.');
    }
  };

  const handleEditPet = (petId: string): void => {
    setSelectedPetId(petId);
    setIsEditPetModalOpen(true);
  };

  const handleCloseEditPetModal = (): void => {
    setIsEditPetModalOpen(false);
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
      // eslint-disable-next-line no-console
      console.log('Pet deleted successfully');
    } catch (error) {
      console.error('Error deleting pet:', error);
      alert('Failed to delete pet. Please try again.');
    }
  };

  const fetchCustomerData = async (): Promise<void> => {
    if (!user.userId) return;
    try {
      const customerResponse = await getCustomer(user.userId);
      const customerData = customerResponse.data;
      if (customerData.pets && customerData.pets.length > 0) {
        setCustomer(customerData);
      } else {
        setCustomer({ ...customerData, pets: [] });
      }
    } catch (error) {
      setError('Error fetching customer data');
      console.error('Error fetching customer data:', error);
    }
  };

  const handlePetUpdated = (updatedPet?: PetResponseModel): void => {
    if (updatedPet) {
      setCustomer(prevCustomer => {
        if (!prevCustomer || !prevCustomer.pets) return prevCustomer;

        const updatedPets = prevCustomer.pets.map(pet =>
          pet.petId === updatedPet.petId
            ? {
                ...updatedPet,
                birthDate: updatedPet.birthDate
                  ? new Date(updatedPet.birthDate)
                  : new Date(),
              }
            : pet
        );

        return { ...prevCustomer, pets: updatedPets };
      });
    } else {
      fetchCustomerData();
    }
  };

  const handlePetDeleted = (): void => {
    fetchCustomerData();
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

      // ✅ reset to type-based image
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

  const handleOpenUploadPetPhotoModal = (petId: string): void => {
    setSelectedPetIdForPhoto(petId);
    setIsUploadPetPhotoModalOpen(true);
  };

  const handleCloseUploadPetPhotoModal = (): void => {
    setIsUploadPetPhotoModalOpen(false);
    setSelectedPetIdForPhoto('');
  };

  const handlePetPhotoUploaded = async (petId: string): Promise<void> => {
    try {
      const newUrl = await fetchPetPhotoUrl(petId, 'Pet', '');
      setPetImageUrls(prev => ({ ...prev, [petId]: newUrl }));
    } catch (e) {
      console.error('Failed to refresh pet photo after upload', e);
    }
  };

  if (error) return <p>{error}</p>;
  if (!customer) return <p>Loading...</p>;

  return (
    <div>
      <NavBar />
      <div className="customers-page customers-container-profile">
        <div className="customers-profile-card shadow-lg p-5 mb-5 bg-white rounded">
          <div
            className="customers-profile-header"
            style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}
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
              <strong>Username:</strong> {userDetails?.username || 'Loading...'}
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

            {customer.pets && customer.pets.length > 0 ? (
              <div className="customers-pets-list">
                {customer.pets.map((pet: PetResponseModel) => (
                  <div key={pet.petId} className="customers-pet-card">
                    <div className="customers-pet-card-content">
                      <img
                        src={
                          petImageUrls[pet.petId] ||
                          getPetTypeImage(pet.petTypeId, petTypes) // ✅ fallback
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
                            <strong>Age:</strong> {calculateAge(pet.birthDate)}{' '}
                            years
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
                ))}
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
            onClick={handleUpdateClick}
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

      <EditPetModal
        isOpen={isEditPetModalOpen}
        onClose={handleCloseEditPetModal}
        petId={selectedPetId}
        customerId={user.userId}
        onPetUpdated={handlePetUpdated}
        onPetDeleted={handlePetDeleted}
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

export default ProfilePage;
