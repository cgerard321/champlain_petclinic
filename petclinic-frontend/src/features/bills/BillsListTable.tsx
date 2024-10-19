import { useEffect, useState } from 'react';
import { Bill } from '@/features/bills/models/Bill.ts';
import { useUser } from '@/context/UserContext';
import './BillsListTable.css';

export default function BillsListTable(): JSX.Element {
  const { user } = useUser();
  const [bills, setBills] = useState<Bill[]>([]);
  const [filteredBills, setFilteredBills] = useState<Bill[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [selectedStatus, setSelectedStatus] = useState<string>('all'); // Default filter status

  useEffect(() => {
    if (!user.userId) return;

    const fetchBills = async (): Promise<void> => {
      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/customers/${user.userId}/bills`,
          {
            headers: {
              Accept: 'text/event-stream',
            },
            credentials: 'include',
          }
        );

        if (!response.ok) {
          throw new Error(`Error: ${response.status} ${response.statusText}`);
        }

        const reader = response.body?.getReader();
        const decoder = new TextDecoder('utf-8');

        let done = false;
        const billsArray: Bill[] = [];

        while (!done) {
          const { value, done: streamDone } = (await reader?.read()) || {};
          done = streamDone || true;

          if (value) {
            const chunk = decoder.decode(value, { stream: true });
            const formattedChunks = chunk.trim().split(/\n\n/);

            formattedChunks.forEach(formattedChunk => {
              const cleanChunk = formattedChunk.trim().replace(/^data:\s*/, '');

              if (cleanChunk) {
                try {
                  const newBill: Bill = JSON.parse(cleanChunk);
                  billsArray.push(newBill);
                  setBills([...billsArray]);
                } catch (e) {
                  console.error('Error parsing chunk:', e);
                }
              }
            });
          }
        }
      } catch (err) {
        console.error('Error fetching bills:', err);
        setError('Failed to fetch bills');
      }
    };

    fetchBills();
  }, [user.userId]);

  // Filtering bills by selected status
  useEffect(() => {
    if (selectedStatus === 'all') {
      setFilteredBills(bills);
    } else {
      setFilteredBills(
        bills.filter(
          bill => bill.billStatus.toLowerCase() === selectedStatus.toLowerCase()
        )
      );
    }
  }, [selectedStatus, bills]);

  // Function to handle downloading the PDF for a bill
  const handleDownloadPdf = async (
    customerId: string,
    billId: string
  ): Promise<void> => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/v2/gateway/customers/${customerId}/bills/${billId}/pdf`,
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/pdf',
          },
          credentials: 'include',
        }
      );

      if (!response.ok) {
        throw new Error('Failed to download PDF');
      }

      const blob = await response.blob();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `bill-${billId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      console.error('Error downloading PDF:', error);
    }
  };

  return (
    <div>
      {/* Dropdown to filter bills by status */}
      <div>
        <label htmlFor="statusFilter">Filter by Status:</label>
        <select
          id="statusFilter"
          value={selectedStatus}
          onChange={e => setSelectedStatus(e.target.value)}
          style={{ width: '150px' }}
        >
          <option value="all">All</option>
          <option value="overdue">Overdue</option>
          <option value="paid">Paid</option>
          <option value="unpaid">Unpaid</option>
        </select>
      </div>

      {error ? (
        <p>{error}</p>
      ) : (
        <div className="billsListTableContainer">
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
                <th>Time Remaining</th>
                <th>Download PDF</th>
              </tr>
            </thead>
            <tbody>
              {filteredBills.map(bill => (
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
                  <td>
                    {bill.billStatus === 'OVERDUE' ? (
                      <span style={{ color: 'red' }}>Overdue</span>
                    ) : (
                      bill.billStatus
                    )}
                  </td>
                  <td>{bill.dueDate}</td>
                  <td>
                    {bill.billStatus === 'PAID' ? (
                      <span>This bill is paid</span>
                    ) : bill.timeRemaining === 0 ? (
                      <span style={{ color: 'red' }}>
                        0 days remaining to pay bill
                      </span>
                    ) : (
                      `${bill.timeRemaining} days remaining to pay bill`
                    )}
                  </td>
                  <td>
                    <button
                      onClick={() =>
                        handleDownloadPdf(user.userId, bill.billId)
                      }
                    >
                      Download PDF
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
