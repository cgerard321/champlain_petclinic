import { useEffect, useState } from 'react';
import { Bill } from '@/features/bills/models/Bill.ts';
import { useUser } from '@/context/UserContext';
import './BillsListTable.css';

export default function BillsListTable(): JSX.Element {
  const { user } = useUser();
  const [bills, setBills] = useState<Bill[]>([]);
  const [error, setError] = useState<string | null>(null);

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

  return (
    <div>
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
              </tr>
            </thead>
            <tbody>
              {bills.map(bill => (
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
    </div>
  );
}
