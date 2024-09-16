import * as React from 'react';
import { useEffect, useState } from 'react';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel';
import './AllOwners.css';
import { NavBar } from '@/layouts/AppNavBar.tsx';
const AllOwners: React.FC = (): JSX.Element => {
  const [owners, setOwners] = useState<OwnerResponseModel[]>([]);

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

  return (
    <div>
      <NavBar />
      <div className="owners-container">
        <h1>Owners</h1>
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
            {owners.map(owner => (
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
