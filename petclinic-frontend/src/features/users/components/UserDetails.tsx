import { FC, useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Role } from '@/shared/models/Role';
import './UserDetails.css';

interface UserResponseModel {
  userId: string;
  username: string;
  email: string;
  verified: boolean;
  roles: Role[];
}

const UserDetails: FC = () => {
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();

  const [user, setUser] = useState<UserResponseModel | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
  const [roles, setRoles] = useState<Role[]>([]);
  const [userRoles, setUserRoles] = useState<Set<string>>(new Set());
  const [modalLoading, setModalLoading] = useState<boolean>(true);
  const [modalError, setModalError] = useState<string | null>(null);

  useEffect(() => {
    const fetchUserDetails = async (): Promise<void> => {
      try {
        const response = await axiosInstance.get<UserResponseModel>(
          `/users/${userId}`
        );
        setUser(response.data);
        setError(null);
      } catch (err) {
        console.error('Error fetching user details:', err);
        setError('Failed to fetch user details. Please try again later.');
      } finally {
        setLoading(false);
      }
    };

    if (userId) {
      fetchUserDetails();
    }
  }, [userId]);

  const fetchRoles = async (): Promise<void> => {
    try {
      const rolesResponse = await axiosInstance.get<Role[]>('/roles');
      setRoles(rolesResponse.data);
      setUserRoles(new Set(user?.roles.map((role: Role) => role.name) || []));
      setModalError(null);
    } catch (err) {
      console.error('Error fetching roles:', err);
      setModalError('Failed to fetch roles. Please try again later.');
    } finally {
      setModalLoading(false);
    }
  };

  const handleCheckboxChange = (roleName: string): void => {
    setUserRoles(prevRoles => {
      const newRoles = new Set(prevRoles);
      if (newRoles.has(roleName)) {
        newRoles.delete(roleName);
      } else {
        newRoles.add(roleName);
      }
      return newRoles;
    });
  };

  const handleSubmit = async (
    event: React.FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    try {
      await axiosInstance.patch(`/users/${userId}`, {
        roles: Array.from(userRoles),
      });
      alert('User roles updated successfully.');
      setIsModalOpen(false);
      window.location.reload();
    } catch (err) {
      console.error('Error updating user roles:', err);
      alert('Error updating user roles. Please try again.');
    }
  };

  const handleEditClick = (): void => {
    navigate(`/users/${userId}/edit`);
  };

  const handleBackClick = (): void => {
    navigate('/users');
  };

  const handleOwnerInfoClick = (): void => {
    navigate(`/customers/${userId}`);
  };

  const handleDelete = async (userId: string): Promise<void> => {
    const confirmDelete = window.confirm(
      'Are you sure you want to delete this user?'
    );
    if (confirmDelete) {
      try {
        await axiosInstance.delete(`/users/${userId}`);
        alert('User deleted successfully.');
        navigate('/users');
      } catch (error) {
        console.error('Error deleting user:', error);
        alert('Error deleting user. Please try again.');
      }
    }
  };

  if (loading) {
    return (
      <div className="centered-message">
        <p>Loading...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="centered-message">
        <p>
          Error: {error} <br />
          <Link to="/users">Return to all users</Link>
        </p>
      </div>
    );
  }

  if (!user) {
    return (
      <div className="centered-message">
        <p>No user found.</p>
      </div>
    );
  }

  return (
    <div className="user-details-card">
      <h2>User Details for {user.username}</h2>
      <div className="user-details-container">
        <div className="section user-info">
          <h3>User Info</h3>
          <p>
            <strong>User ID: </strong>
            {user.userId}
          </p>
          <p>
            <strong>Email: </strong>
            {user.email}
          </p>
          <p>
            <strong>Verified: </strong>
            {user.verified ? 'Yes' : 'No'}
          </p>
          <p>
            <strong>Roles: </strong>
            {user.roles.map(role => role.name).join(', ')}
          </p>
        </div>
      </div>

      <div className="user-details-buttons">
        <button className="user-details-button" onClick={handleEditClick}>
          Update user info
        </button>
        <button
          className="user-details-button"
          onClick={() => {
            setIsModalOpen(true);
            fetchRoles();
          }}
        >
          Update User Roles
        </button>
        <button className="user-details-button" onClick={handleBackClick}>
          Back to All Users
        </button>
        {user.roles.some(role => role.name === 'OWNER') && (
          <button
            className="user-details-button"
            onClick={handleOwnerInfoClick}
          >
            Connected Owner Info
          </button>
        )}
        <button
          className="user-delete-button"
          onClick={() => handleDelete(user.userId)}
          title="Delete"
        >
          Delete User
        </button>
      </div>

      {isModalOpen && (
        <div className="update-role-modal-overlay">
          <div className="update-role-modal-content">
            <h2>Update User Roles</h2>
            <p>Select the roles you want to assign to the user:</p>
            {modalLoading ? (
              <p>Loading roles...</p>
            ) : modalError ? (
              <p>{modalError}</p>
            ) : (
              <form onSubmit={handleSubmit} className="update-role-modal-form">
                {roles.map(role => (
                  <div key={role.id} className="update-role-modal-checkbox">
                    <label>
                      <input
                        type="checkbox"
                        checked={userRoles.has(role.name)}
                        onChange={() => handleCheckboxChange(role.name)}
                      />
                      {role.name}
                    </label>
                  </div>
                ))}
                <div className="update-role-modal-buttons">
                  <button
                    type="submit"
                    className="update-role-modal-save-button"
                  >
                    Save
                  </button>
                  <button
                    type="button"
                    className="update-role-modal-cancel-button"
                    onClick={() => setIsModalOpen(false)}
                  >
                    Cancel
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default UserDetails;
