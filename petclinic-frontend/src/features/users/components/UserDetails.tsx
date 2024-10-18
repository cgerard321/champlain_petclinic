import { FC, useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import './UserDetails.css';

interface UserResponseModel {
  userId: string;
  username: string;
  email: string;
  verified: boolean;
  roles: { id: number; name: string }[];
}

const UserDetails: FC = () => {
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();

  const [user, setUser] = useState<UserResponseModel | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchUserDetails = async (): Promise<void> => {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/v2/gateway/users/${userId}`,
          { withCredentials: true }
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
        await axios.delete(
          `http://localhost:8080/api/v2/gateway/users/${userId}`,
          { withCredentials: true }
        );
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
        {/* User Info */}
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
    </div>
  );
};

export default UserDetails;
