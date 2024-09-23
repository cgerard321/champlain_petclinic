import { useEffect, useState } from 'react';
import { Bill } from '@/features/bills/models/Bill.ts';
import { useUser } from '@/context/UserContext';

export default function CustomerBillsHistory(): JSX.Element {
  const { user } = useUser();
  const [bills, setBills] = useState<Bill[]>([]);
  const [filteredBills, setFilteredBills] = useState<Bill[]>([]); // Filtered bills to display
  const [error, setError] = useState<string | null>(null);
  const [selectedStatus, setSelectedStatus] = useState<string>('all'); // Default filter status

  useEffect(() => {
    if (!user.userId) return;

    const fetchBills = async (): Promise<void> => {
      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/bills/customer/${user.userId}`,
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

  // Filter bills by selected status
  useEffect(() => {
    if (selectedStatus === 'all') {
      setFilteredBills(bills); // Show all bills if "All" is selected
    } else {
      setFilteredBills(
        bills.filter(
          bill => bill.billStatus.toLowerCase() === selectedStatus.toLowerCase()
        )
      );
    }
  }, [selectedStatus, bills]);

  return (
    <div>
      <h1>Customer Bills History</h1>

      {/* Dropdown to filter bills by status */}
      <div>
        <label htmlFor="statusFilter">Filter by Status:</label>
        <select
          id="statusFilter"
          value={selectedStatus}
          onChange={e => setSelectedStatus(e.target.value)}
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
        <table className="table table-striped">
          <thead>
            <tr>
              <th>Bill ID</th>
              <th>Amount</th>
              <th>Status</th>
              <th>Due Date</th>
            </tr>
          </thead>
          <tbody>
            {filteredBills.map(bill => (
              <tr key={bill.billId}>
                <td>{bill.billId}</td>
                <td>{bill.amount}</td>
                <td>{bill.billStatus}</td>
                <td>{bill.dueDate}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
