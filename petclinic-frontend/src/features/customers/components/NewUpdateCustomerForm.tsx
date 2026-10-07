import { FormEvent, useEffect, useState } from 'react';
import { getCustomer } from '@/features/customers/api/getCustomer.ts';

import { getUserDetails } from '@/features/customers/api/getUserDetails';
import { CustomerResponseModel } from '@/features/customers/models/CustomerResponseModel.ts';

import { UserDetailsModel } from '@/features/customers/models/UserDetailsModel';
import { useUser } from '@/context/UserContext';

import defaultProfile from '@/assets/Customers/defaultProfilePicture.png';

import { useToast } from '@/shared/components/toast/ToastProvider';
import { useUsernameValidation } from '../hooks/useUsernameValidation';
import { CustomerRequestModel } from '@/features/customers/models/CustomerRequestModel.ts';
import { provincesOfCanada } from '../utils/provinces';
import { updateUsername } from '@/features/customers/api/updateUsername.ts';
import { updateCustomer } from '@/features/customers/api/updateCustomer.ts';
import { validateTelephone } from '@/features/customers/utils/validation.ts';
import { t } from 'i18next';

const CustomerProfilePage = ({
  onBackButtonClick,
}: {
  onBackButtonClick: () => void;
}): JSX.Element => {
  const { showToast } = useToast();
  const { user, checkSession } = useUser();
  const { validateUsernameField } = useUsernameValidation();
  const [customer, setCustomer] = useState<CustomerRequestModel>({
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    province: '',
    telephone: '',
  });
  const [userDetails, setUserDetails] = useState<UserDetailsModel | null>(null);
  const [username, setUsername] = useState<string>('');
  const [errors, setErrors] = useState<{ [key: string]: string }>({});
  const [profilePicUrl, setProfilePicUrl] = useState<string>('');

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
    const fetchData = async (): Promise<void> => {
      try {
        const [customerResponse, userResponse] = await Promise.all([
          getCustomer(user.userId),
          getUserDetails(user.userId),
        ]);

        const customerData: CustomerResponseModel = customerResponse.data;
        setCustomer({
          firstName: customerData.firstName,
          lastName: customerData.lastName,
          address: customerData.address,
          city: customerData.city,
          province: customerData.province,
          telephone: customerData.telephone,
        });

        const userData: UserDetailsModel = userResponse.data;
        setUserDetails(userData);
        setUsername(userData.username);
      } catch (error) {
        console.error('Error fetching data:', error);
        setUserDetails({
          userId: user.userId,
          username: 'Unknown',
          email: '',
          roles: [],
          verified: false,
          disabled: false,
        });
        setUsername('Unknown');
      }
    };

    fetchData();
    return () => {
      isMounted = false;
      if (objectUrl) {
        URL.revokeObjectURL(objectUrl);
      }
    };
  }, [user.userId]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>
  ): void => {
    const { name, value } = e.target;
    setCustomer({ ...customer, [name]: value });
  };

  const handleUsernameChange = (
    e: React.ChangeEvent<HTMLInputElement>
  ): void => {
    setUsername(e.target.value);
    if (errors.username) {
      setErrors(prev => ({ ...prev, username: '' }));
    }
  };

  const validate = async (): Promise<boolean> => {
    const newErrors: { [key: string]: string } = {};
    if (!customer.firstName)
      newErrors.firstName = 'validation.firstNameRequired';
    if (!customer.lastName) newErrors.lastName = 'validation.lastNameRequired';
    if (!customer.address) newErrors.address = 'validation.addressRequired';
    if (!customer.city) newErrors.city = 'validation.cityRequired';
    if (!customer.province) newErrors.province = 'validation.provinceRequired';

    const telephoneError = validateTelephone(customer.telephone);
    if (telephoneError) newErrors.telephone = telephoneError;

    const usernameError = await validateUsernameField(
      username,
      userDetails?.username
    );
    if (usernameError) {
      newErrors.username = usernameError;
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    if (!(await validate())) return;

    try {
      await updateCustomer(user.userId, customer);

      if (userDetails && username !== userDetails.username) {
        await updateUsername(user.userId, username);

        // Refresh the session so context (and navbar, etc.) picks up the new username
        await checkSession();
      }

      showToast(t('customerForm.updateSuccess'), 'success');
    } catch (error) {
      console.error('Error:', error);
      showToast('customerForm.updateError', 'error');
    }
  };

  return (
    <div>
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
            </div>
            <h1>
              {customer.firstName} {customer.lastName}&apos;s Profile
            </h1>
          </div>

          <div>
            <form onSubmit={handleSubmit} className="customers-profile-info">
              <p>
                <strong>Username:</strong>{' '}
                <input
                  type="text"
                  name="username"
                  value={username}
                  onChange={handleUsernameChange}
                />
                {errors.username && (
                  <span className="error">{t(errors.username)}</span>
                )}
              </p>
              <p>
                <strong>First Name:</strong>{' '}
                <input
                  type="text"
                  name="firstName"
                  value={customer.firstName}
                  onChange={handleChange}
                />
              </p>
              <p>
                <strong>Last Name:</strong>{' '}
                <input
                  type="text"
                  name="lastName"
                  value={customer.lastName}
                  onChange={handleChange}
                />
                {errors.lastName && (
                  <span className="error">{errors.lastName}</span>
                )}
              </p>
              <p>
                <strong>Address:</strong>{' '}
                <input
                  type="text"
                  name="address"
                  value={customer.address}
                  onChange={handleChange}
                />
              </p>
              <p>
                <strong>City:</strong>
                <input
                  type="text"
                  name="city"
                  value={customer.city}
                  onChange={handleChange}
                />
              </p>
              <p>
                <strong>Province:</strong>
                <select
                  name="province"
                  value={customer.province}
                  onChange={handleChange}
                >
                  <option value="">{'customerForm.selectProvince'}</option>
                  {provincesOfCanada.map(province => (
                    <option key={province} value={province}>
                      {province}
                    </option>
                  ))}
                </select>
              </p>
              <p>
                <strong>Telephone:</strong>{' '}
                <input
                  type="text"
                  name="telephone"
                  value={customer.telephone}
                  onChange={handleChange}
                />
              </p>
            </form>
          </div>
          <div className="button-container">
            <button
              className="customers-backButton"
              onClick={() => onBackButtonClick()}
            >
              Back
            </button>
            <button className="customers-updateButton">Save Changes</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CustomerProfilePage;
