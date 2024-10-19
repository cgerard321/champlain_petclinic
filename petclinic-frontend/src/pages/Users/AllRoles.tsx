import { FC, useEffect, useState } from 'react';
import './AllRoles.css';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import { getAllRoles } from '@/features/users/api/getAllRoles';
import { addRole } from '@/features/users/api/addRole';

const AllRoles: FC = (): JSX.Element => {
  interface RoleResponseModel {
    id: number;
    name: string;
  }

  const [roles, setRoles] = useState<RoleResponseModel[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newRoleName, setNewRoleName] = useState('');

  useEffect(() => {
    const fetchRoles = async (): Promise<void> => {
      try {
        const response = await getAllRoles();
        setRoles(response.data);
      } catch (error) {
        console.error('Error fetching roles:', error);
      }
    };

    fetchRoles().catch(error => console.error('Error in fetchRoles:', error));
  }, []);

  const handleCreateRole = async (): Promise<void> => {
    try {
      await addRole({ name: newRoleName.toUpperCase() });
      setIsModalOpen(false);
      setNewRoleName('');
      // Refresh roles list
      const response = await getAllRoles();
      setRoles(response.data);
    } catch (error) {
      console.error('Error creating role:', error);
    }
  };

  return (
    <div>
      <NavBar />

      <div className="roles-container">
        <h1>Roles</h1>
        <button
          className="create-role-button"
          onClick={() => setIsModalOpen(true)}
        >
          Create Role
        </button>

        <table>
          <thead>
            <tr>
              <th>Role Id</th>
              <th>Role Name</th>
            </tr>
          </thead>
          <tbody>
            {roles.map(role => (
              <tr key={role.id}>
                <td>{role.id}</td>
                <td>{role.name}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {isModalOpen && (
        <div className="modal">
          <div className="modal-content">
            <h2>Create New Role</h2>
            <input
              type="text"
              value={newRoleName}
              onChange={e => setNewRoleName(e.target.value)}
              placeholder="Role Name"
            />
            <div className="modal-buttons">
              <button onClick={handleCreateRole}>Confirm</button>
              <button onClick={() => setIsModalOpen(false)}>Cancel</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AllRoles;
