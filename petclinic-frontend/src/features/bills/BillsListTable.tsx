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
  const [activeSection, setActiveSection] = useState<
    'status' | 'amount' | 'date' | null
  >(null);

  const [amountRangeOption, setAmountRangeOption] = useState<string>('none');
  const [customMin, setCustomMin] = useState<string>('');
  const [customMax, setCustomMax] = useState<string>('');

  const [dateMode, setDateMode] = useState<'due' | 'visit'>('due');
  const [dateMonth, setDateMonth] = useState<number | 'any'>(
    new Date().getMonth() + 1
  );
  const [dateYear, setDateYear] = useState<number | 'any'>(
    new Date().getFullYear()
  );

  const [selectedBill, setSelectedBill] = useState<Bill | null>(null);
  const [showPaymentForm, setShowPaymentForm] = useState<boolean>(false);
  const [detailBill, setDetailBill] = useState<Bill | null>(null);
  const [showDetailModal, setShowDetailModal] = useState<boolean>(false);

  const fetchBills = useCallback(async (): Promise<void> => {
    if (!user?.userId) return;
    try {
      const response = await axiosInstance.get(
        `/customers/${user.userId}/bills`,
        { headers: { Accept: 'application/json' }, useV2: true }
      );
      let billsData: Bill[] = [];
      if (Array.isArray(response.data)) billsData = response.data;
      else if (response.data && typeof response.data === 'object')
        billsData = [response.data];
      setBills(billsData);
      setFilteredBills(billsData);
    } catch (err) {
      console.error('Error fetching bills:', err);
      setError('Failed to fetch bills');
    }
  }, [user?.userId]);

  useEffect(() => {
    void fetchBills();
  }, [fetchBills]);

  useEffect(() => {
    if (selectedStatus === 'all') {
      setFilteredBills(bills);
      return;
    }
    setFilteredBills(
      bills.filter(
        b => (b.billStatus || '').toLowerCase() === selectedStatus.toLowerCase()
      )
    );
  }, [selectedStatus, bills]);

  const toggleSection = (section: 'status' | 'amount' | 'date'): void => {
    setActiveSection(prev => {
      const closing = prev === section;
      if (closing) {
        setSelectedStatus('all');
        setAmountRangeOption('none');
        setCustomMin('');
        setCustomMax('');
        setDateMonth(new Date().getMonth() + 1);
        setDateYear(new Date().getFullYear());
        setFilteredBills(bills.slice());
        setError(null);
        return null;
      }
      return section;
    });
  };

  const applyAmountFilter = (): void => {
    let base = bills.slice();
    if (selectedStatus !== 'all')
      base = base.filter(
        b => (b.billStatus || '').toLowerCase() === selectedStatus.toLowerCase()
      );
    if (amountRangeOption === 'none') {
      setFilteredBills(base);
      return;
    }
    if (amountRangeOption === 'custom') {
      const min = Number(customMin);
      const max = Number(customMax);
      if (isNaN(min) || isNaN(max)) {
        setError('Min and max must be numbers');
        return;
      }
      base = base.filter(b => (b.amount ?? 0) >= min && (b.amount ?? 0) <= max);
      setFilteredBills(base);
      return;
    }
    if (amountRangeOption.startsWith('0-')) {
      const max = Number(amountRangeOption.split('-')[1]);
      if (isNaN(max)) {
        setError('Invalid amount option');
        return;
      }
      base = base.filter(b => (b.amount ?? 0) <= max);
      setFilteredBills(base);
      return;
    }
    setFilteredBills(base);
  };

  const applyDateFilter = (): void => {
    let base = bills.slice();
    if (selectedStatus !== 'all')
      base = base.filter(
        b => (b.billStatus || '').toLowerCase() === selectedStatus.toLowerCase()
      );

    if (dateMonth === 'any' && dateYear === 'any') {
      setFilteredBills(base);
      return;
    }

    if (dateMonth === 'any' && typeof dateYear === 'number') {
      const start = new Date(dateYear, 0, 1);
      start.setDate(start.getDate() - 1);
      const end = new Date(dateYear, 11, 31);
      end.setDate(end.getDate() + 1);
      base = base.filter(b => {
        const dateStr = dateMode === 'due' ? b.dueDate : b.date;
        if (!dateStr) return false;
        const d = new Date(dateStr);
        return d >= start && d <= end;
      });
      setFilteredBills(base);
      return;
    }

    if (dateYear === 'any' && typeof dateMonth === 'number') {
      const monthToMatch = dateMonth as number;
      base = base.filter(b => {
        const dateStr = dateMode === 'due' ? b.dueDate : b.date;
        if (!dateStr) return false;
        const d = new Date(dateStr);
        return d.getMonth() + 1 === monthToMatch;
      });
      setFilteredBills(base);
      return;
    }

    const month = dateMonth as number;
    const year = dateYear as number;
    const firstOfMonth = new Date(year, month - 1, 1);
    const lastOfMonth = new Date(year, month, 0);
    const start = new Date(firstOfMonth);
    start.setDate(start.getDate() - 1);
    const end = new Date(lastOfMonth);
    end.setDate(end.getDate() + 1);
    base = base.filter(b => {
      const dateStr = dateMode === 'due' ? b.dueDate : b.date;
      if (!dateStr) return false;
      const d = new Date(dateStr);
      return d >= start && d <= end;
    });
    setFilteredBills(base);
  };

  const handleDownloadPdf = async (
    customerId: string,
    billId: string
  ): Promise<void> => {
    try {
      const response = await axiosInstance.get(
        `/customers/${customerId}/bills/${billId}/pdf?currency=${currency}`,
        {
          responseType: 'blob',
          headers: { 'Content-Type': 'application/pdf' },
          useV2: true,
        }
      );
      if (!response || response.status !== 200 || !response.data)
        throw new Error('Failed to download PDF');
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
    if (selectedBill) {
      setBills(prevBills =>
        prevBills.map(bill =>
          bill.billId === selectedBill.billId
            ? { ...bill, billStatus: 'PAID' }
            : bill
        )
      );
      setSelectedBill(null);
      setTimeout(() => {
        void fetchBills();
        window.dispatchEvent(new CustomEvent('paymentSuccess'));
      }, 100);
    }
  };

  const handlePaymentCancel = (): void => {
    setShowPaymentForm(false);
    setSelectedBill(null);
  };

  const openDetails = (bill: Bill): void => {
    setDetailBill(bill);
    setShowDetailModal(true);
  };

  const closeDetails = (): void => {
    setShowDetailModal(false);
    setDetailBill(null);
  };

  const formatTotalDue = (bill: Bill): string => {
    const amount = bill.taxedAmount ?? bill.amount ?? 0;
    if (currency === 'CAD') return `CAD $${amount.toFixed(2)}`;
    return `USD $${convertCurrency(amount, 'CAD', 'USD').toFixed(2)}`;
  };

  return (
    <div>
      <div className="filter-button-row">
        <button className="filter-btn" onClick={() => toggleSection('status')}>
          {activeSection === 'status' ? 'Close Status' : 'Filter by Status'}
        </button>
        <button className="filter-btn" onClick={() => toggleSection('amount')}>
          {activeSection === 'amount' ? 'Close Amount' : 'Filter by Amount'}
        </button>
        <button className="filter-btn" onClick={() => toggleSection('date')}>
          {activeSection === 'date' ? 'Close Date' : 'Filter by Date'}
        </button>
      </div>

      <div
        className="filterContainer"
        style={{ display: 'flex', alignItems: 'center', gap: '16px' }}
      >
        <label htmlFor="currencyFilter">Currency:</label>
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

      {activeSection === 'status' && (
        <div className="filter-section">
          <label htmlFor="statusFilter">Status:</label>
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
      )}

      {activeSection === 'amount' && (
        <div className="filter-section">
          <label>Amount:</label>
          <select
            value={amountRangeOption}
            onChange={e => setAmountRangeOption(e.target.value)}
          >
            <option value="none">None</option>
            <option value="0-100">&lt; 100</option>
            <option value="0-200">&lt; 200</option>
            <option value="0-500">&lt; 500</option>
            <option value="custom">Custom</option>
          </select>
          {amountRangeOption === 'custom' ? (
            <div className="custom-amount-row">
              <input
                type="number"
                placeholder="min"
                value={customMin}
                onChange={e => setCustomMin(e.target.value)}
              />
              <input
                type="number"
                placeholder="max"
                value={customMax}
                onChange={e => setCustomMax(e.target.value)}
              />
              <div className="filter-actions">
                <button onClick={applyAmountFilter}>Apply</button>
                <button
                  onClick={() => {
                    setAmountRangeOption('none');
                    setCustomMin('');
                    setCustomMax('');
                    setError(null);
                    setFilteredBills(bills);
                  }}
                >
                  Clear
                </button>
              </div>
            </div>
          ) : (
            <div className="filter-actions">
              <button onClick={applyAmountFilter}>Apply</button>
              <button
                onClick={() => {
                  setAmountRangeOption('none');
                  setFilteredBills(bills);
                }}
              >
                Clear
              </button>
            </div>
          )}
        </div>
      )}

      {activeSection === 'date' && (
        <div className="filter-section">
          <label>Date mode:</label>
          <select
            value={dateMode}
            onChange={e => setDateMode(e.target.value as 'due' | 'visit')}
          >
            <option value="due">Due date</option>
            <option value="visit">Visit date</option>
          </select>

          <label>Month:</label>
          <select
            value={dateMonth}
            onChange={e =>
              setDateMonth(
                e.target.value === 'any' ? 'any' : Number(e.target.value)
              )
            }
          >
            <option value="any">Any</option>
            {Array.from({ length: 12 }, (_, i) => i + 1).map(m => (
              <option key={m} value={m}>
                {new Date(0, m - 1).toLocaleString('default', {
                  month: 'long',
                })}
              </option>
            ))}
          </select>

          <label>Year:</label>
          <select
            value={dateYear}
            onChange={e =>
              setDateYear(
                e.target.value === 'any' ? 'any' : Number(e.target.value)
              )
            }
          >
            <option value="any">Any</option>
            {Array.from({ length: 7 })
              .map((_, i) => new Date().getFullYear() - 5 + i)
              .map(y => (
                <option key={y} value={y}>
                  {y}
                </option>
              ))}
          </select>

          <div className="filter-actions">
            <button onClick={applyDateFilter}>Apply</button>
            <button
              onClick={() => {
                setDateMonth(new Date().getMonth() + 1);
                setDateYear(new Date().getFullYear());
                setFilteredBills(bills);
                setError(null);
              }}
            >
              Clear
            </button>
          </div>
        </div>
      )}

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
                  <div className="billColumn leftColumn">
                    <div className="billField vet">
                      <strong>Vet:</strong>
                      <span className="billValue">
                        {bill.vetFirstName} {bill.vetLastName}
                      </span>
                    </div>
                    <div className="billField total">
                      <strong>Total due:</strong>
                      <span className="billValue">{formatTotalDue(bill)}</span>
                    </div>
                  </div>

                  <div className="billColumn rightColumn">
                    <div className="billField date">
                      <strong>Appointment date:</strong>
                      <span className="billValue">{bill.date}</span>
                    </div>
                    <div className="billField status">
                      <strong>Status:</strong>
                      <span
                        className={
                          bill.billStatus === 'OVERDUE'
                            ? 'status--overdue billValue'
                            : bill.billStatus === 'PAID'
                              ? 'status--paid billValue'
                              : 'billValue'
                        }
                      >
                        {bill.billStatus === 'OVERDUE'
                          ? 'Overdue'
                          : bill.billStatus}
                      </span>
                    </div>
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
