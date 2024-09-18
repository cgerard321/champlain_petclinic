import * as React from 'react';
import { useEffect, useState } from 'react';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel';
import './AllOwners.css';
import { NavBar } from '@/layouts/AppNavBar.tsx';
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

  const [owners, setOwners] = useState<OwnerResponseModel[]>([]);

  const [filter, setFilter] = useState<FilterModel>({
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    province: '',
    telephone: '',
  });
  useEffect(() => {
    // Now handle real-time updates with EventSource
    // Listen for updates in the event stream

    const eventSource = new EventSource(
      'http://localhost:8080/api/v2/gateway/owners',

      {
        withCredentials: true,
      }
    );
    eventSource.onmessage = event => {
      try {
        const parsedData: OwnerResponseModel = JSON.parse(event.data);
        setOwners(prevOwners => [...prevOwners, parsedData]); // Add new owner to state
      } catch (error) {
        console.error('Error parsing event data:', error);
      }
    };

    eventSource.onerror = error => {
      console.error('EventSource error:', error);
      eventSource.close(); // Close the connection on error
    };

    // Clean up when the component unmounts
    return () => {
      eventSource.close();
    };
  }, []);

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
  // Then in your filter function
  const filteredOwners = owners.filter(owner => {
    return Object.keys(filter).every(key => {
      if (!filter[key]) return true; // If filter is empty, pass all
      if (!isKeyOfOwnerResponseModel(key)) return true; // If key is not in OwnerResponseModel, pass
      return owner[key].toString().includes(filter[key].toString());
    });
  });
  return (
    <div>
      <NavBar />
      <div className="owners-container">
        <h1>Owners</h1>
        <div>
          <h2>Filter</h2>
          <input
            type="text"
            placeholder="First Name"
            value={filter.firstName}
            onChange={e => setFilter({ ...filter, firstName: e.target.value })}
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
            onChange={e => setFilter({ ...filter, telephone: e.target.value })}
          />
        </div>
        <table>
          <thead>
            <tr>
              <th>First Name</th>
              <th>Last Name</th>
              <th>Address</th>
              <th>City</th>
              <th>Province</th>
              <th>Telephone</th>
            </tr>
          </thead>
          <tbody>
            {filteredOwners.map(owner => (
              <tr key={owner.ownerId}>
                <td>{owner.firstName}</td>
                <td>{owner.lastName}</td>
                <td>{owner.address}</td>
                <td>{owner.city}</td>
                <td>{owner.province}</td>
                <td>{owner.telephone}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default AllOwners;
