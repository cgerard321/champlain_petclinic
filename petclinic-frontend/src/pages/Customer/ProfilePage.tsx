import { useEffect, useState } from 'react';
import { getOwner } from '@/features/customers/api/getOwner';
import { getPetTypes } from '@/features/customers/api/getPetTypes';
import { getUserDetails } from '@/features/customers/api/getUserDetails';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel';
import { PetTypeModel } from '@/features/customers/models/PetTypeModel';
import { UserDetailsModel } from '@/features/customers/models/UserDetailsModel';
import { useUser } from '@/context/UserContext';
import { NavBar } from '@/layouts/AppNavBar';
import AddPetModal from '@/features/customers/components/AddPetModal';
import EditPetModal from '@/features/customers/components/EditPetModal';
import UploadPhotoModal from '@/features/customers/components/UploadPhotoModal';
import './ProfilePage.css';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '@/shared/api/axiosInstance';
import { getPetTypeName, getPetTypeImage } from '@/features/customers/utils/petTypeMapping';
import { deleteOwnerPhoto } from '@/features/customers/api/deleteOwnerPhoto';
import { handlePetPhoto } from '@/features/customers/api/handlePetPhoto';
import { useConfirmModal } from '@/shared/hooks/useConfirmModal';
import defaultProfile from '@/assets/Owners/defaultProfilePicture.png';
import { deletePet } from '@/features/customers/api/deletePet';

const ProfilePage = (): JSX.Element => {
  const [profilePicUrl, setProfilePicUrl] = useState<string>('');
  const { user } = useUser();
  const [petImageUrls, setPetImageUrls] = useState<Record<string, string>>({});
  const [owner, setOwner] = useState<OwnerResponseModel | null>(null);
  const [userDetails, setUserDetails] = useState<UserDetailsModel | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isAddPetModalOpen, setIsAddPetModalOpen] = useState(false);
  const [isEditPetModalOpen, setIsEditPetModalOpen] = useState(false);
  const [isUploadPhotoModalOpen, setIsUploadPhotoModalOpen] = useState(false);
  const [selectedPetId, setSelectedPetId] = useState('');
  const [petTypes, setPetTypes] = useState<PetTypeModel[]>([]);
  const [isDeletePhotoModalOpen, setIsDeletePhotoModalOpen] = useState(false);
  const navigate = useNavigate();
  const { ConfirmModal } = useConfirmModal();

  // Fetch pet types
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

  // Fetch user details
  useEffect(() => {
    const fetchUserData = async (): Promise<void> => {
      try {
        const userDetailsResponse = await getUserDetails(user.userId);
        setUserDetails(userDetailsResponse.data);
      } catch (error) {
        console.error('Error fetching user details:', error);
        setUserDetails({
          userId: user.userId,
          username: 'Unknown',
          email: '',
          roles: [],
          verified: false,
          disabled: false,
        });
      }
    };
    fetchUserData();
  }, [user.userId]);

  // Fetch profile picture
  useEffect(() => {
    let objectUrl: string | null = null;
    const fetchProfilePic = async (): Promise<void> => {
      if (!user.userId) return;
      try {
        const ownerResponse = await getOwner(user.userId, true);
        const ownerData = ownerResponse.data;
        if (ownerData.photo && ownerData.photo.fileData) {
          const base64Data = ownerData.photo.fileData;
          const contentType = ownerData.photo.fileType || 'image/png';
          const byteArray = Uint8Array.from(atob(base64Data), c => c.charCodeAt(0));
          const blob = new Blob([byteArray], { type: contentType });
          objectUrl = URL.createObjectURL(blob);
          setProfilePicUrl(objectUrl);
        } else {
          setProfilePicUrl('');
        }
      } catch (err) {
        console.warn('Failed to fetch owner profile picture', err);
        setProfilePicUrl('');
      }
    };
    fetchProfilePic();
    return () => {
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [user.userId]);

  // Fetch owner + pets + pet photos
  useEffect(() => {
    const fetchOwnerData = async (): Promise<void> => {
      try {
        const ownerResponse = await getOwner(user.userId);
        const ownerData = ownerResponse.data;
        const petsResponse = await axiosInstance.get(`/owners/${user.userId}/pets`, { useV2: false });
        const petsData: PetResponseModel[] = Array.isArray(petsResponse.data) ? petsResponse.data : [];

        const newPetImageUrls: Record<string, string> = {};
        for (const pet of petsData) {
          const photoUrl = await fetchPetPhotoUrl(pet.petId, pet.name, pet.petTypeId);
          newPetImageUrls[pet.petId] = photoUrl;
        }

        setOwner({ ...ownerData, pets: petsData });
        setPetImageUrls(newPetImageUrls);
      } catch (error) {
        console.error('Error fetching owner data:', error);
        setError('Error fetching owner data');
      }
    };
    fetchOwnerData();
  }, [user.userId, petTypes]);

  const fetchPetPhotoUrl = async (petId: string, petName: string, petTypeId: string): Promise<string> => {
    try {
      const response = await axiosInstance.get(`/pets/${petId}`, {
        useV2: false,
        params: { includePhoto: true },
      });
      const petData = response.data;
      if (petData.photo && (petData.photo.data || petData.photo.fileData)) {
        const base64Data = petData.photo.data || petData.photo.fileData;
        const contentType = petData.photo.contentType || petData.photo.fileType || 'image/png';
        const byteArray = Uint8Array.from(atob(base64Data), c => c.charCodeAt(0));
        const blob = new Blob([byteArray], { type: contentType });
        return URL.createObjectURL(blob);
      }
      return getPetTypeImage(petTypeId, petTypes);
    } catch (error) {
      console.error(`Error fetching photo for ${petName}:`, error);
      return getPetTypeImage(petTypeId, petTypes);
    }
  };

  const handleAddPet = (): void => setIsAddPetModalOpen(true);
  const handleCloseAddPetModal = (): void => setIsAddPetModalOpen(false);

  const handlePetAdded = (newPet: PetResponseModel): void => {
    if (owner) setOwner({ ...owner, pets: [...(owner.pets || []), newPet] });
  };

  const handleDeletePet = async (petId: string): Promise<void> => {
    const confirmed = window.confirm('Are you sure you want to delete this pet?');
    if (!confirmed) return;
    try {
      await deletePet(petId);
      if (owner) {
        setOwner({ ...owner, pets: owner.pets?.filter(pet => pet.petId !== petId) || [] });
      }
    } catch (error) {
      console.error('Error deleting pet:', error);
      alert('Failed to delete pet.');
    }
  };

  const handleDeletePetPhoto = async (petId: string, petTypeId: string): Promise<void> => {
    const confirmed = window.confirm("Are you sure you want to delete this pet's photo?");
    if (!confirmed) return;
    try {
      await handlePetPhoto(petId); // delete mode
      setPetImageUrls(prev => ({
        ...prev,
        [petId]: getPetTypeImage(petTypeId, petTypes),
      }));
    } catch (error) {
      console.error('Error deleting pet photo:', error);
      alert('Failed to delete pet photo.');
    }
  };

  const handleUpdateClick = (): void => navigate(AppRoutePaths.CustomerProfileEdit);

  const calculateAge = (birthDate: Date): number => {
    const birth = new Date(birthDate);
    const ageDiffMs = Date.now() - birth.getTime();
    const ageDate = new Date(ageDiffMs);
    return Math.abs(ageDate.getUTCFullYear() - 1970);
  };

  if (error) return <p>{error}</p>;
  if (!owner) return <p>Loading...</p>;

  return (
      <div>
        <NavBar />
        <div className="customers-page customers-container-profile">
          <div className="customers-profile-card shadow-lg p-5 mb-5 bg-white rounded">
            <div className="customers-profile-header" style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
              <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px' }}>
                <img src={profilePicUrl || defaultProfile} alt="Profile Picture" className="profile-picture" />
                <div className="photo-buttons-container">
                  <button onClick={() => setIsUploadPhotoModalOpen(true)} className="photo-button change">
                    Change Photo
                  </button>
                  {profilePicUrl && (
                      <button onClick={() => setIsDeletePhotoModalOpen(true)} className="photo-button delete">
                        Delete Photo
                      </button>
                  )}
                </div>
              </div>
              <h1>{owner.firstName} {owner.lastName}&apos;s Profile</h1>
            </div>

            <div className="customers-profile-info">
              <p><strong>Username:</strong> {userDetails?.username || 'Loading...'}</p>
              <p><strong>First Name:</strong> {owner.firstName}</p>
              <p><strong>Last Name:</strong> {owner.lastName}</p>
              <p><strong>Address:</strong> {owner.address}</p>
              <p><strong>City:</strong> {owner.city}</p>
              <p><strong>Province:</strong> {owner.province}</p>
              <p><strong>Telephone:</strong> {owner.telephone}</p>
            </div>

            <div className="customers-pets-section">
              <div className="customers-pets-header">
                <h3>Owner Pets</h3
                <button className="customers-add-pet-button" onClick={handleAddPet}>Add Pet</button>
              </div>

              {owner.pets && owner.pets.length > 0 ? (
                  <div className="customers-pets-list">
                    {owner.pets.map((pet: PetResponseModel) => (
                        <div key={pet.petId} className="customers-pet-card">
                          <div className="customers-pet-card-content">
                            <img
                                src={petImageUrls[pet.petId] || getPetTypeImage(pet.petTypeId, petTypes)}
                                alt={`${pet.name} profile`}
                                className="pet-profile-picture"
                            />
                            <div className="customers-pet-info">
                              <h4 className="customers-pet-name">{pet.name}</h4>
                              <div className="customers-pet-details">
                                <span><strong>Type:</strong> {getPetTypeName(pet.petTypeId, petTypes)}</span>
                                <span><strong>Weight:</strong> {pet.weight}kg</span>
                                <span><strong>Age:</strong> {calculateAge(pet.birthDate)} years</span>
                              </div>
                            </div>
                          </div>

                          <div className="customers-pet-actions">
                            <button
                                className="customers-edit-pet-button"
                                onClick={() => setSelectedPetId(pet.petId)}
                            >
                              Edit Pet
                            </button>

                            {petImageUrls[pet.petId] &&
                            petImageUrls[pet.petId] !== getPetTypeImage(pet.petTypeId, petTypes) ? (
                                <button
                                    className="customers-delete-photo-button"
                                    onClick={() => handleDeletePetPhoto(pet.petId, pet.petTypeId)}
                                >
                                  Delete Photo
                                </button>
                            ) : (
                                <button
                                    className="customers-upload-photo-button"
                                    onClick={async () => {
                                      const fileInput = document.createElement('input');
                                      fileInput.type = 'file';
                                      fileInput.accept = 'image/*';
                                      fileInput.onchange = async e => {
                                        const file = (e.target as HTMLInputElement).files?.[0];
                                        if (!file) return;
                                        try {
                                          await handlePetPhoto(pet.petId, file);
                                          const newUrl = await fetchPetPhotoUrl(pet.petId, pet.name, pet.petTypeId);
                                          setPetImageUrls(prev => ({ ...prev, [pet.petId]: newUrl }));
                                        } catch (err) {
                                          console.error('Error uploading pet photo:', err);
                                          alert('Failed to upload pet photo.');
                                        }
                                      };
                                      fileInput.click();
                                    }}
                                >
                                  Add Photo
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

            <button className="customers-updateButton" onClick={handleUpdateClick}>
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
            onClose={() => setIsEditPetModalOpen(false)}
            petId={selectedPetId}
            ownerId={user.userId}
        />

        <UploadPhotoModal
            isOpen={isUploadPhotoModalOpen}
            onClose={() => setIsUploadPhotoModalOpen(false)}
            ownerId={user.userId}
            onPhotoUploaded={() => window.location.reload()}
        />

        {isDeletePhotoModalOpen && (
            <div className="modal-overlay" onClick={() => setIsDeletePhotoModalOpen(false)}>
              <div className="modal-content" onClick={e => e.stopPropagation()}>
                <div className="modal-header">
                  <h2>Delete Profile Photo</h2>
                  <button className="close-button" onClick={() => setIsDeletePhotoModalOpen(false)}>×</button>
                </div>
                <div className="modal-body">
                  <p>Are you sure you want to delete your profile photo?</p>
                  <div className="modal-footer">
                    <button onClick={() => setIsDeletePhotoModalOpen(false)} className="cancel-button">Cancel</button>
                    <button
                        onClick={async () => {
                          await deleteOwnerPhoto(user.userId);
                          setProfilePicUrl('');
                          setIsDeletePhotoModalOpen(false);
                        }}
                        className="delete-button"
                    >
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
