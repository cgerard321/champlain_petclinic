import { FC, useEffect, useState } from 'react';
import './AllRoles.css';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import { getAllRoles } from '@/features/users/api/getAllRoles';
import { addRole } from '@/features/users/api/addRole';
import { updateRole } from '@/features/users/api/updateRole.ts';

const AllRoles: FC = (): JSX.Element => {
  interface RoleResponseModel {
    id: number;
    name: string;
  }

  const [roles, setRoles] = useState<RoleResponseModel[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
  const [newRoleName, setNewRoleName] = useState('');
  const [roleToUpdate, setRoleToUpdate] = useState<RoleResponseModel | null>(null);
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
      const formattedRoleName = newRoleName.replace(/\s+/g, '_').toUpperCase();
      await addRole({ name: formattedRoleName });
      setIsModalOpen(false);
      setNewRoleName('');
      await addRole({ name: newRoleName.toUpperCase() });
      setIsModalOpen(false);
      setNewRoleName('');
      
      const response = await getAllRoles();
      setRoles(response.data);
    } catch (error) {
      console.error('Error creating role:', error);
    }
  };

  const handleUpdateRole = async (): Promise<void> => {
    if (!roleToUpdate) return;
    try {
      const formattedRoleName = newRoleName.replace(/\s+/g, '_').toUpperCase();
      await updateRole(roleToUpdate.id, formattedRoleName);
      setIsUpdateModalOpen(false);
      setNewRoleName('');
      setRoleToUpdate(null);
      const response = await getAllRoles();
      setRoles(response.data);
    } catch (error) {
      console.error('Error updating role:', error);
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
           <th>Actions</th>
         </tr>
         </thead>
         <tbody>
         {roles.map(role => (
          <tr key={role.id}>
            <td>{role.id}</td>
            <td>{role.name}</td>
            <td>
              <button
               onClick={() => {
                 setRoleToUpdate(role);
                 setNewRoleName(role.name);
                 setIsUpdateModalOpen(true);
               }}
              >
                Update
              </button>
            </td>
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

     {isUpdateModalOpen && (
      <div className="modal">
        <div className="modal-content">
          <h2>Update Role</h2>
          <input
           type="text"
           value={newRoleName}
           onChange={e => setNewRoleName(e.target.value)}
           placeholder="Role Name"
          />
          <div className="modal-buttons">
            <button onClick={handleUpdateRole}>Confirm</button>
            <button onClick={() => setIsUpdateModalOpen(false)}>Cancel</button>
          </div>
        </div>
      </div>
     )}
   </div>
    
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
