import { useEffect, useState } from 'react';
import './AllUsers.css';
import axiosInstance from '@/shared/api/axiosInstance';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import axios from 'axios';
import { Link } from 'react-router-dom';

const AllUsers: React.FC = (): JSX.Element => {
  interface UserResponseModel {
    userId: string;
    roles: { id: number; name: string }[];
    username: string;
    email: string;
    verified: boolean;
  }

  const [users, setUsers] = useState<UserResponseModel[]>([]);

  useEffect(() => {
    const fetchUsers = async (): Promise<void> => {
      try {
        const response = await axiosInstance.get<UserResponseModel[]>('/users', { useV2: true });
        setUsers(response.data);
      } catch (error) {
        console.error('Error fetching users:', error);
      }
    };

    fetchUsers();
  }, []);

  const handleDelete = async (userId: string): Promise<void> => {
    const confirmed = window.confirm(
      '      Are you sure you want to permanently delete this user?      '
    );
    if (!confirmed) {
      return;
    }

    try {
      await axiosInstance.delete(`/users/${userId}`, { useV2: true });
      setUsers(users.filter(user => user.userId !== userId));
    } catch (error) {
      console.error('Error deleting user:', error);
}
  };

  return (
    <div>
      <NavBar />

      <div className="users-container">
        <h1>Users</h1>

        <table>
          <thead>
            <tr>
              <th>User Id</th>
              <th>Roles</th>
              <th>Username</th>
              <th>Email</th>
              <th>Verified</th>
            </tr>
          </thead>
          <tbody>
            {users.map(user => (
              <tr key={user.userId}>
                <td>
                  <Link to={`/users/${user.userId}`}>{user.userId}</Link>
                </td>
                <td>{user.roles.map(role => role.name).join(', ')}</td>
                <td>{user.username}</td>
                <td>{user.email}</td>
                <td>{user.verified ? 'Yes' : 'No'}</td>
                <td>
                  <button onClick={() => handleDelete(user.userId)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AllUsers;
