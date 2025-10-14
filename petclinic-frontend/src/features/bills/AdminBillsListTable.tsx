import { useEffect, useState, useCallback } from 'react';
import { Bill } from '@/features/bills/models/Bill.ts';
import { getAllOwners } from '../customers/api/getAllOwners';
import { getAllVets } from '../veterinarians/api/getAllVets';
import { BillRequestModel } from './models/BillRequestModel';
import { addBill } from './api/addBill';
import { OwnerResponseModel } from '../customers/models/OwnerResponseModel';
import { VetResponseModel } from '../veterinarians/models/VetResponseModel';
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

export default function AdminBillsListTable(): JSX.Element {
  const navigate = useNavigate();
  const [showArchivedBills, setShowArchivedBills] = useState<boolean>(false);
  const [searchId, setSearchId] = useState<string>('');
  const [searchedBill, setSearchedBill] = useState<Bill | null>(null);
  const [selectedOwnerFilter, setSelectedOwnerFilter] = useState<string>('');
  const [selectedVetFilter, setSelectedVetFilter] = useState<string>('');
  const [selectedVisitTypeFilter, setSelectedVisitTypeFilter] =
    useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const { billsList, getBillsList, setCurrentPage, currentPage, hasMore } =
    useGetAllBillsPaginated();
  const [filter, setFilter] = useState<FilterModel>({
    customerId: '',
    //owner
    firstName: '',
    //owner
    lastName: '',
    visitType: '',
    //vetId: '',
    vetFirstName: '',
    vetLastName: '',
  });
  const [filterYear, setFilterYear] = useState<number>(
    new Date().getFullYear()
  );
  const [filterMonth, setFilterMonth] = useState<number>(
    new Date().getMonth() + 1
  );

  interface FilterModel {
    [key: string]: string;
    customerId: string;
    //owner
    firstName: string;
    //owner
    lastName: string;
    visitType: string;
    //vetId: string;
    vetFirstName: string;
    vetLastName: string;
  }

  const [selectedFilter, setSelectedFilter] = useState<string>('');
  const [filteredBills, setFilteredBills] = useState<Bill[] | null>(null);
  // Remove activeSection, add modal states
  const [showSearchModal, setShowSearchModal] = useState(false);
  const [showFilterModal, setShowFilterModal] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);

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
        // Reset to show all bills
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
    setSelectedFilter('');
    setFilterYear(new Date().getFullYear());
    setFilterMonth(new Date().getMonth() + 1);
    setSelectedOwnerFilter('');
    setSelectedVetFilter('');
    setSelectedVisitTypeFilter('');
    setFilter({
      customerId: '',
      firstName: '',
      lastName: '',
      visitType: '',
      vetFirstName: '',
      vetLastName: '',
    });
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
      setShowCreateModal(false);
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

  return (
    <div style={{ display: 'flex', minHeight: '120vh', background: '#fafbfc' }}>
      {/* Sidebar */}
      <aside className="modern-sidebar">
        <div className="sidebar-title">Options</div>
        <div className="sidebar-button-container">
          <button onClick={() => setShowSearchModal(true)}>Search</button>
          <button onClick={() => setShowFilterModal(true)}>Filter</button>
          <button onClick={() => setShowCreateModal(true)}>Create</button>
          <button
            type="button"
            className={
              showArchivedBills ? 'archived-active' : 'archived-inactive'
            }
            onClick={() => setShowArchivedBills(v => !v)}
            style={{ width: '92%' }}
          >
            {showArchivedBills ? 'Show Archived' : 'Show Archived'}
          </button>
        </div>
      </aside>
      {/* Main Content */}
      <main style={{ flex: 1, padding: '2rem 1rem' }}>
        {/* Modal for Search Bill */}
        {showSearchModal && (
          <div
            style={{
              position: 'fixed',
              top: 0,
              left: 0,
              width: '100vw',
              height: '100vh',
              background: 'rgba(0,0,0,0.5)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              zIndex: 1000,
            }}
          >
            <div
              style={{
                background: '#fff',
                padding: '2rem',
                borderRadius: '8px',
                minWidth: '400px',
                maxWidth: '90vw',
                maxHeight: '90vh',
                overflowY: 'auto',
                position: 'relative',
              }}
            >
              <button
                onClick={() => setShowSearchModal(false)}
                style={{
                  position: 'absolute',
                  top: 10,
                  right: 10,
                  fontSize: '1.2rem',
                }}
              >
                ×
              </button>
              <h3>Search Bill</h3>
              <input
                type="text"
                placeholder="Customer ID"
                value={filter.customerId}
                onChange={e =>
                  setFilter({ ...filter, customerId: e.target.value })
                }
              />
              <input
                type="text"
                placeholder="Enter Bill ID"
                value={searchId}
                onChange={e => setSearchId(e.target.value)}
              />
              <button
                onClick={handleSearch}
                style={{
                  background: '#009879',
                  color: '#fff',
                  border: 'none',
                  borderRadius: '4px',
                  padding: '0.5rem 2rem',
                  fontWeight: 600,
                  fontSize: '1rem',
                  cursor: 'pointer',
                  transition: 'background 0.2s, color 0.2s',
                }}
              >
                Search
              </button>
              {searchedBill && (
                <button
                  onClick={handleGoBack}
                  style={{
                    background: '#009879',
                    color: '#fff',
                    border: 'none',
                    borderRadius: '4px',
                    padding: '0.5rem 2rem',
                    fontWeight: 600,
                    fontSize: '1rem',
                    marginLeft: '0.5rem',
                    cursor: 'pointer',
                    transition: 'background 0.2s, color 0.2s',
                  }}
                >
                  Go Back
                </button>
              )}
            </div>
          </div>
        )}

        {/* Modal for Filter Bills */}
        {showFilterModal && (
          <div
            style={{
              position: 'fixed',
              top: 0,
              left: 0,
              width: '100vw',
              height: '100vh',
              background: 'rgba(0,0,0,0.5)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              zIndex: 1000,
            }}
          >
            <div
              style={{
                background: '#fff',
                padding: '2rem',
                borderRadius: '8px',
                minWidth: '400px',
                maxWidth: '90vw',
                maxHeight: '90vh',
                overflowY: 'auto',
                position: 'relative',
              }}
            >
              <button
                onClick={() => setShowFilterModal(false)}
                style={{
                  position: 'absolute',
                  top: 10,
                  right: 10,
                  fontSize: '1.2rem',
                }}
              >
                ×
              </button>
              <h3>Filter Bills</h3>
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
              <div className="filter-buttons">
                <button
                  onClick={handleMonthFilter}
                  style={{
                    background: '#009879',
                    color: '#fff',
                    border: 'none',
                    borderRadius: '4px',
                    padding: '0.5rem 2rem',
                    fontWeight: 600,
                    fontSize: '1rem',
                    cursor: 'pointer',
                    transition: 'background 0.2s, color 0.2s',
                  }}
                >
                  Filter
                </button>
                <button
                  onClick={clearMonthFilter}
                  style={{
                    background: '#009879',
                    color: '#fff',
                    border: 'none',
                    borderRadius: '4px',
                    padding: '0.5rem 2rem',
                    fontWeight: 600,
                    fontSize: '1rem',
                    marginLeft: '0.5rem',
                    cursor: 'pointer',
                    transition: 'background 0.2s, color 0.2s',
                  }}
                >
                  Clear
                </button>
              </div>
            </div>
          </div>
        )}
        {/* Modal for Create Bill */}
        {showCreateModal && (
          <div
            style={{
              position: 'fixed',
              top: 0,
              left: 0,
              width: '100vw',
              height: '100vh',
              background: 'rgba(0,0,0,0.5)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              zIndex: 1000,
            }}
          >
            <div
              style={{
                background: '#fff',
                padding: '2rem',
                borderRadius: '8px',
                minWidth: '400px',
                maxWidth: '90vw',
                maxHeight: '90vh',
                overflowY: 'auto',
                position: 'relative',
              }}
            >
              <button
                onClick={() => setShowCreateModal(false)}
                style={{
                  position: 'absolute',
                  top: 10,
                  right: 10,
                  fontSize: '1.2rem',
                }}
              >
                ×
              </button>
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
                <div className="breakLine">
                  <label className="breakLine"> Date </label>
                  <input
                    type="date"
                    value={newBill.date}
                    onChange={e =>
                      setNewBill({ ...newBill, date: e.target.value })
                    }
                  />
                </div>
                <div className="breakLine">
                  <label className="breakLine">Amount ($)</label>
                  <input
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
                  <label className="breakLine">Due Date</label>
                  <input
                    type="date"
                    value={newBill.dueDate}
                    onChange={e =>
                      setNewBill({ ...newBill, dueDate: e.target.value })
                    }
                  />
                </div>
                <button
                  type="submit"
                  style={{
                    background: '#009879',
                    color: '#fff',
                    border: 'none',
                    borderRadius: '4px',
                    padding: '0.5rem 2rem',
                    fontWeight: 600,
                    fontSize: '1rem',
                    marginTop: '1rem',
                    cursor: 'pointer',
                    transition: 'background 0.2s, color 0.2s',
                  }}
                >
                  Create Bill
                </button>
              </form>
            </div>
          </div>
        )}

        {searchedBill ? (
          <div>
            <h3>Searched Bill Details:</h3>
            <p>
              <strong>Bill ID:</strong> {searchedBill.billId}
            </p>
            <p>
              {' '}
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
            <table className="table table-striped">
              <thead>
                <tr>
                  <th>Bill ID</th>
                  <th> Customer ID </th>
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
                    <td>{bill.amount}</td>
                    <td>{bill.taxedAmount}</td>
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
      </main>
    </div>
  );
}
