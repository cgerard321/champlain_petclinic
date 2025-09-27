import { useEffect, useState } from 'react';
import { getOwner } from '@/features/customers/api/getOwner';
import { getPetTypes } from '@/features/customers/api/getPetTypes';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel.ts';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel.ts';
import { PetTypeModel } from '@/features/customers/models/PetTypeModel';
import { useUser } from '@/context/UserContext';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import AddPetModal from '@/features/customers/components/AddPetModal';
import './ProfilePage.css';
import { AppRoutePaths } from '@/shared/models/path.routes.ts';
import { useNavigate } from 'react-router-dom';
import axiosInstance from '@/shared/api/axiosInstance';
import { getPetTypeName } from '@/features/customers/utils/petTypeMapping';

const ProfilePage = (): JSX.Element => {
  const { user } = useUser();
  const [owner, setOwner] = useState<OwnerResponseModel | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isAddPetModalOpen, setIsAddPetModalOpen] = useState<boolean>(false);
  const [petTypes, setPetTypes] = useState<PetTypeModel[]>([]);
  const navigate = useNavigate();

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

          setOwner({
            ...ownerData,
            pets: petsData,
          });
        } catch (petsError) {
          console.warn(
            'Error fetching pets, setting owner without pets:',
            petsError
          );
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

    fetchOwnerData();
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

  const handlePetAdded = (newPet: PetResponseModel): void => {
    if (owner) {
      setOwner({
        ...owner,
        pets: [...(owner.pets || []), newPet],
      });
    }
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
      <div className="profile-page container-profile">
        <div className="profile-card shadow-lg p-5 mb-5 bg-white rounded">
          <h1>
            {owner.firstName} {owner.lastName}&apos;s Profile
          </h1>
          <div className="profile-info">
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
          <div className="pets-section">
            <div className="pets-header">
              <h3>Owner Pets</h3>
              <button className="add-pet-button" onClick={handleAddPet}>
                Add Pet
              </button>
            </div>
            {owner.pets && owner.pets.length > 0 ? (
              <div className="pets-list">
                {owner.pets.map((pet: PetResponseModel) => (
                  <div key={pet.petId} className="pet-card">
                    <div className="pet-info">
                      <h4 className="pet-name">{pet.name}</h4>
                      <div className="pet-details">
                        <span className="pet-detail">
                          <strong>Type:</strong>{' '}
                          {getPetTypeName(pet.petTypeId, petTypes)}
                        </span>
                        <span className="pet-detail">
                          <strong>Weight:</strong> {pet.weight}kg
                        </span>
                        <span className="pet-detail">
                          <strong>Age:</strong> {calculateAge(pet.birthDate)}{' '}
                          years
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="no-pets">
                <p>No pets found.</p>
                <p className="no-pets-subtitle">Add your first pet</p>
              </div>
            )}
          </div>
          <button className="updateButton" onClick={handleUpdateClick}>
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
    </div>
  );
};

export default ProfilePage;
