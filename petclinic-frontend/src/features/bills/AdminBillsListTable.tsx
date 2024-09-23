import { useEffect, useState } from 'react';
import { Bill } from '@/features/bills/models/Bill.ts';
import { getAllBills } from '@/features/bills/api/getAllBills.tsx';
import { getBillByBillId } from '@/features/bills/api/GetBillByBillId.tsx';
import { deleteBill } from '@/features/bills/api/deleteBill.tsx';
import './AdminBillsListTable.css';

export default function AdminBillsListTable(): JSX.Element {
  const [bills, setBills] = useState<Bill[]>([]);
  const [searchId, setSearchId] = useState<string>('');
  const [searchedBill, setSearchedBill] = useState<Bill | null>(null);
  const [error, setError] = useState<string | null>(null);

  const fetchBills = async (): Promise<void> => {
    const allBills = await getAllBills();
    setBills(allBills);
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
    getAllBills().then(r => setBills(r));
    try {
      const fetchedBills = await getAllBills();
      setBills(fetchedBills);
    } catch (error) {
      console.error('Error fetching bills:', error);
    }
  };

  const handleDelete = async (billId: string): Promise<void> => {
    const confirmDelete = window.confirm(
      'Are you sure you want to delete this bill?'
    );
    if (!confirmDelete) {
      return;
    }

    const billToDelete = bills.find(bill => bill.billId === billId);
    if (!billToDelete) {
      console.error('Bill not found:', billId);
      window.alert('Bill not found');
      return;
    }

    const response = await deleteBill(billToDelete);

    if (response && (response.status === 200 || response.status === 204)) {
      setBills(bills.filter(bill => bill.billId !== billId));
      window.alert(`Bill (${billId}) has been deleted successfully`);
    } else {
      console.error('Unexpected response status:', response?.status);
      window.alert('Cannot delete this bill. It may be unpaid or overdue.');
    }
  };

  useEffect(() => {
    fetchBills();
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
        <div className="container">
          <h3>All Bills:</h3>
    <div className="container">
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
            <th>Actions</th>
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
                <td>
                  <button
                    className="btn btn-danger"
                    onClick={() => handleDelete(bill.billId)}
                    title="delete"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="32"
                      height="32"
                      fill="currentColor"
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
      )}
    </div>
  );
}
