import { useEffect, useState, useCallback } from 'react';
import { Bill } from '@/features/bills/models/Bill.ts';
import { getAllOwners } from '@/features/customers/api/getAllOwners';
import { getAllVets } from '@/features/veterinarians/api/getAllVets';
import { BillRequestModel } from './models/BillRequestModel';
import { addBill } from './api/addBill';
import { OwnerResponseModel } from '@/features/customers/models/OwnerResponseModel';
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel';
import useGetAllBillsPaginated from '@/features/bills/hooks/useGetAllBillsPaginated.ts';
import './AdminBillsListTable.css';
import { archiveBills } from './api/archiveBills';
import { getAllPaidBills } from '@/features/bills/api/getAllPaidBills.tsx';
import { getAllOverdueBills } from '@/features/bills/api/getAllOverdueBills.tsx';
import { getAllUnpaidBills } from '@/features/bills/api/getAllUnpaidBills.tsx';
import { getBillByBillId } from '@/features/bills/api/GetBillByBillId.tsx';
import { getBillsByMonth } from '@/features/bills/api/getBillByMonth.tsx';
import { getAllBillsByOwnerName } from './api/getAllBillsByOwnerName';
import { getAllBillsByVetName } from './api/getAllBillsByVetName';
import { getAllBillsByVisitType } from './api/getAllBillsByVisitType';
import { getAllBills } from './api/getAllBills';
import InterestExemptToggle from './components/InterestExemptToggle';
import { Currency, convertCurrency } from './utils/convertCurrency';
import axiosInstance from '@/shared/api/axiosInstance';

interface AdminBillsListTableProps {
  currency: Currency;
  setCurrency: (c: Currency) => void;
}

interface FilterModel {
  [key: string]: string;
  customerId: string;
  firstName: string;
  lastName: string;
  visitType: string;
  vetFirstName: string;
  vetLastName: string;
}

export default function AdminBillsListTable({}: AdminBillsListTableProps): JSX.Element {
  const [showArchivedBills, setShowArchivedBills] = useState(false);
  const [searchId, setSearchId] = useState('');
  const [searchedBill, setSearchedBill] = useState<Bill | null>(null);
  const [selectedOwnerFilter, setSelectedOwnerFilter] = useState('');
  const [selectedVetFilter, setSelectedVetFilter] = useState('');
  const [selectedVisitTypeFilter, setSelectedVisitTypeFilter] = useState('');
  const [error, setError] = useState<string | null>(null);
  const { billsList, getBillsList, setCurrentPage, currentPage, hasMore } =
    useGetAllBillsPaginated();
  const [filter, setFilter] = useState<FilterModel>({
    customerId: '',
    firstName: '',
    lastName: '',
    visitType: '',
    vetFirstName: '',
    vetLastName: '',
  });
  const [filterYear, setFilterYear] = useState(new Date().getFullYear());
  const [filterMonth, setFilterMonth] = useState(new Date().getMonth() + 1);
  const [selectedFilter, setSelectedFilter] = useState('');
  const [filteredBills, setFilteredBills] = useState<Bill[] | null>(null);
  const [activeSection, setActiveSection] = useState<string | null>(null);

  const [newBill, setNewBill] = useState<BillRequestModel>({
    customerId: '',
    vetId: '',
    date: '',
    amount: 0,
    visitType: '',
    billStatus: '',
    dueDate: '',
  });
  const [owners, setOwners] = useState<OwnerResponseModel[]>([]);
  const [vets, setVets] = useState<VetResponseModel[]>([]);
  const [detailBill, setDetailBill] = useState<Bill | null>(null);
  const [showDetailModal, setShowDetailModal] = useState<boolean>(false);
  const [currencyOpen, setCurrencyOpen] = useState<boolean>(false);
  const [sendEmail, setSendEmail] = useState<boolean>(false);
  const [currency, setCurrency] = useState<Currency>('CAD');

  const fetchOwnersAndVets = useCallback(async (): Promise<void> => {
    try {
      const ownersList = await getAllOwners();
      const vetsList = await getAllVets();
      setOwners(ownersList);
      setVets(vetsList);
    } catch (err) {
      setError('Failed to fetch owners and vets');
    }
  }, []);

  useEffect(() => {
    if (!selectedFilter) {
      getBillsList(currentPage, 10);
    }
  }, [currentPage, getBillsList, selectedFilter]);

  useEffect(() => {
    const callArchiveBills = async (): Promise<void> => {
      try {
        await archiveBills();
      } catch (error) {
        console.error('Error calling archive bills endpoint:', error);
        setError('Failed to archive bills');
      }
    };
    callArchiveBills();
  }, []);

  const validateForm = (): boolean => {
    if (
      !newBill.customerId ||
      !newBill.vetId ||
      !newBill.date ||
      newBill.amount <= 0 ||
      !newBill.billStatus ||
      !newBill.dueDate
    ) {
      setError(
        'All fields are required and the amount must be greater than zero.'
      );
      return false;
    }
    const billDate = new Date(newBill.date);
    const dueDate = new Date(newBill.dueDate);
    if (billDate > dueDate) {
      setError('The bill date cannot be after the due date.');
      return false;
    }
    setError(null);
    return true;
  };

  const handleFilterChange = async (
    event: React.ChangeEvent<HTMLSelectElement>
  ): Promise<void> => {
    const status = event.target.value;
    setSelectedFilter(status);
    try {
      if (status === 'paid') {
        const paidBills = await getAllPaidBills();
        setFilteredBills(paidBills);
      } else if (status === 'unpaid') {
        const unpaidBills = await getAllUnpaidBills();
        setFilteredBills(unpaidBills);
      } else if (status === 'overdue') {
        const overdueBills = await getAllOverdueBills();
        setFilteredBills(overdueBills);
      } else {
        setFilteredBills(null);
        getBillsList(currentPage, 10);
      }
    } catch (error) {
      console.error('Error fetching filtered bills:', error);
      setError('Error fetching filtered bills. Please try again.');
    }
  };

  const handleOwnerNameChange = async (
    event: React.ChangeEvent<HTMLSelectElement>
  ): Promise<void> => {
    const fullName = event.target.value;
    setSelectedOwnerFilter(fullName);
    try {
      if (fullName) {
        const [ownerFirstName, ownerLastName] = fullName.split(' ');
        if (ownerFirstName && ownerLastName) {
          const billsByOwner = await getAllBillsByOwnerName(
            ownerFirstName,
            ownerLastName
          );
          setFilteredBills(billsByOwner);
        }
      } else {
        const all = await getAllBills();
        setFilteredBills(all);
      }
    } catch (error) {
      console.error('Error fetching bills by owner name:', error);
      setError('Error fetching bills by owner name. Please try again.');
    }
  };

  const handleVetNameChange = async (
    event: React.ChangeEvent<HTMLSelectElement>
  ): Promise<void> => {
    const fullName = event.target.value;
    setSelectedVetFilter(fullName);
    try {
      if (fullName) {
        const [vetFirstName, vetLastName] = fullName.split(' ');
        if (vetFirstName && vetLastName) {
          const billsByVetName = await getAllBillsByVetName(
            vetFirstName,
            vetLastName
          );
          setFilteredBills(billsByVetName);
        } else {
          setSelectedFilter('');
          const all = await getAllBills();
          setFilteredBills(all);
        }
      }
    } catch (error) {
      console.error('Error fetching bills by vet name:', error);
      setError('Error fetching bills by vet name. Please try again.');
    }
  };

  const handleVisitTypeChange = async (
    event: React.ChangeEvent<HTMLSelectElement>
  ): Promise<void> => {
    const visitType = event.target.value;
    setSelectedVisitTypeFilter(visitType);
    try {
      if (visitType) {
        const billsByVisitType = await getAllBillsByVisitType(visitType);
        setFilteredBills(billsByVisitType);
      } else {
        setSelectedVisitTypeFilter('');
        getBillsList(currentPage, 10);
      }
    } catch (error) {
      console.error('Error fetching bills by visit type:', error);
      setError('Error fetching bills by visit type. Please try again.');
    }
  };

  const handleMonthFilter = async (): Promise<void> => {
    setError(null);
    setFilteredBills(null);
    try {
      const billsByMonth = await getBillsByMonth(filterYear, filterMonth);
      setFilteredBills(billsByMonth);
    } catch (err) {
      console.error('Error fetching bills by month:', err);
      setError('Error fetching bills by month. Please try again.');
    }
  };

  const clearMonthFilter = (): void => {
    setFilterYear(new Date().getFullYear());
    setFilterMonth(new Date().getMonth() + 1);
    setFilteredBills(null);
    getBillsList(currentPage, 10);
  };

  const getFilteredBills = (): Bill[] => {
    const billsToFilter = filteredBills || billsList;
    if (!billsToFilter || !Array.isArray(billsToFilter)) {
      return [];
    }

    const filteredByArchiveStatus = showArchivedBills
      ? billsToFilter
      : billsToFilter.filter(bill => !bill.archive);

    if (
      filteredBills &&
      (selectedOwnerFilter ||
        selectedFilter === 'paid' ||
        selectedFilter === 'unpaid' ||
        selectedFilter === 'overdue')
    ) {
      return filteredByArchiveStatus.filter(bill => {
        const matchesCustomerId =
          !filter.customerId || bill.customerId.includes(filter.customerId);
        return matchesCustomerId;
      });
    }

    if (filteredBills && filter.vetId) {
      return filteredByArchiveStatus.filter(
        bill => bill.vetId === filter.vetId
      );
    }

    return filteredByArchiveStatus.filter(bill => {
      const matchesStatus =
        !selectedFilter ||
        bill.billStatus.toLowerCase() === selectedFilter.toLowerCase();
      const matchesCustomerId =
        !filter.customerId || bill.customerId.includes(filter.customerId);
      return matchesStatus && matchesCustomerId;
    });
  };

  const handleCreateBill = async (): Promise<void> => {
    const isValid = validateForm();
    if (!isValid) {
      return;
    }
    const formattedBill = {
      ...newBill,
      billStatus: newBill.billStatus.toUpperCase(),
    };
    try {
      await addBill(formattedBill, sendEmail, currency);
      setActiveSection(null);
      getBillsList(currentPage, 10);
    } catch (err) {
      console.error('Error creating bill:', err);
      setError('Failed to create bill. Please try again.');
    }
  };

  const handleSearch = async (): Promise<void> => {
    setError(null);
    if (searchId) {
      try {
        const bill = await getBillByBillId(searchId);
        if (bill) {
          setSearchedBill(bill);
        } else {
          throw new Error('Bill not found');
        }
      } catch (err) {
        setError('Invalid Bill ID. Please try again.');
        setSearchedBill(null);
      }
    }
  };

  const handleGoBack = (): void => {
    setSearchedBill(null);
    setSearchId('');
    setError(null);
  };

  useEffect(() => {
    fetchOwnersAndVets();
  }, [fetchOwnersAndVets]);

  const handlePreviousPage = (): void => {
    if (currentPage > 0) {
      setCurrentPage(currentPage - 1);
    }
  };

  const handleNextPage = (): void => {
    if (hasMore) {
      setCurrentPage(currentPage + 1);
    }
  };

  const toggleSection = (section: string): void => {
    setActiveSection(activeSection === section ? null : section);
  };

  const formatTotalDue = (bill: Bill): string => {
    const amount = bill.taxedAmount ?? bill.amount ?? 0;
    if (currency === 'CAD') return `CAD $${amount.toFixed(2)}`;
    return `USD $${convertCurrency(amount, 'CAD', 'USD').toFixed(2)}`;
  };

  const openDetails = (bill: Bill): void => {
    setDetailBill(bill);
    setShowDetailModal(true);
  };

  const closeDetails = (): void => {
    setShowDetailModal(false);
    setDetailBill(null);
  };

  const handleDownloadStaffPdf = async (billId: string): Promise<void> => {
    try {
      const response = await axiosInstance.get(
        `/bills/${billId}/pdf?currency=${currency}`,
        {
          responseType: 'blob',
          headers: { Accept: 'application/pdf' },
          useV2: true,
        }
      );

      if (!response || response.status !== 200 || !response.data) {
        throw new Error('Failed to download staff PDF');
      }

      const blob = new Blob([response.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `staff-bill-${billId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error('Error downloading staff PDF:', error);
      alert('Failed to generate the bill PDF. Please try again.');
    }
  };

  return (
    <div className="admin-bills-page" style={{ display: 'flex', gap: '18px' }}>
      <aside className="modern-sidebar">
        <div className="sidebar-title">Options</div>
        <div className="sidebar-button-container">
          <button onClick={() => toggleSection('search')}>
            {activeSection === 'search' ? 'Close Search' : 'Search'}
          </button>
          <button onClick={() => toggleSection('filter')}>
            {activeSection === 'filter' ? 'Close Filter' : 'Filter'}
          </button>
          <button onClick={() => toggleSection('create')}>
            {activeSection === 'create' ? 'Close Create' : 'Create'}
          </button>
          <button
            className={`archive-btn ${showArchivedBills ? 'active' : ''}`}
            onClick={() => setShowArchivedBills(prev => !prev)}
          >
            {showArchivedBills ? 'Hide Archived' : 'Show Archived'}
          </button>
        </div>

        <div style={{ marginTop: '12px' }}>
          <div
            className="currency-dropdown"
            tabIndex={0}
            onBlur={() => setCurrencyOpen(false)}
          >
            <button
              type="button"
              className="currency-btn"
              aria-haspopup="true"
              aria-expanded={currencyOpen}
              aria-label="Select currency"
              onClick={() => setCurrencyOpen((prev: boolean) => !prev)}
            >
              <span className="currency-label">
                <span className="currency-prefix">Currency:</span>
                <span className="currency-value">{currency}</span>
              </span>
              <span className="caret">▾</span>
            </button>

            {currencyOpen && (
              <ul className="currency-menu" role="menu">
                <li>
                  <button
                    type="button"
                    onMouseDown={() => {
                      setCurrency('CAD');
                      setCurrencyOpen(false);
                    }}
                    role="menuitem"
                  >
                    CAD
                  </button>
                </li>
                <li>
                  <button
                    type="button"
                    onMouseDown={() => {
                      setCurrency('USD');
                      setCurrencyOpen(false);
                    }}
                    role="menuitem"
                  >
                    USD
                  </button>
                </li>
              </ul>
            )}
          </div>
        </div>
      </aside>

      <main style={{ flex: 1 }}>
        {activeSection === 'search' && (
          <div className="modalOverlay">
            <div className="modalContent form-modal">
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <h3>Search Bills</h3>
                <button
                  className="modal-close-btn"
                  onClick={() => toggleSection('search')}
                >
                  Close
                </button>
              </div>
              <div style={{ marginTop: '12px' }}>
                <div className="form-grid">
                  <label htmlFor="customerIdSearch">Customer ID</label>
                  <input
                    id="customerIdSearch"
                    type="text"
                    placeholder="Customer ID"
                    value={filter.customerId}
                    onChange={e =>
                      setFilter({ ...filter, customerId: e.target.value })
                    }
                  />

                  <label htmlFor="billIdSearch">Bill ID</label>
                  <input
                    id="billIdSearch"
                    type="text"
                    placeholder="Enter Bill ID"
                    value={searchId}
                    onChange={e => setSearchId(e.target.value)}
                  />

                  <div className="form-actions">
                    <button
                      className="primary-modal-btn wide-btn"
                      onClick={handleSearch}
                    >
                      Search
                    </button>
                    {searchedBill && (
                      <button onClick={handleGoBack}>Go Back</button>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeSection === 'filter' && (
          <div className="modalOverlay">
            <div className="modalContent form-modal">
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <h3>Filter Bills</h3>
                <button
                  className="modal-close-btn"
                  onClick={() => toggleSection('filter')}
                >
                  Close
                </button>
              </div>
              <div style={{ marginTop: '12px' }}>
                <div className="form-grid">
                  <label htmlFor="billFilter">Status</label>
                  <select
                    id="billFilter"
                    value={selectedFilter}
                    onChange={handleFilterChange}
                  >
                    <option value="">All Bills</option>
                    <option value="unpaid">Unpaid</option>
                    <option value="paid">Paid</option>
                    <option value="overdue">Overdue</option>
                  </select>

                  <label htmlFor="yearFilter">Year</label>
                  <input
                    type="number"
                    id="yearFilter"
                    value={filterYear}
                    onChange={e => setFilterYear(parseInt(e.target.value))}
                  />

                  <label htmlFor="monthFilter">Month</label>
                  <select
                    id="monthFilter"
                    value={filterMonth}
                    onChange={e => setFilterMonth(parseInt(e.target.value))}
                  >
                    {Array.from({ length: 12 }, (_, i) => (
                      <option key={i + 1} value={i + 1}>
                        {new Date(0, i).toLocaleString('default', {
                          month: 'long',
                        })}
                      </option>
                    ))}
                  </select>

                  <label htmlFor="ownerNameFilter">Owner Name</label>
                  <select
                    id="ownerNameFilter"
                    value={selectedOwnerFilter}
                    onChange={handleOwnerNameChange}
                  >
                    <option value="">All Owners</option>
                    {owners.map(owner => (
                      <option
                        key={owner.ownerId}
                        value={`${owner.firstName} ${owner.lastName}`}
                      >
                        {owner.firstName} {owner.lastName}
                      </option>
                    ))}
                  </select>

                  <label htmlFor="vetNameFilter">Vet Name</label>
                  <select
                    id="vetNameFilter"
                    value={selectedVetFilter}
                    onChange={handleVetNameChange}
                  >
                    <option value="">All Vets</option>
                    {vets.map(vet => (
                      <option
                        key={vet.vetId}
                        value={`${vet.firstName} ${vet.lastName}`}
                      >
                        {vet.firstName} {vet.lastName}
                      </option>
                    ))}
                  </select>

                  <label htmlFor="visitTypeFilter">Visit Type</label>
                  <select
                    id="visitTypeFilter"
                    value={selectedVisitTypeFilter}
                    onChange={handleVisitTypeChange}
                  >
                    <option value="">All Visit Types</option>
                    <option value="Checkup">Check-Up</option>
                    <option value="Vaccine">Vaccine</option>
                    <option value="Surgery">Surgery</option>
                    <option value="Dental">Dental</option>
                    <option value="Regular">Regular</option>
                    <option value="Emergency">Emergency</option>
                  </select>

                  <div className="form-actions">
                    <button
                      className="primary-modal-btn wide-btn"
                      onClick={handleMonthFilter}
                    >
                      Filter
                    </button>
                    <button
                      className="primary-modal-btn wide-btn"
                      onClick={clearMonthFilter}
                    >
                      Clear
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

        {activeSection === 'create' && (
          <div className="modalOverlay">
            <div className="modalContent form-modal">
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <h3>Create New Bill</h3>
                <button
                  className="modal-close-btn"
                  onClick={() => toggleSection('create')}
                >
                  Close
                </button>
              </div>
              <div style={{ marginTop: '12px' }}>
                {error && <p style={{ color: 'red' }}>{error}</p>}
                <form
                  onSubmit={e => {
                    e.preventDefault();
                    handleCreateBill();
                  }}
                >
                  <div className="form-grid">
                    <label htmlFor="newCustomer">Customer</label>
                    <select
                      id="newCustomer"
                      value={newBill.customerId}
                      onChange={e =>
                        setNewBill({ ...newBill, customerId: e.target.value })
                      }
                    >
                      <option value="">Select Customer</option>
                      {owners.map(owner => (
                        <option key={owner.ownerId} value={owner.ownerId}>
                          {owner.firstName} {owner.lastName}
                        </option>
                      ))}
                    </select>

                    <label htmlFor="newVet">Vet</label>
                    <select
                      id="newVet"
                      value={newBill.vetId}
                      onChange={e =>
                        setNewBill({ ...newBill, vetId: e.target.value })
                      }
                    >
                      <option value="">Select Vet</option>
                      {vets.map(vet => (
                        <option key={vet.vetId} value={vet.vetId}>
                          {vet.firstName} {vet.lastName}
                        </option>
                      ))}
                    </select>

                    <label htmlFor="newVisitType">Visit Type</label>
                    <select
                      id="newVisitType"
                      value={newBill.visitType}
                      onChange={e =>
                        setNewBill({ ...newBill, visitType: e.target.value })
                      }
                    >
                      <option value="">Select Visit Type</option>
                      <option value="CHECKUP">Check-Up</option>
                      <option value="VACCINE">Vaccine</option>
                      <option value="SURGERY">Surgery</option>
                      <option value="DENTAL">Dental</option>
                    </select>

                    <label htmlFor="newDate">Date</label>
                    <input
                      id="newDate"
                      type="date"
                      value={newBill.date}
                      onChange={e =>
                        setNewBill({ ...newBill, date: e.target.value })
                      }
                    />

                    <label htmlFor="newAmount">Amount ($)</label>
                    <input
                      id="newAmount"
                      type="number"
                      min="0"
                      step="0.01"
                      value={newBill.amount}
                      onChange={e =>
                        setNewBill({
                          ...newBill,
                          amount: parseFloat(e.target.value),
                        })
                      }
                    />

                    <label htmlFor="newStatus">Status</label>
                    <select
                      id="newStatus"
                      value={newBill.billStatus}
                      onChange={e =>
                        setNewBill({ ...newBill, billStatus: e.target.value })
                      }
                    >
                      <option value="">Select Status</option>
                      <option value="PAID">PAID</option>
                      <option value="UNPAID">UNPAID</option>
                      <option value="OVERDUE">OVERDUE</option>
                    </select>

                    <label htmlFor="newDueDate">Due Date</label>
                    <input
                      id="newDueDate"
                      type="date"
                      value={newBill.dueDate}
                      onChange={e =>
                        setNewBill({ ...newBill, dueDate: e.target.value })
                      }
                    />
                    <div>
                      <label htmlFor="sendEmail">Send Email Notification</label>
                      <select
                        id="sendEmail"
                        value={sendEmail ? 'true' : 'false'}
                        onChange={e => setSendEmail(e.target.value === 'true')}
                      >
                        <option value="true">Yes</option>
                        <option value="false">No</option>
                      </select>
                    </div>
                    <div>
                      <label htmlFor="billCurrency">
                        Bill Currency for Email:
                      </label>
                      <select
                        id="billCurrency"
                        value={currency}
                        onChange={e => setCurrency(e.target.value as Currency)}
                      >
                        <option value="CAD">CAD</option>
                        <option value="USD">USD</option>
                      </select>
                    </div>

                    <div className="form-actions">
                      <button type="submit" className="full-width-btn">
                        Create Bill
                      </button>
                    </div>
                  </div>
                </form>
              </div>
            </div>
          </div>
        )}

        {searchedBill ? (
          <div className="modalOverlay">
            <div className="modalContent">
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                }}
              >
                <h3>Search Results - Bill Details</h3>
                <button className="modal-close-btn" onClick={handleGoBack}>
                  Close
                </button>
              </div>

              <div style={{ marginTop: '12px' }}>
                <p>
                  <strong>Bill ID:</strong> {searchedBill.billId}
                </p>
                <p>
                  <strong>Customer ID:</strong> {searchedBill.customerId}
                </p>
                <p>
                  <strong>Owner Name:</strong> {searchedBill.ownerFirstName}{' '}
                  {searchedBill.ownerLastName}
                </p>
                <p>
                  <strong>Visit Type:</strong> {searchedBill.visitType}
                </p>
                <p>
                  <strong>Vet Name:</strong> {searchedBill.vetFirstName}{' '}
                  {searchedBill.vetLastName}
                </p>
                <p>
                  <strong>Date:</strong> {searchedBill.date}
                </p>
                <p>
                  <strong>Amount:</strong> {formatTotalDue(searchedBill)}
                </p>
                <p>
                  <strong>Status:</strong> {searchedBill.billStatus}
                </p>
                <p>
                  <strong>Due Date:</strong> {searchedBill.dueDate}
                </p>
                <div style={{ marginTop: '16px' }}>
                  <strong>Interest Exempt:</strong>{' '}
                  {searchedBill.interestExempt ? 'Yes' : 'No'}
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div>
            {error ? (
              <p>{error}</p>
            ) : (
              <div className="billsListContainer">
                {getFilteredBills().length === 0 ? (
                  <p>No bills to display.</p>
                ) : (
                  getFilteredBills().map(bill => (
                    <div
                      key={bill.billId}
                      className="billCard"
                      data-bill-id={bill.billId}
                    >
                      <div className="billCardContent">
                        <div className="billColumn leftColumn">
                          <div className="billField">
                            <strong>Owner:</strong>
                            <span className="billValue">
                              {bill.ownerFirstName} {bill.ownerLastName}
                            </span>
                          </div>
                          <div className="billField">
                            <strong>Vet:</strong>
                            <span className="billValue">
                              {bill.vetFirstName} {bill.vetLastName}
                            </span>
                          </div>
                        </div>

                        <div className="billColumn rightColumn">
                          <div className="billField">
                            <strong>Total:</strong>
                            <span className="billValue">
                              {formatTotalDue(bill)}
                            </span>
                          </div>
                          <div className="billField status">
                            <strong>Status:</strong>
                            <span
                              className={`billValue ${
                                bill.billStatus?.toLowerCase() === 'overdue'
                                  ? 'status--overdue'
                                  : bill.billStatus?.toLowerCase() === 'paid'
                                    ? 'status--paid'
                                    : ''
                              }`}
                            >
                              {bill.billStatus}
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

            <div className="pagination-controls">
              {currentPage > 0 && (
                <button onClick={handlePreviousPage}>Previous</button>
              )}
              <span> Page {currentPage + 1} </span>
              {hasMore && <button onClick={handleNextPage}>Next</button>}
            </div>
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
                <div style={{ display: 'flex', gap: '8px' }}>
                  <button
                    className="detailsButton printButton"
                    onClick={() => handleDownloadStaffPdf(detailBill.billId)}
                  >
                    Print Bill (PDF)
                  </button>
                  <button className="modal-close-btn" onClick={closeDetails}>
                    Close
                  </button>
                </div>
              </div>

              <div style={{ marginTop: '12px' }}>
                <p>
                  <strong>Bill ID:</strong> {detailBill.billId}
                </p>
                <p>
                  <strong>Customer ID:</strong> {detailBill.customerId}
                </p>
                <p>
                  <strong>Owner Name:</strong> {detailBill.ownerFirstName}{' '}
                  {detailBill.ownerLastName}
                </p>
                <p>
                  <strong>Visit Type:</strong> {detailBill.visitType}
                </p>
                <p>
                  <strong>Vet Name:</strong> {detailBill.vetFirstName}{' '}
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
                  <strong>Taxed Amount:</strong>{' '}
                  {currency === 'CAD'
                    ? `CAD $${detailBill.taxedAmount.toFixed(2)}`
                    : `USD $${convertCurrency(detailBill.taxedAmount, 'CAD', 'USD').toFixed(2)}`}
                </p>
                <p>
                  <strong>Status:</strong>{' '}
                  <span
                    className={
                      detailBill.billStatus?.toLowerCase() === 'overdue'
                        ? 'status--overdue'
                        : detailBill.billStatus?.toLowerCase() === 'paid'
                          ? 'status--paid'
                          : ''
                    }
                  >
                    {detailBill.billStatus}
                  </span>
                </p>
                <p>
                  <strong>Due Date:</strong> {detailBill.dueDate}
                </p>
                <div style={{ marginTop: '16px' }}>
                  <strong>Interest Exempt:</strong>
                  <InterestExemptToggle
                    billId={detailBill.billId}
                    isExempt={detailBill.interestExempt || false}
                    onToggleComplete={() => {
                      getBillsList(currentPage, 10);
                      setDetailBill({
                        ...detailBill,
                        interestExempt: !detailBill.interestExempt,
                      });
                    }}
                    variant="simple"
                  />
                </div>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
