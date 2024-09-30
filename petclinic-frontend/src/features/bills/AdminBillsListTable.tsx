import { useEffect, useState } from 'react';
import { Bill } from '@/features/bills/models/Bill.ts';
import { getAllBills } from '@/features/bills/api/getAllBills.tsx';
import { getBillByBillId } from '@/features/bills/api/GetBillByBillId.tsx';
import { getAllOwners } from '../customers/api/getAllOwners';
import { getAllVets } from '../veterinarians/api/getAllVets';
import { BillRequestModel } from './models/BillRequestModel';
import { addBill } from './api/addBill';
import { OwnerResponseModel } from '../customers/models/OwnerResponseModel';
import { VetResponseModel } from '../veterinarians/models/VetResponseModel';

export default function AdminBillsListTable(): JSX.Element {
  const [bills, setBills] = useState<Bill[]>([]);
  const [searchId, setSearchId] = useState<string>('');
  const [searchedBill, setSearchedBill] = useState<Bill | null>(null);
  const [error, setError] = useState<string | null>(null);

  const [showCreateForm, setCreateForm] = useState<boolean>(false);
  const [newBill, setNewBill] = useState<BillRequestModel>({
    customerId: '',
    vetId: '',
    visitType: '',
    date: '',
    amount: 0,
    billStatus: '',
    dueDate: '',
  });
  const [owners, setOwners] = useState<OwnerResponseModel[]>([]);
  const [vets, setVets] = useState<VetResponseModel[]>([]);

  const fetchBills = async (): Promise<void> => {
    const allBills = await getAllBills();
    setBills(allBills);
  };

  const fetchOwnersAndVets = async (): Promise<void> => {
    const ownersList = await getAllOwners();
    const vetsList = await getAllVets();
    setOwners(ownersList);
    setVets(vetsList);
  };

  const handleCreateBill = async (): Promise<void> => {
    try {
      await addBill(newBill);
      setCreateForm(false);
      fetchBills();
    } catch (err) {
      console.error('Error creating bill:', err);
      setError('Failed to create bill. Please try again.');
    }
  };

  const handleSearch = async (): Promise<void> => {
    setError(null);
    if (searchId) {
      try {
        const bill = await getBillByBillId(searchId);
        if (bill) {
          setSearchedBill(bill);
        } else {
          throw new Error('Bill not found');
        }
      } catch (err) {
        setError('Invalid Bill ID. Please try again.');
        setSearchedBill(null);
      }
    }
  };

  const handleGoBack = (): void => {
    setSearchedBill(null);
    setSearchId('');
    setError(null);
  };

  useEffect(() => {
    fetchBills();
    fetchOwnersAndVets();
  }, []);

  return (
    <div>
      <div>
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <input
          type="text"
          placeholder="Enter Bill ID"
          value={searchId}
          onChange={e => setSearchId(e.target.value)}
        />
        <button onClick={handleSearch}>Search</button>
        {searchedBill && <button onClick={handleGoBack}>Go Back</button>}
      </div>

      {/* Create Bill Form */}
      <button onClick={() => setCreateForm(!showCreateForm)}>
        {showCreateForm ? 'Cancel' : 'Create New Bill'}
      </button>

      {searchedBill ? (
        <div>
          <h3>Searched Bill Details:</h3>
          <p>
            <strong>Bill ID:</strong> {searchedBill.billId}
          </p>
          <p>
            <strong>Owner Name:</strong> {searchedBill.ownerFirstName}{' '}
            {searchedBill.ownerLastName}
          </p>
          <p>
            <strong>Visit Type:</strong> {searchedBill.visitType}
          </p>
          <p>
            <strong>Vet Name:</strong> {searchedBill.vetFirstName}{' '}
            {searchedBill.vetLastName}
          </p>
          <p>
            <strong>Date:</strong> {searchedBill.date}
          </p>
          <p>
            <strong>Amount:</strong> {searchedBill.amount}
          </p>
          <p>
            <strong>Taxed Amount:</strong> {searchedBill.taxedAmount}
          </p>
          <p>
            <strong>Status:</strong> {searchedBill.billStatus}
          </p>
          <p>
            <strong>Due Date:</strong> {searchedBill.dueDate}
          </p>
        </div>
      ) : (
        <div>
          <h3>All Bills:</h3>
          <table className="table table-striped">
            <thead>
              <tr>
                <th>Bill ID</th>
                <th>Owner Name</th>
                <th>Visit Type</th>
                <th>Vet Name</th>
                <th>Date</th>
                <th>Amount</th>
                <th>Taxed Amount</th>
                <th>Status</th>
                <th>Due Date</th>
              </tr>
            </thead>
            <tbody>
              {bills
                .filter(data => data != null)
                .map((bill: Bill) => (
                  <tr key={bill.billId}>
                    <td>{bill.billId}</td>
                    <td>
                      {bill.ownerFirstName} {bill.ownerLastName}
                    </td>
                    <td>{bill.visitType}</td>
                    <td>
                      {bill.vetFirstName} {bill.vetLastName}
                    </td>
                    <td>{bill.date}</td>
                    <td>{bill.amount}</td>
                    <td>{bill.taxedAmount}</td>
                    <td>{bill.billStatus}</td>
                    <td>{bill.dueDate}</td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      )}

      {showCreateForm && (
        <div>
          <h3>Create New Bill</h3>
          <form
            onSubmit={e => {
              e.preventDefault();
              handleCreateBill();
            }}
          >
            <div>
              <label>Customer</label>
              <select
                value={newBill.customerId}
                onChange={e =>
                  setNewBill({ ...newBill, customerId: e.target.value })
                }
              >
                <option value="">Select Customer</option>
                {owners.map(owner => (
                  <option key={owner.ownerId} value={owner.ownerId}>
                    {owner.firstName} {owner.lastName}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label>Vet</label>
              <select
                value={newBill.vetId}
                onChange={e =>
                  setNewBill({ ...newBill, vetId: e.target.value })
                }
              >
                <option value="">Select Vet</option>
                {vets.map(vet => (
                  <option key={vet.vetId} value={vet.vetId}>
                    {vet.firstName} {vet.lastName}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label>Visit Type</label>
              <input
                type="text"
                value={newBill.visitType}
                onChange={e =>
                  setNewBill({ ...newBill, visitType: e.target.value })
                }
              />
            </div>

            <div>
              <label>Date</label>
              <input
                type="date"
                value={newBill.date}
                onChange={e => setNewBill({ ...newBill, date: e.target.value })}
              />
            </div>

            <div>
              <label>Amount</label>
              <input
                type="number"
                value={newBill.amount}
                onChange={e =>
                  setNewBill({ ...newBill, amount: parseFloat(e.target.value) })
                }
              />
            </div>

            <div>
              <label>Status</label>
              <input
                type="text"
                value={newBill.billStatus}
                onChange={e =>
                  setNewBill({ ...newBill, billStatus: e.target.value })
                }
              />
            </div>

            <div>
              <label>Due Date</label>
              <input
                type="date"
                value={newBill.dueDate}
                onChange={e =>
                  setNewBill({ ...newBill, dueDate: e.target.value })
                }
              />
            </div>

            <button type="submit">Create Bill</button>
          </form>
        </div>
      )}
    </div>
  );
}
