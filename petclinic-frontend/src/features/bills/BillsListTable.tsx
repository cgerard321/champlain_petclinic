import { useEffect, useState } from 'react';
import { Bill } from '@/features/bills/models/Bill.ts';
import { useUser } from '@/context/UserContext';

export default function BillsListTable(): JSX.Element {
    const { user } = useUser();  // Get user info from context
    const [bills, setBills] = useState<Bill[]>([]);  // Store the bills
    const [error, setError] = useState<string | null>(null);  // Error handling

    useEffect(() => {
        if (!user.userId) return;

        // Fetch bills using the fetch API to handle streaming
        const fetchBills = async () => {
            try {
                const response = await fetch(`http://localhost:8080/api/gateway/bills/customer/1`, {
                    headers: {
                        'Accept': 'text/event-stream',  // Ensure the correct headers for SSE
                    },
                });

                const reader = response.body?.getReader();
                const decoder = new TextDecoder('utf-8');

                let done = false;
                let billsArray: Bill[] = [];

                while (!done) {
                    const { value, done: streamDone } = await reader?.read() || {};
                    done = streamDone || true;

                    if (value) {
                        const chunk = decoder.decode(value, { stream: true });
                        // Process each chunk of data (which contains a new bill)
                        if (chunk) {
                            try {
                                const newBill: Bill = JSON.parse(chunk);  // Parse the chunked JSON string
                                billsArray.push(newBill);  // Add to bills array
                                setBills([...billsArray]);  // Update state with new bill
                            } catch (e) {
                                console.error('Error parsing chunk:', e);
                            }
                        }
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
                    {bills.map((bill) => (
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
            )}
        </div>
    );
}