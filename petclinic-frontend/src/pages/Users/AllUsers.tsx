import { useEffect, useState } from 'react';
import './AllUsers.css';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import axios from 'axios';

const AllUsers: React.FC = (): JSX.Element => {
  interface UserResponseModel {
    userId: string;
    username: string;
    email: string;
    verified: boolean;
  }

  const [users, setUsers] = useState<UserResponseModel[]>([]);

  useEffect(() => {
    const fetchUsers = async (): Promise<void> => {
      try {
        const response = await axios.get(
          'http://localhost:8080/api/v2/gateway/users',
          { withCredentials: true }
        );
        setUsers(response.data);
      } catch (error) {
        console.error('Error fetching users:', error);
      }
    };

    fetchUsers();
  }, []);

  return (
    <div>
      <NavBar />

      <div className="users-container">
        <h1>Users</h1>

        <table>
          <thead>
            <tr>
              <th>User Id</th>
              <th>Username</th>
              <th>Email</th>
              <th>Verified</th>
            </tr>
          </thead>
          <tbody>
            {users.map(user => (
              <tr key={user.userId}>
                <td>{user.userId}</td>
                <td>{user.username}</td>
                <td>{user.email}</td>
                <td>{user.verified ? 'Yes' : 'No'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AllUsers;
