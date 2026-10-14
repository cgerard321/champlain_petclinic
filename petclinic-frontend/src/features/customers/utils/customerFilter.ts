import type { CustomerResponseModel } from '@/features/customers/models/CustomerResponseModel.ts';

export interface CustomerFilter {
  customerId: string;
  firstName: string;
  lastName: string;
  address: string;
  city: string;
  province: string;
  telephone: string;
}

export const FILTER_KEYS: (keyof CustomerFilter)[] = [
  'customerId',
  'firstName',
  'lastName',
  'address',
  'city',
  'province',
  'telephone',
];

export const EMPTY_FILTER: CustomerFilter = {
  customerId: '',
  firstName: '',
  lastName: '',
  address: '',
  city: '',
  province: '',
  telephone: '',
};

const normalize = (value: string): string => value.trim().toLowerCase();
const digitsOnly = (value: string): string => value.replace(/\D/g, '');

function fieldMatches(
  key: keyof CustomerFilter,
  customerValue: string | null | undefined,
  filterValue: string
): boolean {
  const query = normalize(filterValue);
  if (query === '') return true;
  if (customerValue === null || customerValue === undefined) return false;

  if (key === 'telephone') {
    const queryDigits = digitsOnly(query);
    if (queryDigits !== '') {
      return digitsOnly(customerValue).includes(queryDigits);
    }
  }
  return normalize(String(customerValue)).includes(query);
}

export function filterCustomers(
  customers: CustomerResponseModel[],
  filter: CustomerFilter
): CustomerResponseModel[] {
  return customers.filter(customer =>
    FILTER_KEYS.every(key => fieldMatches(key, customer[key], filter[key]))
  );
}
export function hasActiveFilters(filter: CustomerFilter): boolean {
  return FILTER_KEYS.some(key => filter[key].trim() !== '');
}

export function filterFromSearchParams(
  params: URLSearchParams
): CustomerFilter {
  const filter = { ...EMPTY_FILTER };
  FILTER_KEYS.forEach(key => {
    filter[key] = params.get(key) ?? '';
  });
  return filter;
}
