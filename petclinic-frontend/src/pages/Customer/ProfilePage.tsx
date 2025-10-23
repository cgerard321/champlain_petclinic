import { useEffect, useState } from 'react';
import { getOwner } from '@/features/customers/api/getOwner';
import { getPetTypes } from '@/features/customers/api/getPetTypes';
import { getUserDetails } from '@/features/customers/api/getUserDetails';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel.ts';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel.ts';
import { PetTypeModel } from '@/features/customers/models/PetTypeModel';
import { UserDetailsModel } from '@/features/customers/models/UserDetailsModel';
import { useUser } from '@/context/UserContext';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import AddPetModal from '@/features/customers/components/AddPetModal';
import EditPetModal from '@/features/customers/components/EditPetModal';
import UploadPhotoModal from '@/features/customers/components/UploadPhotoModal';
import './ProfilePage.css';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '@/shared/api/axiosInstance';
import { getPetTypeName } from '@/features/customers/utils/petTypeMapping';
import { deletePet } from '@/features/customers/api/deletePet';
import defaultProfile from '@/assets/Owners/defaultProfilePicture.png';
import { deleteOwnerPhoto } from '@/features/customers/api/deleteOwnerPhoto.ts';

const ProfilePage = (): JSX.Element => {
  const [profilePicUrl, setProfilePicUrl] = useState<string>('');
  const { user } = useUser();
  const [petImageUrls, setPetImageUrls] = useState<Record<string, string>>({});
  const [owner, setOwner] = useState<OwnerResponseModel | null>(null);
  const [userDetails, setUserDetails] = useState<UserDetailsModel | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isAddPetModalOpen, setIsAddPetModalOpen] = useState<boolean>(false);
  const [isEditPetModalOpen, setIsEditPetModalOpen] = useState<boolean>(false);
  const [isUploadPhotoModalOpen, setIsUploadPhotoModalOpen] =
    useState<boolean>(false);
  const [selectedPetId, setSelectedPetId] = useState<string>('');
  const [petTypes, setPetTypes] = useState<PetTypeModel[]>([]);
  const navigate = useNavigate();
  const [isDeletePhotoModalOpen, setIsDeletePhotoModalOpen] =
    useState<boolean>(false);
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
        const ownerResponse = await getOwner(user.userId, true);
        const ownerData = ownerResponse.data;

        if (ownerData.photo && ownerData.photo.fileData) {
          const base64Data = ownerData.photo.fileData;
          const contentType = ownerData.photo.fileType || 'image/png';
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
          'Failed to fetch owner profile picture, using local default',
          err
        );
        if (isMounted) {
          setProfilePicUrl(''); // will fall back to default
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

  useEffect(() => {
    let isMounted = true;

    const fetchOwnerData = async (): Promise<void> => {
      try {
        const ownerResponse = await getOwner(user.userId);
        const ownerData = ownerResponse.data;

        try {
          const petsResponse = await axiosInstance.get(
            `/owners/${user.userId}/pets`,
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
                const photoIdFromPet = (pet as any).photoId;

                let petPhotoUrl: string;

                if (photoIdFromPet && photoIdFromPet !== '1') {
                    petPhotoUrl = await fetchPetPhotoUrl(
                        pet.petId,
                        photoIdFromPet,
                        pet.name
                    );
                } else {
                    petPhotoUrl = defaultProfile;
                }

                newPetImageUrls[pet.petId] = petPhotoUrl;
            }

            if (isMounted) {
                setPetImageUrls(newPetImageUrls);
                setOwner({
                    ...ownerData,
                    pets: petsData,
                });
            }
        } catch (petsError) {
          console.warn(
            'Error fetching pets, setting owner without pets:',
            petsError
          );
          if (isMounted) {
            setOwner({
              ...ownerData,
              pets: [],
            });
          }
        }
      } catch (error) {
        if (isMounted) {
          setError('Error fetching owner data');
        }
        console.error('Error fetching owner data:', error);
      }
    };

    fetchOwnerData();

    return () => {
      isMounted = false;
    };
  }, [user.userId]);

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

  const handleOpenUploadPhotoModal = (): void => {
    setIsUploadPhotoModalOpen(true);
  };

  const handleCloseUploadPhotoModal = (): void => {
    setIsUploadPhotoModalOpen(false);
  };

  const handlePetAdded = (newPet: PetResponseModel): void => {
    if (owner) {
      setOwner({
        ...owner,
        pets: [...(owner.pets || []), newPet],
      });
    }
  };

  const handleDeletePhoto = async (): Promise<void> => {
    try {
      await deleteOwnerPhoto(user.userId);
      if (profilePicUrl) {
        URL.revokeObjectURL(profilePicUrl);
      }
      setProfilePicUrl('');
      setIsDeletePhotoModalOpen(false);
    } catch (error) {
      console.error('Error deleting profile photo:', error);
      alert('Failed to delete profile photo. Please try again.');
    }
  };

  const handleDeletePet = async (petId: string): Promise<void> => {
    const confirmed = window.confirm(
      'Are you sure you want to delete this pet? This action cannot be undone.'
    );

    if (!confirmed) {
      return;
    }

    try {
      await deletePet(petId);

      if (owner) {
        setOwner({
          ...owner,
          pets: owner.pets?.filter(pet => pet.petId !== petId) || [],
        });
      }

      // eslint-disable-next-line no-console
      console.log('Pet deleted successfully');
    } catch (error) {
      console.error('Error deleting pet:', error);
      alert('Failed to delete pet. Please try again.');
    }
  };

  const handlePhotoUploaded = async (): Promise<void> => {
    if (!user.userId) return;

    try {
      const ownerResponse = await getOwner(user.userId, true);
      const ownerData = ownerResponse.data;

      if (ownerData.photo && ownerData.photo.fileData) {
        const base64Data = ownerData.photo.fileData;
        const contentType = ownerData.photo.fileType || 'image/png';
        const byteCharacters = atob(base64Data);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i);
        }
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], { type: contentType });
        const objectUrl = URL.createObjectURL(blob);

        if (profilePicUrl) {
          URL.revokeObjectURL(profilePicUrl);
        }
        setProfilePicUrl(objectUrl);
      } else {
        if (profilePicUrl) {
          URL.revokeObjectURL(profilePicUrl);
        }
        setProfilePicUrl('');
      }
    } catch (error) {
      console.error('Error refreshing profile picture:', error);
    }
  };

    const fetchPetPhotoUrl = async (
        petId: string,
        photoId: string,
        petName: string
    ): Promise<string> => {
        if (!photoId || photoId === '1') {
            return defaultProfile;
        }

        try {
            const response = await axiosInstance.get(
                `/pets/owners/${user.userId}/pets/${petId}`,
                {
                    useV2: false,
                    params: { includePhoto: true }
                }
            );
            const petData = response.data;

            console.log(`Full pet data for ${petName}:`, petData);
            console.log(`Photo object for ${petName}:`, petData.photo);

            if (petData.photo) {
                console.log(`Photo keys for ${petName}:`, Object.keys(petData.photo));
                console.log(`Photo data exists?`, !!petData.photo.data);
                console.log(`Photo fileData exists?`, !!(petData.photo as any).fileData);
            }

            if (petData.photo && petData.photo.data) {
                const base64Data = petData.photo.data;
                const contentType = petData.photo.contentType || 'image/png';
                const byteCharacters = atob(base64Data);
                const byteNumbers = new Array(byteCharacters.length);
                for (let i = 0; i < byteCharacters.length; i++) {
                    byteNumbers[i] = byteCharacters.charCodeAt(i);
                }
                const byteArray = new Uint8Array(byteNumbers);
                const blob = new Blob([byteArray], { type: contentType });
                return URL.createObjectURL(blob);
            } else {
                console.warn(`No photo data found for ${petName} (${petId}). Using default.`);
                return defaultProfile;
            }
        } catch (error) {
            console.error(`Error fetching photo for ${petName} (${petId}):`, error);
            return defaultProfile;
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

  //eliminated code duplication
  const fetchOwnerData = async (): Promise<void> => {
    if (!user.userId) return;

    try {
      const ownerResponse = await getOwner(user.userId);
      const ownerData = ownerResponse.data;
      if (ownerData.pets && ownerData.pets.length > 0) {
        setOwner(ownerData);
      } else {
        setOwner({
          ...ownerData,
          pets: [],
        });
      }
    } catch (error) {
      setError('Error fetching owner data');
      console.error('Error fetching owner data:', error);
    }
  };

  const handlePetUpdated = (updatedPet?: PetResponseModel): void => {
    if (updatedPet) {
      setOwner(prevOwner => {
        if (!prevOwner || !prevOwner.pets) return prevOwner;

        const updatedPets = prevOwner.pets.map(pet =>
          pet.petId === updatedPet.petId
            ? {
                ...updatedPet,
                birthDate: updatedPet.birthDate
                  ? new Date(updatedPet.birthDate)
                  : new Date(),
              }
            : pet
        );

        return {
          ...prevOwner,
          pets: updatedPets,
        };
      });
    } else {
      fetchOwnerData();
    }
  };

  const handlePetDeleted = (): void => {
    fetchOwnerData();
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
                  className="photo-button change"
                >
                  Change Photo
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
              {owner.firstName} {owner.lastName}&apos;s Profile
            </h1>
          </div>
          <div className="customers-profile-info">
            <p>
              <strong>Username:</strong> {userDetails?.username || 'Loading...'}
            </p>
            <p>
              <strong>First Name:</strong> {owner.firstName}
            </p>
            <p>
              <strong>Last Name:</strong> {owner.lastName}
            </p>
            <p>
              <strong>Address:</strong> {owner.address}
            </p>
            <p>
              <strong>City:</strong> {owner.city}
            </p>
            <p>
              <strong>Province:</strong> {owner.province}
            </p>
            <p>
              <strong>Telephone:</strong> {owner.telephone}
            </p>
          </div>
          <div className="customers-pets-section">
            <div className="customers-pets-header">
              <h3>Owner Pets</h3>
              <button
                className="customers-add-pet-button"
                onClick={handleAddPet}
              >
                Add Pet
              </button>
            </div>
            {owner.pets && owner.pets.length > 0 ? (
                <div className="customers-pets-list">
                    {owner.pets.map((pet: PetResponseModel) => (
                        <div key={pet.petId} className="customers-pet-card">
                            <div className="customers-pet-card-content">
                                <img
                                    src={petImageUrls[pet.petId] || defaultProfile}
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
        ownerId={user.userId}
        isOpen={isAddPetModalOpen}
        onClose={handleCloseAddPetModal}
        onPetAdded={handlePetAdded}
      />

      <EditPetModal
        isOpen={isEditPetModalOpen}
        onClose={handleCloseEditPetModal}
        petId={selectedPetId}
        ownerId={user.userId}
        onPetUpdated={handlePetUpdated}
        onPetDeleted={handlePetDeleted}
      />

      <UploadPhotoModal
        isOpen={isUploadPhotoModalOpen}
        onClose={handleCloseUploadPhotoModal}
        ownerId={user.userId}
        onPhotoUploaded={handlePhotoUploaded}
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
    </div>
  );
};

export default ProfilePage;
