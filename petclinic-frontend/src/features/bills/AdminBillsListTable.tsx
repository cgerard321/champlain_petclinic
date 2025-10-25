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

export default function AdminBillsListTable({
  currency,
  setCurrency,
}: AdminBillsListTableProps): JSX.Element {
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
    // Required fields that user must fill
    if (!newBill.customerId) {
      setError('Please select a customer.');
      return false;
    }
    if (!newBill.vetId) {
      setError('Please select a vet.');
      return false;
    }
    if (!newBill.visitType) {
      setError('Please select a visit type.');
      return false;
    }
    if (newBill.amount <= 0) {
      setError('Amount must be greater than zero.');
      return false;
    }

    // Auto-filled fields should be populated by now, but let's verify
    if (!newBill.date) {
      setError('Date is required.');
      return false;
    }
    if (!newBill.billStatus) {
      setError('Status is required.');
      return false;
    }
    if (!newBill.dueDate) {
      setError('Due date is required.');
      return false;
    }

    // Business logic validation
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

    // Ensure we have a valid array to work with
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
      await addBill(formattedBill);
      setActiveSection(null);
      getBillsList(currentPage, 10);
      setError(null); // Clear any previous errors on success
    } catch (err: unknown) {
      console.error('Error creating bill:', err);
      
      // Extract detailed error message from the response
      let errorMessage = 'Failed to create bill. Please try again.';
      
      // Type guard for axios error
      if (err && typeof err === 'object' && 'response' in err) {
        const axiosErr = err as { response?: { data?: unknown } };
        if (axiosErr.response?.data) {
          const responseData = axiosErr.response.data;
          if (typeof responseData === 'string') {
            // Handle plain text error responses
            errorMessage = responseData;
          } else if (responseData && typeof responseData === 'object') {
            // Handle structured error responses
            const structuredData = responseData as Record<string, unknown>;
            if (typeof structuredData.message === 'string') {
              errorMessage = structuredData.message;
            } else if (typeof structuredData.reason === 'string') {
              errorMessage = structuredData.reason;
            }
          }
        }
      } else if (err && typeof err === 'object' && 'message' in err) {
        // Handle network/other errors
        const genericErr = err as { message: string };
        errorMessage = genericErr.message;
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
    fetchOwnersAndVets();
  }, [fetchOwnersAndVets]);

  // Auto-fill logic for better UX
  useEffect(() => {
    // Reset and auto-fill when modal opens
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
      setError(null); // Clear any previous errors
    }
  }, [activeSection]);

  // Update due date when bill date changes
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
          useV2: true, // required so gateway uses the v2 API path
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
    <div>
      <div className="filter-button-row">
        <button className="filter-btn" onClick={() => toggleSection('search')}>
          {activeSection === 'search' ? 'Close Search' : 'Search'}
        </button>
        <button className="filter-btn" onClick={() => toggleSection('filter')}>
          {activeSection === 'filter' ? 'Close Filter' : 'Filter'}
        </button>
        <button className="filter-btn" onClick={() => toggleSection('create')}>
          {activeSection === 'create' ? 'Close Create' : 'Create'}
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

        <div className="archive-toggle" style={{ marginLeft: '16px' }}>
          <label>
            <input
              type="checkbox"
              checked={showArchivedBills}
              onChange={e => setShowArchivedBills(e.target.checked)}
            />
            Show Archived Bills
          </label>
        </div>
      </div>

      {activeSection === 'search' && (
        <div className="filter-section">
          <input
            type="text"
            placeholder="Customer ID"
            value={filter.customerId}
            onChange={e => setFilter({ ...filter, customerId: e.target.value })}
          />
          <input
            type="text"
            placeholder="Enter Bill ID"
            value={searchId}
            onChange={e => setSearchId(e.target.value)}
          />
          <button onClick={handleSearch}>Search</button>
          {searchedBill && <button onClick={handleGoBack}>Go Back</button>}
        </div>
      )}

      {activeSection === 'filter' && (
        <div className="filter-section">
          <label htmlFor="billFilter">Status: </label>
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
          <label htmlFor="yearFilter">Year: </label>
          <input
            type="number"
            id="yearFilter"
            value={filterYear}
            onChange={e => setFilterYear(parseInt(e.target.value))}
          />
          <label htmlFor="monthFilter">Month: </label>
          <select
            id="monthFilter"
            value={filterMonth}
            onChange={e => setFilterMonth(parseInt(e.target.value))}
          >
            {Array.from({ length: 12 }, (_, i) => (
              <option key={i + 1} value={i + 1}>
                {new Date(0, i).toLocaleString('default', { month: 'long' })}
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
          <div className="filter-buttons">
            <button onClick={handleMonthFilter}>Filter</button>
            <button onClick={clearMonthFilter}>Clear</button>
          </div>
        </div>
      )}

      {activeSection === 'create' && (
        <div className="filter-section">
          <h3>Create New Bill</h3>
          {error && (
            <div
              style={{
                backgroundColor: '#fee',
                border: '1px solid #fcc',
                borderRadius: '4px',
                padding: '8px 12px',
                marginBottom: '16px',
                color: '#c33',
              }}
            >
              <strong>Error:</strong> {error}
            </div>
          )}
          <form
            onSubmit={e => {
              e.preventDefault();
              handleCreateBill();
            }}
          >
            <div>
              <label>Customer</label>
              <select
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
            </div>
            <div>
              <label>Vet</label>
              <select
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
            </div>
            <div>
              <label>Visit Type</label>
              <select
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
            </div>
            <div>
              <label>Date</label>
              <input
                type="date"
                value={newBill.date}
                min={new Date().toISOString().split('T')[0]}
                onChange={e => handleDateChange(e.target.value)}
                placeholder="Auto-filled to today"
              />
            </div>
            <div>
              <label>Amount ($)</label>
              <input
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
            </div>
            <div>
              <label>Status</label>
              <select
                value={newBill.billStatus}
                onChange={e =>
                  setNewBill({ ...newBill, billStatus: e.target.value })
                }
              >
                <option value="">Select Status</option>
                <option value="PAID">PAID</option>
                <option value="UNPAID">UNPAID</option>
              </select>
            </div>
            <div>
              <label>Due Date</label>
              <input
                type="date"
                value={newBill.dueDate}
                onChange={e =>
                  setNewBill({ ...newBill, dueDate: e.target.value })
                }
                placeholder="Auto-calculated (45 days after bill date)"
              />
            </div>
            <button type="submit">Create Bill</button>
          </form>
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
                <button onClick={closeDetails}>Close</button>
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
                    // Update the detailBill state to reflect the change
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
    </div>
  );
}
