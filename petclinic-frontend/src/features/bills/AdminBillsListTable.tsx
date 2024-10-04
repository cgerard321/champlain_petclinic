import { useEffect, useState } from 'react';
import { Bill } from '@/features/bills/models/Bill.ts';
import { getAllBills } from '@/features/bills/api/getAllBills.tsx';
import { getAllPaidBills } from '@/features/bills/api/getAllPaidBills.tsx';
import { getAllUnpaidBills } from '@/features/bills/api/getAllUnpaidBills.tsx';
import { getAllOverdueBills } from '@/features/bills/api/getAllOverdueBills.tsx';
import { getBillByBillId } from '@/features/bills/api/getBillByBillId.tsx';

export default function AdminBillsListTable(): JSX.Element {
  const [bills, setBills] = useState<Bill[]>([]);
  const [searchId, setSearchId] = useState<string>('');
  const [searchedBill, setSearchedBill] = useState<Bill | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [selectedStatus, setSelectedStatus] = useState<string>('...');

  const fetchBills = async (): Promise<void> => {
    let allBills;
    switch (selectedStatus) {
      case 'Paid':
        allBills = await getAllPaidBills();
        break;
      case 'Unpaid':
        allBills = await getAllUnpaidBills();
        break;
      case 'Overdue':
        allBills = await getAllOverdueBills();
        break;
      default:
        allBills = await getAllBills();
        break;
    }
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
  };

  const handleStatusChange = (e: React.ChangeEvent<HTMLSelectElement>): void => {
    setSelectedStatus(e.target.value);
  };

  useEffect(() => {
    fetchBills();
  }, [selectedStatus]);

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
                <strong>Owner Name:</strong> {searchedBill.ownerFirstName} {searchedBill.ownerLastName}
              </p>
              <p>
                <strong>Visit Type:</strong> {searchedBill.visitType}
              </p>
              <p>
                <strong>Vet Name:</strong> {searchedBill.vetFirstName} {searchedBill.vetLastName}
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
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h3>All Bills:</h3>
                <div style={{ display: 'flex', alignItems: 'center' }}>
                  <label htmlFor="statusFilter" style={{ marginRight: '10px' }}>Filter by status:</label>
                  <select id="statusFilter" value={selectedStatus} onChange={handleStatusChange}>
                    <option value="...">...</option>
                    <option value="Paid">Paid</option>
                    <option value="Unpaid">Unpaid</option>
                    <option value="Overdue">Overdue</option>
                  </select>
                </div>
              </div>

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
                          <td>{bill.ownerFirstName} {bill.ownerLastName}</td>
                          <td>{bill.visitType}</td>
                          <td>{bill.vetFirstName} {bill.vetLastName}</td>
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
      </div>
  );
}
