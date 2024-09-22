import React, { useEffect, useState } from 'react';

interface BillResponseDTO {
    billId: string;
    customerId: string;
    amount: number;
    billStatus: string; // Paid, Unpaid, Overdue
    dueDate: string;
}

const CustomerBillsHistory: React.FC = () => {
    const [bills, setBills] = useState<BillResponseDTO[]>([]);
    const [filter, setFilter] = useState<string>('All');
    const customerId = '123';

    // Fetch bills from API
    useEffect(() => {
        fetch(`/api/v2/gateway/customers/${customerId}/bills`)
            .then(response => response.json())
            .then(data => setBills(data))
            .catch(error => console.error('Error fetching bills:', error));
    }, [customerId]);

    // Filtering bills based on status
    const filteredBills = bills.filter(bill =>
        filter === 'All' ? true : bill.billStatus === filter
    );

    return (
        <div>
            <h2>Customer Bills History</h2>

            <div>
                <label htmlFor="filter">Filter by Status: </label>
                <select id="filter" value={filter} onChange={e => setFilter(e.target.value)}>
                    <option value="All">All</option>
                    <option value="Paid">Paid</option>
                    <option value="Unpaid">Unpaid</option>
                    <option value="Overdue">Overdue</option>
                </select>
            </div>

            <table>
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
                        <td>${bill.amount.toFixed(2)}</td>
                        <td>{bill.billStatus}</td>
                        <td>{new Date(bill.dueDate).toLocaleDateString()}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
};

export default CustomerBillsHistory;
