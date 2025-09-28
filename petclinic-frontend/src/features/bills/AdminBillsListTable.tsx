import { useEffect, useState, useCallback } from 'react';
import { Bill } from '@/features/bills/models/Bill.ts';
import { getAllOwners } from '../customers/api/getAllOwners';
import { getAllVets } from '../veterinarians/api/getAllVets';
import { BillRequestModel } from './models/BillRequestModel';
import { addBill } from './api/addBill';
import { OwnerResponseModel } from '../customers/models/OwnerResponseModel';
import { VetResponseModel } from '../veterinarians/models/VetResponseModel';
import { deleteBill } from '@/features/bills/api/deleteBill.tsx';
import useGetAllBillsPaginated from '@/features/bills/hooks/useGetAllBillsPaginated.ts';
import './AdminBillsListTable.css';
import { useNavigate } from 'react-router-dom';
import { getAllPaidBills } from '@/features/bills/api/getAllPaidBills.tsx';
import { getAllOverdueBills } from '@/features/bills/api/getAllOverdueBills.tsx';
import { getAllUnpaidBills } from '@/features/bills/api/getAllUnpaidBills.tsx';
import { getBillByBillId } from '@/features/bills/api/GetBillByBillId.tsx';
import { getBillsByMonth } from '@/features/bills/api/getBillByMonth.tsx';

export default function AdminBillsListTable(): JSX.Element {
  const navigate = useNavigate();
  const [searchId, setSearchId] = useState<string>('');
  const [searchedBill, setSearchedBill] = useState<Bill | null>(null);
  const [error, setError] = useState<string | null>(null);
  const { billsList, getBillsList, setCurrentPage, currentPage, hasMore } =
    useGetAllBillsPaginated();
  const [filter, setFilter] = useState<FilterModel>({
    customerId: '',
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
  }

  const [selectedFilter, setSelectedFilter] = useState<string>('');
  const [filteredBills, setFilteredBills] = useState<Bill[] | null>(null);
  const [activeSection, setActiveSection] = useState<string | null>(null);

  const [newBill, setNewBill] = useState<BillRequestModel>({
    customerId: '',
    vetId: '',
    visitType: '',
    date: '',
    amount: 0,
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

  const validateForm = (): boolean => {
    if (
      !newBill.customerId ||
      !newBill.vetId ||
      !newBill.visitType ||
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

    return billsToFilter.filter(bill => {
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

  const handleDelete = async (billId: string): Promise<void> => {
    const billToDelete = billsList.find(bill => bill.billId === billId);
    if (!billToDelete) {
      console.error('Bill not found: ${billId}');
      window.alert('Bill not found: ${billId}');
      return;
    }

    const confirmDelete = window.confirm(
      'Are you sure you want to delete this bill?'
    );
    if (!confirmDelete) {
      return;
    }

    try {
      const response = await deleteBill(billToDelete);
      if (response.status === 200 || response.status === 204) {
        window.alert(`Bill ${billId} has been deleted successfully`);
        getBillsList(currentPage, 10);
      }
    } catch (error) {
      window.alert('Cannot delete this bill. It may be unpaid or overdue.');
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
  const [oldPaidBills, setOldPaidBills] = useState<Bill[]>([]);
  const [showOldPaidBills, setShowOldPaidBills] = useState(false);
  const [cutoffYear, setCutoffYear] = useState<number>(
    new Date().getFullYear()
  );
  const [showDeleteWarning, setShowDeleteWarning] = useState(false);

  const handleFetchOldPaidBills = async (): Promise<void> => {
    try {
      const bills = await getAllPaidBills();
      // filter client-side by cutoffYear
      const filtered = bills.filter(bill => {
        const billYear = new Date(bill.date).getFullYear();
        return billYear <= cutoffYear; // bills older than or equal to cutoff year
      });

      setOldPaidBills(filtered);
      setShowOldPaidBills(true);
    } catch (err) {
      console.error('Failed to fetch paid bills:', err);
      window.alert('Failed to fetch.' + err);
    }
  };

  const handleDeleteOldPaidBills = async (): Promise<void> => {
    const currentYear = new Date().getFullYear();

    // Check if cutoffYear is less than 5 years old
    if (currentYear - cutoffYear < 5) {
      setShowDeleteWarning(true); // show red text
      return; // do not delete
    }

    // Otherwise, proceed with deletion
    setShowDeleteWarning(false);

    const confirmDelete = window.confirm(
      `Are you sure you want to delete ${oldPaidBills.length} paid bills from ${cutoffYear} or earlier?`
    );
    if (!confirmDelete) return;

    try {
      for (const bill of oldPaidBills) {
        await deleteBill(bill);
      }
      window.alert(`${oldPaidBills.length} bills deleted successfully.`);
      setOldPaidBills([]);
      setShowOldPaidBills(false);
    } catch (err) {
      console.error('Error deleting bills:', err);
      window.alert('Some bills could not be deleted: ' + err);
    }
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
        <button onClick={() => toggleSection('debug')}>
          {activeSection === 'debug' ? 'Close Admin Delete' : 'Admin Delete'}
        </button>
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

      {activeSection === 'debug' && (
        <div className="p-4 mb-4 border rounded bg-gray-50">
          <h3 className="text-lg font-semibold mb-2">
            Fetch Paid Bills by Year
          </h3>
          <input
            type="number"
            value={cutoffYear}
            onChange={e => setCutoffYear(parseInt(e.target.value))}
            placeholder="Enter year"
          />
          <button
            className="ml-2"
            onClick={() => {
              handleFetchOldPaidBills();
              setShowDeleteWarning(false);
            }}
          >
            Get Bills
          </button>

          {showOldPaidBills && (
            <>
              {oldPaidBills.length === 0 ? (
                <p>No paid bills from {cutoffYear} or earlier.</p>
              ) : (
                <>
                  <table className="min-w-full border mt-4">
                    <thead>
                      <tr>
                        <th className="border px-2 py-1">Bill ID</th>
                        <th className="border px-2 py-1">CustomerID</th>
                        <th className="border px-2 py-1">Date</th>
                        <th className="border px-2 py-1">Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {oldPaidBills.map(bill => (
                        <tr key={bill.billId}>
                          <td className="border px-2 py-1">{bill.billId}</td>
                          <td className="border px-2 py-1">
                            {bill.customerId}
                          </td>
                          <td className="border px-2 py-1">{bill.date}</td>
                          <td className="border px-2 py-1">
                            {bill.billStatus}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  <div className="flex flex-col items-start space-y-2 mt-4">
                    <button
                      onClick={handleDeleteOldPaidBills}
                      className="bg-red-600 hover:bg-red-700 text-white font-semibold px-4 py-2 rounded"
                    >
                      Delete Bills
                    </button>

                    {/* Warning message */}
                    {showDeleteWarning && (
                      <p className="error-text">
                        Cannot delete bills that are not at least 5 years old.
                      </p>
                    )}
                  </div>
                </>
              )}
            </>
          )}
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
                <th>Actions</th>
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
                    <button
                      className="btn btn-danger"
                      onClick={() => handleDelete(bill.billId)}
                      title="delete"
                    >
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="32"
                        height="32"
                        fill="currentColor"
                        className="bi bi-trash"
                        viewBox="0 0 16 16"
                      >
                        <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z" />
                        <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1h3.5a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4zM2.5 3h11V2h-11z" />
                      </svg>
                    </button>
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
