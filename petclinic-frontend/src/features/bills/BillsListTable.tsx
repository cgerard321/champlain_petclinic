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

  // New state for details modal
  const [detailBill, setDetailBill] = useState<Bill | null>(null);
  const [showDetailModal, setShowDetailModal] = useState<boolean>(false);

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

  // Open details modal for a bill
  const openDetails = (bill: Bill): void => {
    setDetailBill(bill);
    setShowDetailModal(true);
  };

  // Close details modal
  const closeDetails = (): void => {
    setShowDetailModal(false);
    setDetailBill(null);
  };

  // helper to format displayed total due according to selected currency
  const formatTotalDue = (bill: Bill): string => {
    const amount = bill.taxedAmount ?? bill.amount ?? 0;
    if (currency === 'CAD') {
      return `CAD $${amount.toFixed(2)}`;
    }
    return `USD $${convertCurrency(amount, 'CAD', 'USD').toFixed(2)}`;
  };

  return (
    <div>
      {/* Status and Currency dropdowns together */}
      <div className="filterContainer">
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
        <div className="billsListContainer">
          {filteredBills.length === 0 ? (
            <p>No bills to display.</p>
          ) : (
            filteredBills.map(bill => (
              <div
                key={bill.billId}
                className="billCard"
                data-bill-id={bill.billId}
              >
                <div className="billCardContent">
                  <div className="billField vet">
                    <strong>Vet:</strong> {bill.vetFirstName} {bill.vetLastName}
                  </div>

                  <div className="billField date">
                    <strong>Appointment date:</strong> {bill.date}
                  </div>

                  <div className="billField total">
                    <strong>Total due:</strong> {formatTotalDue(bill)}
                  </div>

                  <div className="billField status">
                    <strong>Status:</strong>{' '}
                    <span
                      className={
                        bill.billStatus === 'OVERDUE'
                          ? 'status--overdue'
                          : bill.billStatus === 'PAID'
                            ? 'status--paid'
                            : undefined
                      }
                    >
                      {bill.billStatus === 'OVERDUE'
                        ? 'Overdue'
                        : bill.billStatus}
                    </span>
                  </div>
                </div>

                <div className="billActions">
                  <button
                    className="detailsButton"
                    onClick={() => openDetails(bill)}
                  >
                    Details
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {/* Details Modal */}
      {showDetailModal && detailBill && (
        <div className="modalOverlay">
          <div className="modalContent">
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <h3>Bill Details</h3>
              <button onClick={closeDetails}>Close</button>
            </div>

            <div style={{ marginTop: '12px' }}>
              <p>
                <strong>Bill ID:</strong> {detailBill.billId}
              </p>
              <p>
                <strong>Owner:</strong> {detailBill.ownerFirstName}{' '}
                {detailBill.ownerLastName}
              </p>
              <p>
                <strong>Visit Type:</strong> {detailBill.visitType}
              </p>
              <p>
                <strong>Vet:</strong> {detailBill.vetFirstName}{' '}
                {detailBill.vetLastName}
              </p>
              <p>
                <strong>Date:</strong> {detailBill.date}
              </p>
              <p>
                <strong>Amount:</strong>{' '}
                {currency === 'CAD'
                  ? `CAD $${detailBill.amount.toFixed(2)}`
                  : `USD $${convertCurrency(detailBill.amount, 'CAD', 'USD').toFixed(2)}`}
              </p>
              <p>
                <strong>Interest:</strong>{' '}
                {currency === 'CAD'
                  ? `CAD $${(detailBill.interest || 0).toFixed(2)}`
                  : `USD $${convertCurrency(detailBill.interest || 0, 'CAD', 'USD').toFixed(2)}`}
              </p>
              <p>
                <strong>Total Due:</strong>{' '}
                {currency === 'CAD'
                  ? `CAD $${detailBill.taxedAmount.toFixed(2)}`
                  : `USD $${convertCurrency(detailBill.taxedAmount, 'CAD', 'USD').toFixed(2)}`}
              </p>
              <p>
                <strong>Status:</strong> {detailBill.billStatus}
              </p>
              <p>
                <strong>Due Date:</strong> {detailBill.dueDate}
              </p>
              <p>
                <strong>Time Remaining:</strong>{' '}
                {detailBill.billStatus === 'PAID' ? (
                  <span style={{ color: 'green' }}>This bill is paid</span>
                ) : detailBill.timeRemaining === 0 ? (
                  <span style={{ color: 'red' }}>
                    0 days remaining to pay bill
                  </span>
                ) : (
                  `${detailBill.timeRemaining} days remaining to pay bill`
                )}
              </p>
            </div>

            <div style={{ display: 'flex', gap: '12px', marginTop: '16px' }}>
              <button
                onClick={() =>
                  handleDownloadPdf(user.userId, detailBill.billId)
                }
              >
                Download PDF
              </button>
              {detailBill.billStatus !== 'PAID' && (
                <button
                  onClick={() => {
                    handlePayBillClick(detailBill);
                    closeDetails();
                  }}
                >
                  Pay Bill
                </button>
              )}
            </div>
          </div>
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
