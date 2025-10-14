import { useEffect, useState, useCallback } from 'react';
import { Bill } from '@/features/bills/models/Bill.ts';
import { useUser } from '@/context/UserContext';
import PaymentForm from './PaymentForm';
import './BillsListTable.css';
import axiosInstance from '@/shared/api/axiosInstance';
import { Currency, convertCurrency } from './utils/convertCurrency';

interface BillsListTableProps {
  currency: Currency;
  setCurrency: (c: Currency) => void;
}

export default function BillsListTable({
  currency,
  setCurrency,
}: BillsListTableProps): JSX.Element {
  const { user } = useUser();
  const [bills, setBills] = useState<Bill[]>([]);
  const [filteredBills, setFilteredBills] = useState<Bill[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [selectedStatus, setSelectedStatus] = useState<string>('all');
  const [selectedBill, setSelectedBill] = useState<Bill | null>(null);
  const [showPaymentForm, setShowPaymentForm] = useState<boolean>(false);

  const fetchBills = useCallback(async (): Promise<void> => {
    if (!user.userId) return;

    try {
      const response = await axiosInstance.get(
        `/customers/${user.userId}/bills`,
        {
          headers: {
            Accept: 'application/json',
          },
          useV2: true,
        }
      );

      if (response.status < 200 || response.status >= 300) {
        throw new Error(`Error: ${response.status} ${response.statusText}`);
      }

      let billsData: Bill[] = [];
      if (Array.isArray(response.data)) {
        billsData = response.data;
      } else if (response.data && typeof response.data === 'object') {
        billsData = [response.data];
      }
      setBills(billsData);
    } catch (err) {
      console.error('Error fetching bills:', err);
      setError('Failed to fetch bills');
    }
  }, [user.userId]);

  useEffect(() => {
    fetchBills();
  }, [fetchBills]);

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
      const response = await axiosInstance.get(
        `/customers/${customerId}/bills/${billId}/pdf?currency=${currency}`,
        {
          responseType: 'blob',
          headers: {
            'Content-Type': 'application/pdf',
          },
          useV2: true,
        }
      );

      if (!response || response.status !== 200 || !response.data) {
        throw new Error('Failed to download PDF');
      }

      const blob = new Blob([response.data], { type: 'application/pdf' });
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

  const handlePayBillClick = (bill: Bill): void => {
    setSelectedBill(bill);
    setShowPaymentForm(true);
  };

  const handlePaymentSuccess = (): void => {
    setShowPaymentForm(false);

    // Update bill status to PAID
    if (selectedBill) {
      setBills(prevBills => {
        const updatedBills = prevBills.map(bill =>
          bill.billId === selectedBill.billId
            ? { ...bill, billStatus: 'PAID' }
            : bill
        );

        // Update filtered bills to reflect the change immediately
        setFilteredBills(
          updatedBills.filter(bill => {
            if (selectedStatus === 'all') return true;
            return (
              bill.billStatus.toLowerCase() === selectedStatus.toLowerCase()
            );
          })
        );

        return updatedBills;
      });
    }

    setSelectedBill(null);

    // Trigger a refresh of the current balance and bills data
    // to ensure everything is in sync
    setTimeout(() => {
      fetchBills();
      // Notify CurrentBalance component to refresh
      window.dispatchEvent(new CustomEvent('paymentSuccess'));
    }, 100);
  };

  const handlePaymentCancel = (): void => {
    setShowPaymentForm(false);
    setSelectedBill(null);
  };

  return (
    <div>
      {/* Status and Currency dropdowns together */}
      <div
        className="filterContainer"
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '16px',
        }}
      >
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
        <label htmlFor="currencyFilter" style={{ marginLeft: '8px' }}>
          Currency:
        </label>
        <select
          id="currencyFilter"
          value={currency}
          onChange={e => setCurrency(e.target.value as Currency)}
          style={{ width: '100px' }}
        >
          <option value="CAD">CAD</option>
          <option value="USD">USD</option>
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
                <th>Interest</th>
                <th>Total Due</th>
                <th>Status</th>
                <th>Due Date</th>
                <th>Time Remaining</th>
                <th>Download PDF</th>
                <th>Pay Bill</th>
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
                  <td>
                    {currency === 'CAD'
                      ? `CAD $${bill.amount.toFixed(2)}`
                      : `USD $${convertCurrency(bill.amount, 'CAD', 'USD').toFixed(2)}`}
                  </td>
                  <td>
                    {currency === 'CAD'
                      ? `CAD $${(bill.interest || 0).toFixed(2)}`
                      : `USD $${convertCurrency(bill.interest || 0, 'CAD', 'USD').toFixed(2)}`}
                  </td>
                  <td>
                    {currency === 'CAD'
                      ? `CAD $${bill.taxedAmount.toFixed(2)}`
                      : `USD $${convertCurrency(bill.taxedAmount, 'CAD', 'USD').toFixed(2)}`}
                  </td>
                  <td>
                    {bill.billStatus === 'OVERDUE' ? (
                      <span style={{ color: 'red' }}>Overdue</span>
                    ) : bill.billStatus === 'PAID' ? (
                      <span style={{ color: 'green' }}>{bill.billStatus}</span>
                    ) : (
                      bill.billStatus
                    )}
                  </td>
                  <td>{bill.dueDate}</td>
                  <td>
                    {bill.billStatus === 'PAID' ? (
                      <span style={{ color: 'green' }}>This bill is paid</span>
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
                  <td>
                    {bill.billStatus !== 'PAID' && (
                      <button onClick={() => handlePayBillClick(bill)}>
                        Pay Bill
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showPaymentForm && selectedBill && (
        <PaymentForm
          billId={selectedBill.billId}
          customerId={user.userId}
          billAmount={selectedBill.taxedAmount}
          baseAmount={selectedBill.amount}
          interestAmount={selectedBill.interest || 0}
          onPaymentSuccess={handlePaymentSuccess}
          onCancel={handlePaymentCancel}
        />
      )}
    </div>
  );
}
