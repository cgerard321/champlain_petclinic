import { useUser } from '@/context/UserContext';
import { Bill } from '@/features/bills/models/Bill.ts';
import axiosInstance from '@/shared/api/axiosInstance';
import { useCallback, useEffect, useState } from 'react';
import './BillsListTable.css';
import PaymentForm from './PaymentForm';
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

  // primary filters
  const [selectedStatus, setSelectedStatus] = useState<string>('all');
  const [activeSection, setActiveSection] = useState<
    'status' | 'amount' | 'date' | null
  >(null);

  // amount filter
  const [amountRangeOption, setAmountRangeOption] = useState<string>('none');
  const [customMin, setCustomMin] = useState<string>('');
  const [customMax, setCustomMax] = useState<string>('');

  // date filter (month/year with Any)
  const [dateMode, setDateMode] = useState<'due' | 'visit'>('due');
  const [dateMonth, setDateMonth] = useState<number | 'any'>(
    new Date().getMonth() + 1
  );
  const [dateYear, setDateYear] = useState<number | 'any'>(
    new Date().getFullYear()
  );

  // payment form
  const [selectedBill, setSelectedBill] = useState<Bill | null>(null);
  const [showPaymentForm, setShowPaymentForm] = useState<boolean>(false);

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
    } catch (err) {
      console.error('Error fetching bills:', err);
      setError('Failed to fetch bills');
    }
  }, [user?.userId]);

  useEffect(() => {
    void fetchBills();
  }, [fetchBills]);

  // Apply base status filter whenever bills or status changes
  useEffect(() => {
    let base = bills.slice();
    if (selectedStatus !== 'all')
      base = base.filter(
        b => (b.billStatus || '').toLowerCase() === selectedStatus.toLowerCase()
      );
    setFilteredBills(base);
  }, [bills, selectedStatus]);

  const toggleSection = (section: 'status' | 'amount' | 'date'): void => {
    setActiveSection(prev => {
      const closing = prev === section;
      if (closing) {
        // Reset all filters when the section is closed
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

  // amount filtering (client-side)
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
      base = base.filter(b => b.amount >= min && b.amount <= max);
      setFilteredBills(base);
      return;
    }
    if (amountRangeOption.startsWith('0-')) {
      const max = Number(amountRangeOption.split('-')[1]);
      if (isNaN(max)) {
        setError('Invalid amount option');
        return;
      }
      base = base.filter(b => b.amount <= max);
      setFilteredBills(base);
      return;
    }
    setFilteredBills(base);
  };

  // date filtering with month/year and ±1 day expansion
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

    // both month and year specified
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
      setBills(prev =>
        prev.map(b =>
          b.billId === selectedBill.billId ? { ...b, billStatus: 'PAID' } : b
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
            onChange={e => {
              setAmountRangeOption(e.target.value);
            }}
          >
            <option value="none">None</option>
            <option value="0-100">&lt; 100</option>
            <option value="0-200">&lt; 200</option>
            <option value="0-500">&lt; 500</option>
            <option value="custom">Custom</option>
          </select>
          {amountRangeOption === 'custom' && (
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
          )}
          {amountRangeOption !== 'custom' && (
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
        <p className="error-text">{error}</p>
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
                  <td>${bill.amount.toFixed(2)}</td>
                  <td>${(bill.interest || 0).toFixed(2)}</td>
                  <td>${bill.taxedAmount.toFixed(2)}</td>
                  <td
                    className={
                      bill.billStatus === 'PAID'
                        ? 'status-paid'
                        : bill.billStatus === 'OVERDUE'
                          ? 'status-overdue'
                          : ''
                    }
                  >
                    {bill.billStatus}
                  </td>
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
                      <span className="time-paid">This bill is paid</span>
                    ) : bill.timeRemaining === 0 ? (
                      <span className="time-zero">
                        0 days remaining to pay bill
                      </span>
                    ) : (
                      `${bill.timeRemaining} days remaining to pay bill`
                    )}
                  </td>
                  <td>
                    <button
                      onClick={() =>
                        void handleDownloadPdf(user.userId, bill.billId)
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
