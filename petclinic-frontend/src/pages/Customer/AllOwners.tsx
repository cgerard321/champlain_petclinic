import { useEffect, useState } from 'react';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel';
import { Bill } from '@/features/bills/models/Bill';
import './AllOwners.css';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import axios from 'axios';

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
  const [searchId, setSearchId] = useState<string>('');
  const [searchResult, setSearchResult] = useState<OwnerResponseModel | null>(
    null
  );
  const [bills, setBills] = useState<Bill[]>([]);
  const [error, setError] = useState<string | null>(null);

  const [filter, setFilter] = useState<FilterModel>({
    firstName: '',
    lastName: '',
    address: '',
    city: '',
    province: '',
    telephone: '',
  });
  useEffect(() => {
    const eventSource = new EventSource(
      'http://localhost:8080/api/v2/gateway/owners',
      {
        withCredentials: true,
      }
    );

    eventSource.onmessage = event => {
      try {
        const parsedData: OwnerResponseModel = JSON.parse(event.data);
        setOwners(prevOwners => [...prevOwners, parsedData]);
      } catch (error) {
        console.error('Error parsing event data:', error);
      }
    };

    eventSource.onerror = error => {
      console.error('EventSource error:', error);
      eventSource.close();
    };

    return () => {
      eventSource.close();
    };
  }, []);

  interface AxiosErrorResponse {
    response?: {
      data?: {
        message?: string;
      };
      statusText?: string;
    };
  }

  const handleSearch = async (): Promise<void> => {
    if (!searchId) {
      setError('Please enter an owner ID.');
      return;
    }



    try {
      const ownerResponse = await axios.get(
        `http://localhost:8080/api/v2/gateway/owners/${searchId}`,
        {
          withCredentials: true,
        }
      );
      setSearchResult(ownerResponse.data);

      const billsResponse = await axios.get(
        `http://localhost:8080/api/v2/gateway/bills/customer/${searchId}`,
        {
          withCredentials: true,
        }
      );

      const billsData: Bill[] = [];
      const data = billsResponse.data;

      if (typeof data === 'string') {
        const pieces = data.split('\n').filter(Boolean);
        for (const piece of pieces) {
          if (piece.startsWith('data:')) {
            const billData = piece.slice(5).trim();
            try {
              const bill: Bill = JSON.parse(billData);
              billsData.push(bill);
            } catch (error) {
              console.error('Error parsing bill data:', error);
            }
          }
        }
      } else if (Array.isArray(data)) {
        billsData.push(...data);
      } else {
        console.error('Unexpected bills response format:', data);
      }

      setBills(billsData);
      setError(null);
    } catch (err) {
      const axiosError = err as AxiosErrorResponse;
      const errorMessage =
        axiosError.response?.data?.message ||
        axiosError.response?.statusText ||
        'An error occurred.';

      console.error('Error fetching data:', err);
      setError(`Error: ${errorMessage}`);
      setSearchResult(null);
      setBills([]);
    }
  };

  const handleDelete = async (ownerId: string) => {

    const confirmDelete = window.confirm("Are you sure you want to delete this owner?");


    if (confirmDelete) {
      try {

        await axios.delete(`http://localhost:8080/api/v2/gateway/owners/${ownerId}`, {
          withCredentials: true,
        });


        setOwners(owners.filter(owner => owner.ownerId !== ownerId));
        alert('Owner deleted successfully.');
      } catch (error) {
        console.error('Error deleting owner:', error);
        alert('Error deleting owner. Please try again.');
      }
    } else {

      alert('Owner deletion canceled.');
    }
  };



  const calculateAge = (birthDate: Date): number => {
    const birth = new Date(birthDate);
    const ageDiffMs = Date.now() - birth.getTime();
    const ageDate = new Date(ageDiffMs);
    return Math.abs(ageDate.getUTCFullYear() - 1970);
  };

  const petTypeMapping: { [key: string]: string } = {
    '1': 'Cat',
    '2': 'Dog',
    '3': 'Lizard',
    '4': 'Snake',
    '5': 'Bird',
    '6': 'Hamster',
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
        <h1>Search for owner</h1>
        {/* Search Bar */}
        <div className="search-bar">
          <input
            type="text"
            placeholder="Enter Owner ID"
            value={searchId}
            onChange={e => setSearchId(e.target.value)}
          />
          <button type="button" onClick={handleSearch}>
            Search
          </button>
        </div>

        {/* Result/error */}
        {error && <p className="error">{error}</p>}
        {searchResult && (
          <div className="search-result">
            <h3>Owner</h3>
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
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>{searchResult.ownerId}</td>
                  <td>{searchResult.firstName}</td>
                  <td>{searchResult.lastName}</td>
                  <td>{searchResult.address}</td>
                  <td>{searchResult.city}</td>
                  <td>{searchResult.province}</td>
                  <td>{searchResult.telephone}</td>
                </tr>
              </tbody>
            </table>

            {/* Pets */}
            {searchResult.pets && searchResult.pets.length > 0 && (
              <div className="pets-container">
                <h3>Pets</h3>
                <table>
                  <thead>
                    <tr>
                      <th>Pet Id</th>
                      <th>Name</th>
                      <th>Species</th>
                      <th>Age</th>
                      <th>Weight</th>
                    </tr>
                  </thead>
                  <tbody>
                    {searchResult.pets.map(pet => (
                      <tr key={pet.petId}>
                        <td>{pet.petId}</td>
                        <td>{pet.name}</td>
                        <td>{petTypeMapping[pet.petTypeId] || 'Unknown'}</td>
                        <td>{calculateAge(pet.birthDate)}</td>
                        <td>{pet.weight}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {/* Bills */}
            {bills.length > 0 && (
              <div className="bills-container">
                <h3>Associated Bills</h3>
                <table>
                  <thead>
                    <tr>
                      <th>Bill Id</th>
                      <th>Visit Type</th>
                      <th>Vet Id</th>
                      <th>Date</th>
                      <th>Amount</th>
                      <th>Taxed Amount</th>
                      <th>Bill Status</th>
                      <th>Due Date</th>
                    </tr>
                  </thead>
                  <tbody>
                    {bills.map(bill => (
                      <tr key={bill.billId}>
                        <td>{bill.billId}</td>
                        <td>{bill.visitType}</td>
                        <td>{bill.vetId}</td>
                        <td>{bill.date}</td>
                        <td>{bill.amount.toFixed(2)}</td>
                        <td>{bill.taxedAmount.toFixed(2)}</td>
                        <td>{bill.billStatus}</td>
                        <td>{bill.dueDate}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
      </div>
      <div className="owners-container">
        <h1>Owners</h1>

        {/* All Owners */}

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
              <th>Owner Id</th>
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
                <td>{owner.ownerId}</td>
                <td>{owner.firstName}</td>
                <td>{owner.lastName}</td>
                <td>{owner.address}</td>
                <td>{owner.city}</td>
                <td>{owner.province}</td>
                <td>{owner.telephone}</td>
                <td>
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
                      <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4zM2.5 3h11V2h-11z" />
                    </svg>
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

export default AllOwners;
