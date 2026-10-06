import { useEffect, useState, useCallback } from 'react';
import { Bill } from '@/features/bills/models/Bill.ts';
import { getAllCustomers } from '@/features/customers/api/getAllCustomers.ts';
import { getAllVets } from '@/features/veterinarians/api/getAllVets';
import { BillRequestModel } from './models/BillRequestModel';
import { addBill } from './api/addBill';
import { CustomerResponseModel } from '@/features/customers/models/CustomerResponseModel.ts';
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel';
import useGetAllBillsPaginated from '@/features/bills/hooks/useGetAllBillsPaginated.ts';
import useGetAllBillsStream from '@/features/bills/hooks/useGetAllBillsStream';
import './AdminBillsListTable.css';
import { archiveBills } from './api/archiveBills';
//import { getAllPaidBills } from '@/features/bills/api/getAllPaidBills.tsx';
//import { getAllOverdueBills } from '@/features/bills/api/getAllOverdueBills.tsx';
//import { getAllUnpaidBills } from '@/features/bills/api/getAllUnpaidBills.tsx';
import { getBillByBillId } from '@/features/bills/api/GetBillByBillId.tsx';
//import { getBillsByMonth } from '@/features/bills/api/getBillByMonth.tsx';
//import { getAllBillsByOwnerName } from './api/getAllBillsByOwnerName';
//import { getAllBillsByVetName } from './api/getAllBillsByVetName';
//import { getAllBillsByVisitType } from './api/getAllBillsByVisitType';
//import { getAllBills } from './api/getAllBills';
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
  const [showStreamedBills, setShowStreamedBills] = useState(false);
  const [searchId, setSearchId] = useState('');
  const [searchedBill, setSearchedBill] = useState<Bill | null>(null);
  const [selectedCustomerFilter, setSelectedCustomerFilter] = useState('');
  const [selectedVetFilter, setSelectedVetFilter] = useState('');
  const [selectedVisitTypeFilter, setSelectedVisitTypeFilter] = useState('');
  const [error, setError] = useState<string | null>(null);
  const { billsList, getBillsList, setCurrentPage, currentPage, hasMore } =
    useGetAllBillsPaginated();
  const {
    bills: streamedBills,
    loading: streamLoading,
    error: streamError,
    getBillsStream,
  } = useGetAllBillsStream();

  const [filter, setFilter] = useState<FilterModel>({
    customerId: '',
    firstName: '',
    lastName: '',
    visitType: '',
    vetFirstName: '',
    vetLastName: '',
  });

  const [appliedFilter, setAppliedFilter] = useState<FilterModel>({
    customerId: '',
    firstName: '',
    lastName: '',
    visitType: '',
    vetFirstName: '',
    vetLastName: '',
  });

  // helper that forwards the current local filter state into the paginated API
  const callGetBillsListWithFilters = useCallback(
    async (page = 0, size = 10, filterToUse = appliedFilter): Promise<void> => {
      await getBillsList(
        page,
        size,
        undefined, // billId
        filterToUse.customerId || undefined,
        filterToUse.firstName || undefined,
        filterToUse.lastName || undefined,
        filterToUse.visitType || undefined,
        undefined, // vetId
        filterToUse.vetFirstName || undefined,
        filterToUse.vetLastName || undefined,
        showArchivedBills
      );
    },
    [getBillsList, appliedFilter, showArchivedBills]
  );

  const callGetBillsStream = useCallback(async (): Promise<void> => {
    await getBillsStream(
      undefined, // billId
      filter.customerId || undefined,
      filter.firstName || undefined,
      filter.lastName || undefined,
      filter.visitType || undefined,
      undefined, // vetId
      filter.vetFirstName || undefined,
      filter.vetLastName || undefined
    );
  }, [getBillsStream, filter]);

  const handleViewAllBills = async (): Promise<void> => {
    try {
      await callGetBillsStream();
      setShowStreamedBills(true);
    } catch {
      // Stay on paginated view if streaming fails
    }
  };

  const handleBackToPagination = (): void => {
    setShowStreamedBills(false);
  };
  const [filterYear, setFilterYear] = useState(new Date().getFullYear());
  const [filterMonth, setFilterMonth] = useState(0);
  const [appliedFilterYear, setAppliedFilterYear] = useState(
    new Date().getFullYear()
  );
  const [appliedFilterMonth, setAppliedFilterMonth] = useState(0);
  const [selectedFilter, setSelectedFilter] = useState('');
  const [appliedSelectedFilter, setAppliedSelectedFilter] = useState('');
  const [filteredBills, setFilteredBills] = useState<Bill[] | null>(null);
  const [applyFilters, setApplyFilters] = useState(false);
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
  const [customers, setCustomers] = useState<CustomerResponseModel[]>([]);
  const [vets, setVets] = useState<VetResponseModel[]>([]);
  const [detailBill, setDetailBill] = useState<Bill | null>(null);
  const [showDetailModal, setShowDetailModal] = useState<boolean>(false);
  const [currencyOpen, setCurrencyOpen] = useState<boolean>(false);
  const [sendEmail, setSendEmail] = useState<boolean>(false);
  const [currency, setCurrency] = useState<Currency>('CAD');
  const [customerError, setCustomerError] = useState<boolean>(false);
  const [vetError, setVetError] = useState<boolean>(false);
  const [visitTypeError, setVisitTypeError] = useState<boolean>(false);
  const [dateError, setDateError] = useState<boolean>(false);
  const [statusError, setStatusError] = useState<boolean>(false);
  const [dueDateError, setDueDateError] = useState<boolean>(false);

  const fetchCustomersAndVets = useCallback(async (): Promise<void> => {
    try {
      const customersList = await getAllCustomers();
      const vetsList = await getAllVets();
      setCustomers(customersList);
      setVets(vetsList);
    } catch (err) {
      setError('Failed to fetch customers and vets');
    }
  }, []);

  useEffect(() => {
    callGetBillsListWithFilters(currentPage, 10);
  }, [currentPage, callGetBillsListWithFilters, selectedFilter]);

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
    let valid = true;
    setCustomerError(false);
    setVetError(false);
    setVisitTypeError(false);
    setDateError(false);
    setStatusError(false);
    setDueDateError(false);
    setError(null);

    if (!newBill.customerId) {
      setCustomerError(true);
      valid = false;
    }
    if (!newBill.vetId) {
      setVetError(true);
      valid = false;
    }
    if (!newBill.visitType) {
      setVisitTypeError(true);
      valid = false;
    }
    if (newBill.amount <= 0) {
      setError('Please fill out this field.');
      valid = false;
    }
    if (!newBill.date) {
      setDateError(true);
      valid = false;
    }
    if (!newBill.billStatus) {
      setStatusError(true);
      valid = false;
    }
    if (!newBill.dueDate) {
      setDueDateError(true);
      valid = false;
    }
    const billDate = new Date(newBill.date);
    const dueDate = new Date(newBill.dueDate);
    if (billDate > dueDate) {
      setDateError(true);
      setError('The bill date cannot be after the due date.');
      valid = false;
    }
    return valid;
  };

  const handleFilterChange = (
    event: React.ChangeEvent<HTMLSelectElement>
  ): void => {
    const status = event.target.value;
    setSelectedFilter(status);
    setFilteredBills(null);
  };

  const handleArchiveToggle = (): void => {
    setCurrentPage(0);
    setShowArchivedBills(prev => !prev);
  };

  const handleCustomerNameChange = async (
    event: React.ChangeEvent<HTMLSelectElement>
  ): Promise<void> => {
    const fullName = event.target.value;
    setSelectedCustomerFilter(fullName);

    if (fullName) {
      const parts = fullName.trim().split(' ');
      const first = parts.shift() ?? '';
      const last = parts.join(' ') ?? '';
      setFilter(prev => ({ ...prev, firstName: first, lastName: last }));
    } else {
      setFilter(prev => ({ ...prev, firstName: '', lastName: '' }));
    }

    setFilteredBills(null);
  };

  const handleVetNameChange = async (
    event: React.ChangeEvent<HTMLSelectElement>
  ): Promise<void> => {
    const fullName = event.target.value;
    setSelectedVetFilter(fullName);

    if (fullName) {
      const parts = fullName.trim().split(' ');
      const first = parts.shift() ?? '';
      const last = parts.join(' ') ?? '';
      setFilter(prev => ({ ...prev, vetFirstName: first, vetLastName: last }));
    } else {
      setFilter(prev => ({ ...prev, vetFirstName: '', vetLastName: '' }));
    }

    setFilteredBills(null);
  };

  const handleVisitTypeChange = (
    event: React.ChangeEvent<HTMLSelectElement>
  ): void => {
    const visitType = event.target.value;
    setSelectedVisitTypeFilter(visitType);

    if (visitType) {
      setFilter(prev => ({ ...prev, visitType }));
      setFilteredBills(null);
    } else {
      setFilter(prev => ({ ...prev, visitType: '' }));
      callGetBillsListWithFilters(currentPage, 10);
    }
  };

  const handleFilters = async (): Promise<void> => {
    setError(null);
    setFilteredBills(null);
    setAppliedFilter(filter);
    setAppliedSelectedFilter(selectedFilter);
    setAppliedFilterYear(filterYear);
    setAppliedFilterMonth(filterMonth);
    setApplyFilters(true);
    setActiveSection(null);
    callGetBillsListWithFilters(currentPage, 10, filter);
  };

  const clearFilters = (): void => {
    const emptyFilter: FilterModel = {
      customerId: '',
      firstName: '',
      lastName: '',
      visitType: '',
      vetFirstName: '',
      vetLastName: '',
    };

    setFilterYear(new Date().getFullYear());
    setFilterMonth(0);
    setSelectedFilter('');
    setSelectedCustomerFilter('');
    setSelectedVetFilter('');
    setSelectedVisitTypeFilter('');
    setFilter(emptyFilter);
    setAppliedFilter(emptyFilter);
    setFilteredBills(null);
    setApplyFilters(false);
    setActiveSection(null);
    callGetBillsListWithFilters(currentPage, 10);
  };

  const getFilteredBills = (): Bill[] => {
    const billsToFilter = showStreamedBills
      ? streamedBills
      : filteredBills || billsList;
    if (!billsToFilter || !Array.isArray(billsToFilter)) {
      return [];
    }

    const filteredByArchiveStatus = showArchivedBills
      ? billsToFilter
      : billsToFilter.filter(bill => !bill.archive);

    if (!applyFilters) {
      return filteredByArchiveStatus;
    }

    return filteredByArchiveStatus.filter(bill => {
      const matchesStatus =
        !appliedSelectedFilter ||
        (bill.billStatus || '').toLowerCase() ===
          appliedSelectedFilter.toLowerCase();

      const matchesCustomerId =
        !appliedFilter.customerId ||
        bill.customerId.includes(appliedFilter.customerId);

      const customerFirst = appliedFilter.firstName?.trim();
      const customerLast = appliedFilter.lastName?.trim();
      const matchesCustomer =
        (!customerFirst ||
          (bill.customerFirstName || '')
            .toLowerCase()
            .includes(customerFirst.toLowerCase())) &&
        (!customerLast ||
          (bill.customerLastName || '')
            .toLowerCase()
            .includes(customerLast.toLowerCase()));

      // vet name
      const vetFirst = appliedFilter.vetFirstName?.trim();
      const vetLast = appliedFilter.vetLastName?.trim();
      const matchesVet =
        (!vetFirst ||
          (bill.vetFirstName || '')
            .toLowerCase()
            .includes(vetFirst.toLowerCase())) &&
        (!vetLast ||
          (bill.vetLastName || '')
            .toLowerCase()
            .includes(vetLast.toLowerCase()));

      const matchesVisitType =
        !appliedFilter.visitType ||
        (bill.visitType || '').toLowerCase() ===
          appliedFilter.visitType.toLowerCase();

      let matchesMonth = true;
      if (applyFilters) {
        const d = new Date(bill.date);
        matchesMonth =
          d.getFullYear() === appliedFilterYear &&
          (appliedFilterMonth === 0 || d.getMonth() + 1 === appliedFilterMonth);
      }

      return (
        matchesStatus &&
        matchesCustomerId &&
        matchesCustomer &&
        matchesVet &&
        matchesVisitType &&
        matchesMonth
      );
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
      callGetBillsListWithFilters(currentPage, 10);
      setError(null);
    } catch (err: unknown) {
      console.error('Error creating bill:', err);

      let errorMessage = 'Failed to create bill. Please try again.';

      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: unknown } };
        if (axiosErr.response?.data) {
          const responseData = axiosErr.response.data;
          if (typeof responseData === 'string') {
            errorMessage = responseData;
          } else if (responseData && typeof responseData === 'object') {
            const structuredData = responseData as Record<string, unknown>;
            if (typeof structuredData.message === 'string') {
              errorMessage = structuredData.message;
            } else if (typeof structuredData.reason === 'string') {
              errorMessage = structuredData.reason;
            }
          }
        } else if (err && typeof err === 'object' && 'message' in err) {
          // Handle network/other errors
          const genericErr = err as { message: string };
          errorMessage = genericErr.message;
        }
      }

      setError(errorMessage);
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
    fetchCustomersAndVets();
  }, [fetchCustomersAndVets]);

  useEffect(() => {
    if (activeSection === 'create') {
      const today = new Date().toISOString().split('T')[0];
      const dueDate = new Date();
      dueDate.setDate(dueDate.getDate() + 45);
      const dueDateString = dueDate.toISOString().split('T')[0];

      setNewBill({
        customerId: '',
        vetId: '',
        date: today,
        amount: 0,
        visitType: '',
        billStatus: 'UNPAID',
        dueDate: dueDateString,
      });
      setError(null);
    }
  }, [activeSection]);

  const handleDateChange = (selectedDate: string): void => {
    const billDate = new Date(selectedDate);
    const dueDate = new Date(billDate);
    dueDate.setDate(billDate.getDate() + 45);
    const dueDateString = dueDate.toISOString().split('T')[0];

    setNewBill(prev => ({
      ...prev,
      date: selectedDate,
      dueDate: dueDateString,
    }));
  };

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
    const amount = bill.totalAmount ?? bill.amount ?? 0;
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
            onClick={handleArchiveToggle}
          >
            {showArchivedBills ? 'Hide Archived' : 'Show Archived'}
          </button>
          {showStreamedBills ? (
            <button className="archive-btn" onClick={handleBackToPagination}>
              Back to Pages
            </button>
          ) : (
            <button
              className="archive-btn"
              onClick={handleViewAllBills}
              disabled={streamLoading}
            >
              {streamLoading ? 'Loading Bills...' : 'View All Bills'}
            </button>
          )}
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
                    <option value={0}>All Months</option>
                    {Array.from({ length: 12 }, (_, i) => (
                      <option key={i + 1} value={i + 1}>
                        {new Date(0, i).toLocaleString('default', {
                          month: 'long',
                        })}
                      </option>
                    ))}
                  </select>

                  <label htmlFor="customerNameFilter">Customer Name</label>
                  <select
                    id="customerNameFilter"
                    value={selectedCustomerFilter}
                    onChange={handleCustomerNameChange}
                  >
                    <option value="">All Customers</option>
                    {customers.map(customer => (
                      <option
                        key={customer.customerId}
                        value={`${customer.firstName} ${customer.lastName}`}
                      >
                        {customer.firstName} {customer.lastName}
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
                      onClick={handleFilters}
                    >
                      Filter
                    </button>
                    <button
                      className="primary-modal-btn wide-btn"
                      onClick={clearFilters}
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
                      onChange={e => {
                        setNewBill({ ...newBill, customerId: e.target.value });
                        setCustomerError(false);
                      }}
                      style={
                        customerError
                          ? { border: '2px solid #ff3b3b', outline: 'none' }
                          : {}
                      }
                    >
                      <option value="">Select Customer</option>
                      {customers.map(customer => (
                        <option key={customer.customerId} value={customer.customerId}>
                          {customer.firstName} {customer.lastName}
                        </option>
                      ))}
                    </select>

                    <label htmlFor="newVet">Vet</label>
                    <select
                      id="newVet"
                      value={newBill.vetId}
                      onChange={e => {
                        setNewBill({ ...newBill, vetId: e.target.value });
                        setVetError(false);
                      }}
                      style={
                        vetError
                          ? { border: '2px solid #ff3b3b', outline: 'none' }
                          : {}
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
                      onChange={e => {
                        setNewBill({ ...newBill, visitType: e.target.value });
                        setVisitTypeError(false);
                      }}
                      style={
                        visitTypeError
                          ? { border: '2px solid #ff3b3b', outline: 'none' }
                          : {}
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
                      min={new Date().toISOString().split('T')[0]}
                      onChange={e => {
                        handleDateChange(e.target.value);
                        setDateError(false);
                      }}
                      style={
                        dateError
                          ? { border: '2px solid #ff3b3b', outline: 'none' }
                          : {}
                      }
                    />

                    <label htmlFor="newAmount">Amount ($)</label>
                    <input
                      id="newAmount"
                      type="number"
                      min="0"
                      step="0.01"
                      value={newBill.amount === 0 ? '' : newBill.amount}
                      onChange={e =>
                        setNewBill({
                          ...newBill,
                          amount:
                            e.target.value === ''
                              ? 0
                              : parseFloat(e.target.value) || 0,
                        })
                      }
                      placeholder="Enter bill amount"
                      required
                    />

                    <label htmlFor="newStatus">Status</label>
                    <select
                      id="newStatus"
                      value={newBill.billStatus}
                      onChange={e =>
                        setNewBill({ ...newBill, billStatus: e.target.value })
                      }
                      style={
                        statusError
                          ? { border: '2px solid #ff3b3b', outline: 'none' }
                          : {}
                      }
                    >
                      <option value="">Select Status</option>
                      <option value="PAID">PAID</option>
                      <option value="UNPAID">UNPAID</option>
                    </select>

                    <label htmlFor="newDueDate">Due Date</label>
                    <input
                      id="newDueDate"
                      type="date"
                      value={newBill.dueDate}
                      onChange={e =>
                        setNewBill({ ...newBill, dueDate: e.target.value })
                      }
                      style={
                        dueDateError
                          ? { border: '2px solid #ff3b3b', outline: 'none' }
                          : {}
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
                        style={{ marginTop: '20.5px' }}
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
                <button onClick={handleGoBack}>Close</button>
              </div>

              <div style={{ marginTop: '12px' }}>
                <p>
                  <strong>Bill ID:</strong> {searchedBill.billId}
                </p>
                <p>
                  <strong>Customer ID:</strong> {searchedBill.customerId}
                </p>
                <p>
                  <strong>Customer Name:</strong> {searchedBill.customerFirstName}{' '}
                  {searchedBill.customerLastName}
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
                  <strong>Total Amount:</strong> {formatTotalDue(searchedBill)}
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
            {error || streamError ? (
              <p>{error || streamError}</p>
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
                            <strong>Customer:</strong>
                            <span className="billValue">
                              {bill.customerFirstName} {bill.customerLastName}
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

            {!showStreamedBills && (
              <div className="pagination-controls">
                {currentPage > 0 && (
                  <button onClick={handlePreviousPage}>Previous</button>
                )}

                <span> Page {currentPage + 1} </span>

                {hasMore && <button onClick={handleNextPage}>Next</button>}
              </div>
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
                  <strong>Customer Name:</strong> {detailBill.customerFirstName}{' '}
                  {detailBill.customerLastName}
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
                  <strong>GST (5%):</strong>{' '}
                  {currency === 'CAD'
                    ? `CAD $${detailBill.gstAmount.toFixed(2)}`
                    : `USD $${convertCurrency(detailBill.gstAmount, 'CAD', 'USD').toFixed(2)}`}
                </p>
                <p>
                  <strong>QST (9.975%):</strong>{' '}
                  {currency === 'CAD'
                    ? `CAD $${detailBill.qstAmount.toFixed(2)}`
                    : `USD $${convertCurrency(detailBill.qstAmount, 'CAD', 'USD').toFixed(2)}`}
                </p>
                <p>
                  <strong>Total Tax:</strong>{' '}
                  {currency === 'CAD'
                    ? `CAD $${detailBill.taxedAmount.toFixed(2)}`
                    : `USD $${convertCurrency(detailBill.taxedAmount, 'CAD', 'USD').toFixed(2)}`}
                </p>
                <p>
                  <strong>Total with Interest:</strong>{' '}
                  {currency === 'CAD'
                    ? `CAD $${detailBill.totalAmount.toFixed(2)}`
                    : `USD $${convertCurrency(detailBill.totalAmount, 'CAD', 'USD').toFixed(2)}`}
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
                      callGetBillsListWithFilters(currentPage, 10);
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
