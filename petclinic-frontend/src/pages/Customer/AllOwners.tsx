import { useEffect, useState } from 'react';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel';
import { Bill } from '@/features/bills/models/Bill';
import './AllOwners.css';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import axios from 'axios';

const AllOwners: React.FC = (): JSX.Element => {
  const [owners, setOwners] = useState<OwnerResponseModel[]>([]);
  const [searchId, setSearchId] = useState<string>('');
  const [searchResult, setSearchResult] = useState<OwnerResponseModel | null>(
    null
  );
  const [bills, setBills] = useState<Bill[]>([]);
  const [error, setError] = useState<string | null>(null);

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
            {owners.map(owner => (
              <tr key={owner.ownerId}>
                <td>{owner.ownerId}</td>
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
