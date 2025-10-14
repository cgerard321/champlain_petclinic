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
import { useNavigate } from 'react-router-dom';
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
  const navigate = useNavigate();
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

  const handleEditClick = (): void => {
    navigate(`/bills/admin/${searchId}/edit`);
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

  return (
    <div>
      <div className="button-container">
        <button onClick={() => toggleSection('search')}>
          {activeSection === 'search' ? 'Close Search' : 'Search'}
        </button>
        <button onClick={() => toggleSection('filter')}>
          {activeSection === 'filter' ? 'Close Filter' : 'Filter'}
        </button>
        <button onClick={() => toggleSection('create')}>
          {activeSection === 'create' ? 'Close Create' : 'Create'}
        </button>
        <div className="archive-toggle">
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
        <div className="create-bill-form">
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
        <div className="create-bill-form">
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
        <div className="create-bill-form">
          <h3>Create New Bill</h3>
          {error && <p style={{ color: 'red' }}>{error}</p>}
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
                onChange={e => setNewBill({ ...newBill, date: e.target.value })}
              />
            </div>
            <div>
              <label>Amount ($)</label>
              <input
                type="number"
                min="0"
                step="0.01"
                value={newBill.amount}
                onChange={e =>
                  setNewBill({ ...newBill, amount: parseFloat(e.target.value) })
                }
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
                <option value="OVERDUE">OVERDUE</option>
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
              />
            </div>
            <button type="submit">Create Bill</button>
          </form>
        </div>
      )}

      {searchedBill ? (
        <div>
          <h3>Searched Bill Details:</h3>
          <p>
            <strong>Bill ID:</strong> {searchedBill.billId}
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
            <strong>Amount:</strong> {searchedBill.amount}
          </p>
          <p>
            <strong>Taxed Amount:</strong> {searchedBill.taxedAmount}
          </p>
          <p>
            <strong>Status:</strong> {searchedBill.billStatus}
          </p>
          <p>
            <strong>Due Date:</strong> {searchedBill.dueDate}
          </p>
          <button onClick={handleEditClick}>Edit Bill</button>
        </div>
      ) : (
        <div className="admin-bills-list-table-container">
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '16px',
              marginBottom: '16px',
            }}
          >
            <label htmlFor="statusFilter">Status:</label>
            <select
              id="statusFilter"
              value={selectedFilter}
              onChange={handleFilterChange}
              style={{ width: '150px' }}
            >
              <option value="">All Bills</option>
              <option value="unpaid">Unpaid</option>
              <option value="paid">Paid</option>
              <option value="overdue">Overdue</option>
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
          <table className="table table-striped">
            <thead>
              <tr>
                <th>Bill ID</th>
                <th>Customer ID</th>
                <th>Owner Name</th>
                <th>Visit Type</th>
                <th>Vet Name</th>
                <th>Date</th>
                <th>Amount</th>
                <th>Taxed Amount</th>
                <th>Status</th>
                <th>Due Date</th>
                <th>Interest Exempt</th>
              </tr>
            </thead>
            <tbody>
              {getFilteredBills().map((bill: Bill) => (
                <tr key={bill.billId}>
                  <td>{bill.billId}</td>
                  <td>{bill.customerId}</td>
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
                      ? `CAD $${bill.taxedAmount.toFixed(2)}`
                      : `USD $${convertCurrency(bill.taxedAmount, 'CAD', 'USD').toFixed(2)}`}
                  </td>
                  <td>{bill.billStatus}</td>
                  <td>{bill.dueDate}</td>
                  <td>
                    <InterestExemptToggle
                      billId={bill.billId}
                      isExempt={bill.interestExempt || false}
                      onToggleComplete={() => getBillsList(currentPage, 10)}
                      variant="simple"
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="pagination-controls">
            {currentPage > 0 && (
              <button onClick={handlePreviousPage}>Previous</button>
            )}
            <span> Page {currentPage + 1} </span>
            {hasMore && <button onClick={handleNextPage}>Next</button>}
          </div>
        </div>
      )}
    </div>
  );
}
