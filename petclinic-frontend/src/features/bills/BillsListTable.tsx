import { useUser } from '@/context/UserContext';
import { Bill } from '@/features/bills/models/Bill.ts';
import axiosInstance from '@/shared/api/axiosInstance';
import { useCallback, useEffect, useState, useMemo } from 'react';
import './BillsListTable.css';
import PaymentForm from './PaymentForm';
import { Currency, convertCurrency } from './utils/convertCurrency';
import { useTranslation } from 'react-i18next';

interface BillsListTableProps {
  currency: Currency;
  setCurrency: (c: Currency) => void;
}

export default function BillsListTable({
  currency,
  setCurrency,
}: BillsListTableProps): JSX.Element {
  const { t, i18n } = useTranslation('bills');
  const { user } = useUser();
  const [bills, setBills] = useState<Bill[]>([]);
  const [filteredBills, setFilteredBills] = useState<Bill[]>([]);
  const [error, setError] = useState<string | null>(null);

  const safeBills = useMemo(() => (Array.isArray(bills) ? bills : []), [bills]);
  const safeFilteredBills = useMemo(
    () => (Array.isArray(filteredBills) ? filteredBills : []),
    [filteredBills]
  );

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
      setError(t('errors.fetchBills'));
      setBills([]);
      setFilteredBills([]);
    }
  }, [user?.userId, t]);

  useEffect(() => {
    void fetchBills();
  }, [fetchBills]);

  useEffect(() => {
    if (selectedStatus === 'all') {
      setFilteredBills(safeBills);
      return;
    }
    setFilteredBills(
      safeBills.filter(
        b => (b.billStatus || '').toLowerCase() === selectedStatus.toLowerCase()
      )
    );
  }, [selectedStatus, safeBills]);

  const toggleSection = useCallback(
    (section: 'status' | 'amount' | 'date'): void => {
      setActiveSection(prev => {
        const closing = prev === section;
        if (closing) {
          setSelectedStatus('all');
          setAmountRangeOption('none');
          setCustomMin('');
          setCustomMax('');
          setDateMonth(new Date().getMonth() + 1);
          setDateYear(new Date().getFullYear());
          setFilteredBills(safeBills.slice());
          setError(null);
          return null;
        }
        return section;
      });
    },
    [safeBills]
  );

  useEffect(() => {
    const onStatus = (): void => {
      toggleSection('status');
    };
    const onAmount = (): void => {
      toggleSection('amount');
    };
    const onDate = (): void => {
      toggleSection('date');
    };

    window.addEventListener(
      'customerToggleStatusFilter',
      onStatus as EventListener
    );
    window.addEventListener(
      'customerToggleAmountFilter',
      onAmount as EventListener
    );
    window.addEventListener(
      'customerToggleDateFilter',
      onDate as EventListener
    );

    return () => {
      window.removeEventListener(
        'customerToggleStatusFilter',
        onStatus as EventListener
      );
      window.removeEventListener(
        'customerToggleAmountFilter',
        onAmount as EventListener
      );
      window.removeEventListener(
        'customerToggleDateFilter',
        onDate as EventListener
      );
    };
  }, [toggleSection]);

  useEffect(() => {
    void setCurrency;
  }, [setCurrency]);

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
        setError(t('errors.invalidAmountRange'));
        return;
      }
      base = base.filter(b => (b.amount ?? 0) >= min && (b.amount ?? 0) <= max);
      setFilteredBills(base);
      return;
    }
    if (amountRangeOption.startsWith('0-')) {
      const max = Number(amountRangeOption.split('-')[1]);
      if (isNaN(max)) {
        setError(t('errors.invalidAmountOption'));
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
    const amount = bill.totalAmount ?? bill.amount ?? 0;
    if (currency === 'CAD') return `CAD $${amount.toFixed(2)}`;
    return `USD $${convertCurrency(amount, 'CAD', 'USD').toFixed(2)}`;
  };

  return (
    <div>
      {activeSection === 'status' && (
        <div className="modalOverlay">
          <div className="modalContent form-modal">
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <h3>{t('filterStatus.title')}</h3>
              <button
                className="modal-close-btn"
                onClick={() => toggleSection('status')}
              >
                {t('buttons.close')}
              </button>
            </div>
            <div style={{ marginTop: '12px' }}>
              <div className="form-grid">
                <label htmlFor="statusFilter">{t('status')}</label>
                <select
                  id="statusFilter"
                  value={selectedStatus}
                  onChange={e => setSelectedStatus(e.target.value)}
                >
                  <option value="all">{t('filterStatus.options.all')}</option>
                  <option value="paid">{t('statusType.paid')}</option>
                  <option value="unpaid">{t('statusType.unpaid')}</option>
                </select>

                <div className="form-actions">
                  <button
                    className="primary-modal-btn wide-btn"
                    onClick={() => toggleSection('status')}
                  >
                    {t('buttons.apply')}
                  </button>
                  <button
                    onClick={() => {
                      setSelectedStatus('all');
                      setFilteredBills(safeBills);
                      setError(null);
                      toggleSection('status');
                    }}
                  >
                    {t('buttons.clear')}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeSection === 'amount' && (
        <div className="modalOverlay">
          <div className="modalContent form-modal">
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <h3>{t('filterAmount.title')}</h3>
              <button
                className="modal-close-btn"
                onClick={() => toggleSection('amount')}
              >
                {t('buttons.close')}
              </button>
            </div>
            <div style={{ marginTop: '12px' }}>
              <div className="form-grid">
                <label>{t('filterAmount.label')}</label>
                <select
                  value={amountRangeOption}
                  onChange={e => setAmountRangeOption(e.target.value)}
                >
                  <option value="none">{t('filterAmount.options.none')}</option>
                  <option value="0-100">&lt; 100</option>
                  <option value="0-200">&lt; 200</option>
                  <option value="0-500">&lt; 500</option>
                  <option value="custom">
                    {t('filterAmount.options.custom')}
                  </option>
                </select>

                {amountRangeOption === 'custom' && (
                  <>
                    <label>{t('filterAmount.custom.labelMin')}</label>
                    <input
                      type="number"
                      placeholder={t('filterAmount.custom.min')}
                      value={customMin}
                      onChange={e => setCustomMin(e.target.value)}
                    />
                    <label>{t('filterAmount.custom.labelMax')}</label>
                    <input
                      type="number"
                      placeholder={t('filterAmount.custom.max')}
                      value={customMax}
                      onChange={e => setCustomMax(e.target.value)}
                    />
                  </>
                )}

                <div className="form-actions">
                  <button
                    className="primary-modal-btn wide-btn"
                    onClick={() => {
                      applyAmountFilter();
                      toggleSection('amount');
                    }}
                  >
                    {t('buttons.apply')}
                  </button>
                  <button
                    onClick={() => {
                      setAmountRangeOption('none');
                      setCustomMin('');
                      setCustomMax('');
                      setError(null);
                      setFilteredBills(safeBills);
                      toggleSection('amount');
                    }}
                  >
                    {t('buttons.clear')}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {activeSection === 'date' && (
        <div className="modalOverlay">
          <div className="modalContent form-modal">
            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
              }}
            >
              <h3>{t('filterDate.title')}</h3>
              <button
                className="modal-close-btn"
                onClick={() => toggleSection('date')}
              >
                {t('buttons.close')}
              </button>
            </div>
            <div style={{ marginTop: '12px' }}>
              <div className="form-grid">
                <label>Date mode</label>
                <select
                  value={dateMode}
                  onChange={e => setDateMode(e.target.value as 'due' | 'visit')}
                >
                  <option value="due">{t('filterDate.options.dueDate')}</option>
                  <option value="visit">
                    {t('filterDate.options.visitDate')}
                  </option>
                </select>

                <label>{t('filterDate.month')}</label>
                <select
                  value={dateMonth}
                  onChange={e =>
                    setDateMonth(
                      e.target.value === 'any' ? 'any' : Number(e.target.value)
                    )
                  }
                >
                  <option value="any">{t('filterDate.options.any')}</option>
                  {Array.from({ length: 12 }, (_, i) => i + 1).map(m => (
                    <option key={m} value={m}>
                      {new Date(2000, m - 1, 1).toLocaleString(
                        i18n.language.startsWith('fr') ? 'fr-CA' : 'en-CA',
                        { month: 'long' }
                      )}
                    </option>
                  ))}
                </select>

                <label>{t('filterDate.year')}</label>
                <select
                  value={dateYear}
                  onChange={e =>
                    setDateYear(
                      e.target.value === 'any' ? 'any' : Number(e.target.value)
                    )
                  }
                >
                  <option value="any">{t('filterDate.options.any')}</option>
                  {Array.from({ length: 7 })
                    .map((_, i) => new Date().getFullYear() - 5 + i)
                    .map(y => (
                      <option key={y} value={y}>
                        {y}
                      </option>
                    ))}
                </select>

                <div className="form-actions">
                  <button
                    className="primary-modal-btn wide-btn"
                    onClick={() => {
                      applyDateFilter();
                      toggleSection('date');
                    }}
                  >
                    {t('buttons.apply')}
                  </button>
                  <button
                    onClick={() => {
                      setDateMonth(new Date().getMonth() + 1);
                      setDateYear(new Date().getFullYear());
                      setFilteredBills(safeBills);
                      setError(null);
                      toggleSection('date');
                    }}
                  >
                    {t('buttons.clear')}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {error ? (
        <p>{error}</p>
      ) : (
        <div className="billsListContainer">
          {safeFilteredBills.length === 0 ? (
            <p>{t('billCard.empty')}</p>
          ) : (
            safeFilteredBills.map(bill => (
              <div
                key={bill.billId}
                className="billCard"
                data-bill-id={bill.billId}
              >
                <div className="billCardContent">
                  <div className="billColumn leftColumn">
                    <div className="billField vet">
                      <strong>{t('billCard.vet')}</strong>
                      <span className="billValue">
                        {bill.vetFirstName} {bill.vetLastName}
                      </span>
                    </div>
                    <div className="billField total">
                      <strong>{t('billCard.totalDue')}</strong>
                      <span className="billValue">{formatTotalDue(bill)}</span>
                    </div>
                  </div>

                  <div className="billColumn rightColumn">
                    <div className="billField date">
                      <strong>{t('billCard.date')}</strong>
                      <span className="billValue">{bill.date}</span>
                    </div>
                    <div className="billField status">
                      <strong>{t('status')}</strong>
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
                          ? t('statusType.overdue')
                          : bill.billStatus === 'PAID'
                            ? t('statusType.paid')
                            : t('statusType.unpaid')}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="billActions">
                  <button
                    className="detailsButton"
                    onClick={() => openDetails(bill)}
                  >
                    {t('buttons.details')}
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
              <h3>{t('billCard.detail.title')}</h3>
              <button onClick={closeDetails}>{t('buttons.close')}</button>
            </div>

            <div style={{ marginTop: '12px' }}>
              <p>
                <strong>{t('billCard.detail.id')}</strong> {detailBill.billId}
              </p>
              <p>
                <strong>{t('billCard.detail.customer')}</strong>{' '}
                {detailBill.customerFirstName} {detailBill.customerLastName}
              </p>
              <p>
                <strong>{t('billCard.detail.visit')}</strong>{' '}
                {t(`visitTypes.${String(detailBill.visitType).toLowerCase()}`)}
              </p>
              <p>
                <strong>{t('billCard.vet')}</strong> {detailBill.vetFirstName}{' '}
                {detailBill.vetLastName}
              </p>
              <p>
                <strong>{t('billCard.date')}:</strong> {detailBill.date}
              </p>
              <p>
                <strong>{t('billCard.detail.amount')}:</strong>{' '}
                {currency === 'CAD'
                  ? `CAD $${detailBill.amount.toFixed(2)}`
                  : `USD $${convertCurrency(detailBill.amount, 'CAD', 'USD').toFixed(2)}`}
              </p>
              <p>
                <strong>{t('billCard.detail.gst')}</strong>{' '}
                {currency === 'CAD'
                  ? `CAD $${detailBill.gstAmount.toFixed(2)}`
                  : `USD $${convertCurrency(detailBill.gstAmount, 'CAD', 'USD').toFixed(2)}`}
              </p>
              <p>
                <strong>{t('billCard.detail.qst')}</strong>{' '}
                {currency === 'CAD'
                  ? `CAD $${detailBill.qstAmount.toFixed(2)}`
                  : `USD $${convertCurrency(detailBill.qstAmount, 'CAD', 'USD').toFixed(2)}`}
              </p>
              <p>
                <strong>{t('billCard.detail.tax')}</strong>{' '}
                {currency === 'CAD'
                  ? `CAD $${detailBill.taxedAmount.toFixed(2)}`
                  : `USD $${convertCurrency(detailBill.taxedAmount, 'CAD', 'USD').toFixed(2)}`}
              </p>
              <p>
                <strong>{t('billCard.detail.interest')}</strong>{' '}
                {currency === 'CAD'
                  ? `CAD $${(detailBill.interest || 0).toFixed(2)}`
                  : `USD $${convertCurrency(detailBill.interest || 0, 'CAD', 'USD').toFixed(2)}`}
              </p>
              <p>
                <strong>{t('billCard.totalDue')}</strong>{' '}
                {currency === 'CAD'
                  ? `CAD $${detailBill.totalAmount.toFixed(2)}`
                  : `USD $${convertCurrency(detailBill.totalAmount, 'CAD', 'USD').toFixed(2)}`}
              </p>
              <p>
                <strong>{t('status')}</strong> {detailBill.billStatus}
              </p>
              <p>
                <strong>{t('billCard.detail.dueDate')}</strong>{' '}
                {detailBill.dueDate}
              </p>
              <p>
                <strong>{t('billCard.detail.time')}</strong>{' '}
                {detailBill.billStatus === 'PAID' ? (
                  <span style={{ color: 'green' }}>
                    {t('billCard.detail.timeStatus.paid')}
                  </span>
                ) : detailBill.timeRemaining === 0 ? (
                  <span style={{ color: 'red' }}>
                    {t('billCard.detail.timeStatus.zero')}
                  </span>
                ) : (
                  t('billCard.detail.timeStatus.unpaid', {
                    count: detailBill.timeRemaining,
                  })
                )}
              </p>
            </div>

            <div style={{ display: 'flex', gap: '12px', marginTop: '16px' }}>
              <button
                onClick={() =>
                  handleDownloadPdf(user.userId, detailBill.billId)
                }
              >
                {t('buttons.downloadPdf')}
              </button>
              {detailBill.billStatus !== 'PAID' && (
                <button
                  onClick={() => {
                    handlePayBillClick(detailBill);
                    closeDetails();
                  }}
                >
                  {t('buttons.payBill')}
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
          billAmount={selectedBill.totalAmount}
          baseAmount={selectedBill.amount}
          qstAmount={selectedBill.qstAmount || 0}
          gstAmount={selectedBill.gstAmount || 0}
          taxedAmount={selectedBill.taxedAmount || 0}
          interestAmount={selectedBill.interest || 0}
          onPaymentSuccess={handlePaymentSuccess}
          onCancel={handlePaymentCancel}
        />
      )}
    </div>
  );
}
