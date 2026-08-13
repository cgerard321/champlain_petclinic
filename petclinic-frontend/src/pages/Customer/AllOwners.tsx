import { useEffect, useState } from 'react';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel';
import './AllOwners.css';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import { Link } from 'react-router-dom';
import { getAllOwners } from '@/features/customers/api/getAllOwners.ts';
import { deleteOwner } from '@/features/customers/api/deleteOwner';
import { IsVet } from '@/context/UserContext';

const AllOwners: React.FC = (): JSX.Element => {
  interface FilterModel {
    [key: string]: string;
    firstName: string;
    lastName: string;
    address: string;
    city: string;
    province: string;
    telephone: string;
  }

  const isVet = IsVet();
  const [owners, setOwners] = useState<OwnerResponseModel[]>([]);
  const [filter, setFilter] = useState<FilterModel>({
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    province: '',
    telephone: '',
  });

  const [isFilterVisible, setFilterVisible] = useState(true);

  useEffect(() => {
    const getOwners = async (): Promise<void> => {
      const fetchedOwners = await getAllOwners();
      setOwners(fetchedOwners);
    };

    getOwners();
  }, []);

  const handleDelete = async (ownerId: string): Promise<void> => {
    const confirmDelete = window.confirm(
      'Are you sure you want to delete this owner?'
    );

    if (confirmDelete) {
      await deleteOwner(ownerId);
      setOwners(owners.filter(owner => owner.ownerId !== ownerId));
      alert('Owner deleted successfully.');
    } else {
      alert('Owner deletion canceled.');
    }
  };

  function isKeyOfOwnerResponseModel(
    key: string
  ): key is keyof OwnerResponseModel {
    return [
      'firstName',
      'lastName',
      'address',
      'city',
      'province',
      'telephone',
    ].includes(key);
  }

  const filteredOwners = owners.filter(owner => {
    return Object.keys(filter).every(key => {
      if (!filter[key]) return true;
      if (!isKeyOfOwnerResponseModel(key)) return true;
      const ownerValue = owner[key];
      if (ownerValue === undefined || ownerValue === null) return false;
      return ownerValue.toString().includes(filter[key].toString());
    });
  });

  return (
    <div>
      <NavBar />

      <div className="owners-container">
        <h1>Owners</h1>

        {isFilterVisible && (
          <div className="filter-container">
            <button
              className="btn btn-primary filter-toggle"
              onClick={() => setFilterVisible(!isFilterVisible)}
            >
              {isFilterVisible ? 'Hide Filter' : 'Show Filter'}
            </button>
            <input
              type="text"
              placeholder="First Name"
              value={filter.firstName}
              onChange={e =>
                setFilter({ ...filter, firstName: e.target.value })
              }
            />
            <input
              type="text"
              placeholder="Last Name"
              value={filter.lastName}
              onChange={e => setFilter({ ...filter, lastName: e.target.value })}
            />
            <input
              type="text"
              placeholder="Address"
              value={filter.address}
              onChange={e => setFilter({ ...filter, address: e.target.value })}
            />
            <input
              type="text"
              placeholder="City"
              value={filter.city}
              onChange={e => setFilter({ ...filter, city: e.target.value })}
            />
            <input
              type="text"
              placeholder="Province"
              value={filter.province}
              onChange={e => setFilter({ ...filter, province: e.target.value })}
            />
            <input
              type="text"
              placeholder="Telephone"
              value={filter.telephone}
              onChange={e =>
                setFilter({ ...filter, telephone: e.target.value })
              }
            />
          </div>
        )}

        {!isFilterVisible && (
          <button
            className="btn btn-primary filter-toggle"
            onClick={() => setFilterVisible(!isFilterVisible)}
          >
            {isFilterVisible ? 'Hide Filter' : 'Show Filter'}
          </button>
        )}

        <table>
          <thead>
            <tr>
              <th>Owner Id</th>
              <th>First Name</th>
              <th>Last Name</th>
              <th>Address</th>
              <th>City</th>
              <th>Province</th>
              <th>Telephone</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {filteredOwners.map(owner => (
              <tr key={owner.ownerId}>
                <td>
                  <Link to={`/customers/${owner.ownerId}`}>
                    {owner.ownerId}
                  </Link>
                </td>
                <td>{owner.firstName}</td>
                <td>{owner.lastName}</td>
                <td>{owner.address}</td>
                <td>{owner.city}</td>
                <td>{owner.province}</td>
                <td>{owner.telephone}</td>
                <td>
                  {!isVet && (
                    <button
                      className="btn btn-danger"
                      onClick={() => handleDelete(owner.ownerId)}
                      title="Delete"
                      style={{ backgroundColor: 'red', color: 'white' }}
                    >
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="32"
                        height="32"
                        fill="#fff"
                        className="bi bi-trash"
                        viewBox="0 0 16 16"
                      >
                        <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z" />
                        <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h3.5a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4zM2.5 3h11V2h-11z" />
                      </svg>
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AllOwners;
