import { Fragment, useEffect, useMemo, useState } from 'react';
import { CustomerResponseModel } from '@/features/customers/models/CustomerResponseModel.ts';
import './AllCustomers.css';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import { Link, useSearchParams } from 'react-router-dom';
import { getAllCustomers } from '@/features/customers/api/getAllCustomers.ts';
import { deleteCustomer } from '@/features/customers/api/deleteCustomer.ts';
import { IsVet } from '@/context/UserContext';
import { provincesOfCanada } from '@/features/customers/utils/provinces.ts';
import {
  CustomerFilter,
  filterCustomers,
  filterFromSearchParams,
  hasActiveFilters,
} from '@/features/customers/utils/customerFilter.ts';

const TEXT_FILTERS: { key: keyof CustomerFilter; label: string }[] = [
  { key: 'customerId', label: 'Customer ID' },
  { key: 'firstName', label: 'First Name' },
  { key: 'lastName', label: 'Last Name' },
  { key: 'address', label: 'Address' },
  { key: 'city', label: 'City' },
  { key: 'telephone', label: 'Telephone' },
];

const AllCustomers: React.FC = (): JSX.Element => {
  const isVet = IsVet();
  const [customers, setCustomers] = useState<CustomerResponseModel[]>([]);
  const [isFilterVisible, setFilterVisible] = useState(true);
  const [isLoading, setIsLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);

  const [searchParams, setSearchParams] = useSearchParams();
  const filter = useMemo(
    () => filterFromSearchParams(searchParams),
    [searchParams]
  );

  const updateFilter = (key: keyof CustomerFilter, value: string): void => {
    setSearchParams(
      prev => {
        const next = new URLSearchParams(prev);
        if (value) next.set(key, value);
        else next.delete(key);
        return next;
      },
      { replace: true }
    );
  };

  const clearFilters = (): void => {
    setSearchParams({}, { replace: true });
  };

  useEffect(() => {
    const getCustomers = async (): Promise<void> => {
      try {
        const fetchedCustomers = await getAllCustomers();
        setCustomers(fetchedCustomers);
      } catch (error) {
        console.error('Failed to load customers:', error);
        setLoadError('Unable to load customers. Please try again later.');
      } finally {
        setIsLoading(false);
      }
    };

    getCustomers();
  }, []);

  const handleDelete = async (customerId: string): Promise<void> => {
    const confirmDelete = window.confirm(
      'Are you sure you want to delete this customer?'
    );

    if (confirmDelete) {
      await deleteCustomer(customerId);
      setCustomers(
        customers.filter(customer => customer.customerId !== customerId)
      );
      alert('Customer deleted successfully.');
    } else {
      alert('Customer deletion canceled.');
    }
  };
  const filteredCustomers = useMemo(
    () => filterCustomers(customers, filter),
    [customers, filter]
  );

  const renderResults = (): JSX.Element => {
    if (isLoading) {
      return (
        <p className="customers-state" role="status">
          Loading customers…
        </p>
      );
    }

    if (loadError) {
      return (
        <p className="customers-state text-danger" role="alert">
          {loadError}
        </p>
      );
    }

    if (customers.length === 0) {
      return (
        <p className="customers-state" role="status">
          No customers registered.
        </p>
      );
    }

    if (filteredCustomers.length === 0) {
      return (
        <p className="customers-state" role="status">
          No customers match your filters.
        </p>
      );
    }

    return (
      <div className="customers-table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Customer Id</th>
              <th>First Name</th>
              <th>Last Name</th>
              <th>Address</th>
              <th>City</th>
              <th>Province</th>
              <th>Telephone</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {filteredCustomers.map(customer => (
              <tr key={customer.customerId}>
                <td>
                  <Link to={`/customers/${customer.customerId}`}>
                    {customer.customerId}
                  </Link>
                </td>
                <td>{customer.firstName}</td>
                <td>{customer.lastName}</td>
                <td>{customer.address}</td>
                <td>{customer.city}</td>
                <td>{customer.province}</td>
                <td>{customer.telephone}</td>
                <td>
                  {!isVet && (
                    <button
                      className="btn btn-danger"
                      onClick={() => handleDelete(customer.customerId)}
                      title="Delete"
                      style={{ backgroundColor: 'red', color: 'white' }}
                    >
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="32"
                        height="32"
                        fill="#fff"
                        className="bi bi-trash"
                        viewBox="0 0 16 16"
                      >
                        <path d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z" />
                        <path d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H6a1 1 0 0 1 1-1h3.5a1 1 0 0 1 1 1zM4.118 4 4 4.059V13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V4.059L11.882 4zM2.5 3h11V2h-11z" />
                      </svg>
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>{' '}
      </div>
    );
  };

  return (
    <div>
      <NavBar />

      <div className="customers-container">
        <h1>Customers</h1>
        <div className="customers-filter-bar">
          <button
            type="button"
            className="btn btn-primary customers-filter-toggle"
            onClick={() => setFilterVisible(!isFilterVisible)}
            aria-expanded={isFilterVisible}
            aria-controls="customer-filters"
          >
            {isFilterVisible ? 'Hide Filter' : 'Show Filter'}
          </button>

          {isFilterVisible && (
            <div id="customer-filters" className="customers-filters">
              {TEXT_FILTERS.map(({ key, label }) => (
                <Fragment key={key}>
                  <label htmlFor={`filter-${key}`} className="visually-hidden">
                    {label}
                  </label>
                  <input
                    id={`filter-${key}`}
                    type="text"
                    placeholder={label}
                    value={filter[key]}
                    onChange={e => updateFilter(key, e.target.value)}
                  />
                </Fragment>
              ))}

              <label htmlFor="filter-province" className="visually-hidden">
                Province
              </label>
              <select
                id="filter-province"
                value={filter.province}
                onChange={e => updateFilter('province', e.target.value)}
              >
                <option value="">All provinces</option>
                {provincesOfCanada.map(province => (
                  <option key={province} value={province}>
                    {province}
                  </option>
                ))}
              </select>
            </div>
          )}
        </div>
        {!isLoading && !loadError && (
          <div className="customers-filter-summary">
            <span role="status" aria-live="polite">
              Showing {filteredCustomers.length} of {customers.length} customers
            </span>
            {hasActiveFilters(filter) && (
              <button
                type="button"
                className="btn btn-outline-secondary btn-sm"
                onClick={clearFilters}
              >
                Clear filters
              </button>
            )}
          </div>
        )}

        {renderResults()}
      </div>
    </div>
  );
};

export default AllCustomers;
