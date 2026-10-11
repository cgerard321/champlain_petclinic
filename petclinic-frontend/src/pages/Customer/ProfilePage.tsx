import * as React from 'react';
import CustomerProfilePage from '@/features/customers/components/CustomerProfilePage.tsx';
import UpdateCustomerForm from '@/features/customers/components/NewUpdateCustomerForm.tsx';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import { getCustomer } from '@/features/customers/api/getCustomer';
import { getPetTypes } from '@/features/customers/api/getPetTypes';
import { getUserDetails } from '@/features/customers/api/getUserDetails';
import { useUser } from '@/context/UserContext';
import { CustomerResponseModel } from '@/features/customers/models/CustomerResponseModel';
import { PetTypeModel } from '@/features/customers/models/PetTypeModel';
import { UserDetailsModel } from '@/features/customers/models/UserDetailsModel';
import { getPetTypeImage } from '@/features/customers/utils/petTypeMapping';
import axiosInstance from '@/shared/api/axiosInstance';
import './ProfilePage.css';
import { PetResponseModel } from '@/features/customers/models/PetResponseModel';
const ProfilePage: React.FC = (): JSX.Element => {
  const { user } = useUser();
  const [edit, setEdit] = React.useState<boolean>(false);

  const [customer, setCustomer] = React.useState<CustomerResponseModel | null>(
    null
  );
  const [userDetails, setUserDetails] = React.useState<UserDetailsModel | null>(
    null
  );
  const [petTypes, setPetTypes] = React.useState<PetTypeModel[]>([]);
  const [profilePicUrl, setProfilePicUrl] = React.useState<string>('');
  const [petImageUrls, setPetImageUrls] = React.useState<
    Record<string, string>
  >({});
  const [loading, setLoading] = React.useState<boolean>(true);
  const [error, setError] = React.useState<string | null>(null);

  React.useEffect(() => {
    const fetchProfileData = async (): Promise<void> => {
      if (!user.userId) return;
      try {
        setLoading(true);
        setError(null);
        const [customerResponse, userDetailsResponse, petTypesResponse] =
          await Promise.all([
            getCustomer(user.userId),
            getUserDetails(user.userId),
            getPetTypes(),
          ]);
        const petsResponse = await axiosInstance.get(
          `/pets/customers/${user.userId}/pets`,
          { useV2: false }
        );

        let petsData: PetResponseModel[] = [];

        if (typeof petsResponse.data === 'string') {
          const pieces = petsResponse.data.split('\n').filter(Boolean);

          for (const piece of pieces) {
            if (piece.startsWith('data:')) {
              try {
                const petData = piece.slice(5).trim();
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

        setCustomer({
          ...customerResponse.data,
          pets: petsData,
        });
        setUserDetails(userDetailsResponse.data);
        setPetTypes(petTypesResponse);
      } catch (error) {
        console.error('Error fetching profile data:', error);
        setError('Error fetching profile data');
      } finally {
        setLoading(false);
      }
    };
    fetchProfileData();
  }, [user.userId]);

  const fetchProfilePhoto = React.useCallback(async (): Promise<void> => {
    if (!user.userId) return;
    try {
      const customerResponse = await getCustomer(user.userId, true);
      const customerData = customerResponse.data;
      if (customerData.photo?.fileData) {
        const base64Data = customerData.photo.fileData;
        const contentType = customerData.photo.fileType || 'image/png';
        const byteCharacters = atob(base64Data);
        const byteNumbers = new Array(byteCharacters.length);
        for (let i = 0; i < byteCharacters.length; i++) {
          byteNumbers[i] = byteCharacters.charCodeAt(i);
        }
        const byteArray = new Uint8Array(byteNumbers);
        const blob = new Blob([byteArray], {
          type: contentType,
        });
        const objectUrl = URL.createObjectURL(blob);

        setProfilePicUrl(previousUrl => {
          if (previousUrl) {
            URL.revokeObjectURL(previousUrl);
          }
          return objectUrl;
        });
      } else {
        setProfilePicUrl(previousUrl => {
          if (previousUrl) {
            URL.revokeObjectURL(previousUrl);
          }
          return '';
        });
      }
    } catch (error) {
      console.error('Error fetching profile picture:', error);
    }
  }, [user.userId]);

  React.useEffect(() => {
    if (!customer) return;
    fetchProfilePhoto();
  }, [customer, fetchProfilePhoto]);

  const fetchPetPhoto = React.useCallback(
    async (petId: string, petTypeId: string): Promise<void> => {
      try {
        const response = await axiosInstance.get(`/pets/${petId}`, {
          useV2: false,
          params: {
            includePhoto: true,
          },
        });
        const petData = response.data;
        if (petData.photo && (petData.photo.data || petData.photo.fileData)) {
          const base64Data = petData.photo.data || petData.photo.fileData;
          const contentType =
            petData.photo.contentType || petData.photo.fileType || 'image/png';
          const byteCharacters = atob(base64Data);
          const byteNumbers = new Array(byteCharacters.length);
          for (let i = 0; i < byteCharacters.length; i++) {
            byteNumbers[i] = byteCharacters.charCodeAt(i);
          }
          const byteArray = new Uint8Array(byteNumbers);
          const blob = new Blob([byteArray], {
            type: contentType,
          });
          const objectUrl = URL.createObjectURL(blob);
          setPetImageUrls(previousUrls => {
            const previousUrl = previousUrls[petId];
            if (previousUrl) {
              URL.revokeObjectURL(previousUrl);
            }
            return {
              ...previousUrls,
              [petId]: objectUrl,
            };
          });
        } else {
          setPetImageUrls(previousUrls => ({
            ...previousUrls,
            [petId]: getPetTypeImage(petTypeId, petTypes),
          }));
        }
      } catch (error) {
        console.error(`Error fetching pet photo for ${petId}:`, error);
        setPetImageUrls(previousUrls => ({
          ...previousUrls,
          [petId]: getPetTypeImage(petTypeId, petTypes),
        }));
      }
    },
    [petTypes]
  );

  React.useEffect(() => {
    if (!customer?.pets || petTypes.length === 0) {
      return;
    }
    const fetchAllPetPhotos = async (): Promise<void> => {
      await Promise.all(
        customer.pets!.map(pet => fetchPetPhoto(pet.petId, pet.petTypeId))
      );
    };
    fetchAllPetPhotos();
  }, [customer?.pets, petTypes, fetchPetPhoto]);

  const refreshPetPhoto = async (petId: string): Promise<void> => {
    const pet = customer?.pets?.find(currentPet => currentPet.petId === petId);
    if (!pet) return;
    await fetchPetPhoto(pet.petId, pet.petTypeId);
  };

  const changeToUpdatePage = (): void => {
    setEdit(true);
  };
  const changeToProfilePage = (): void => {
    setEdit(false);
  };

  if (loading) {
    return <p>Loading...</p>;
  }
  if (error) {
    return <p>{error}</p>;
  }
  if (!customer || !userDetails) {
    return <p>Unable to load profile.</p>;
  }

  return (
    <div>
      <NavBar />
      {edit ? (
        <UpdateCustomerForm
          customer={customer}
          userDetails={userDetails}
          profilePicUrl={profilePicUrl}
          setCustomer={setCustomer}
          onBackButtonClick={changeToProfilePage}
        />
      ) : (
        <CustomerProfilePage
          customer={customer}
          userDetails={userDetails}
          petTypes={petTypes}
          profilePicUrl={profilePicUrl}
          petImageUrls={petImageUrls}
          pets={customer.pets}
          setCustomer={setCustomer}
          setPetImageUrls={setPetImageUrls}
          setProfilePicUrl={setProfilePicUrl}
          onUpdateProfileClick={changeToUpdatePage}
          onProfilePhotoUploaded={fetchProfilePhoto}
          onPetPhotoUploaded={refreshPetPhoto}
        />
      )}
    </div>
  );
};
export default ProfilePage;
