import { useEffect, useState } from 'react';
import { Bill } from '@/features/bills/models/Bill.ts';
import { useUser } from '@/context/UserContext';

export default function BillsListTable(): JSX.Element {
    const { user } = useUser();
    const [bills, setBills] = useState<Bill[]>([]);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (!user.userId) return;

        // Use EventSource to handle the Flux stream
        const eventSource = new EventSource(`${user.userId}`);

        eventSource.onmessage = (event) => {
            const newBill: Bill = JSON.parse(event.data);
            setBills((prevBills) => [...prevBills, newBill]); // Append new bills to the state
        };

        eventSource.onerror = (err) => {
            console.error('Error receiving SSE:', err);
            setError('Failed to fetch bills');
            eventSource.close();
        };

        return () => {
            eventSource.close(); // Close the event source when the component unmounts
        };
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