import { useState, useCallback } from 'react';
import { getAllBillsPaginated } from '../api/getAllBillsPaginated';
import { Bill } from '../models/Bill';

interface UseGetAllBillsPaginatedReturn {
  billsList: Bill[];
  getBillsList: (page: number, size: number) => Promise<void>;
  setCurrentPage: (page: number) => void;
  currentPage: number;
  hasMore: boolean;
}

export default function useGetAllBillsPaginated(): UseGetAllBillsPaginatedReturn {
  const [billsList, setBillsList] = useState<Bill[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);

  const getBillsList = useCallback(
    async (page: number, size: number): Promise<void> => {
      try {
        // Fetch the exact number of bills for the current page
        const bills = await getAllBillsPaginated(page, size);

        // Update the bills list
        setBillsList(bills);

        // If the number of bills fetched equals the page size, assume there's more data
        if (bills.length === size) {
          setHasMore(true);
        } else {
          setHasMore(false); // No more pages if less than the page size is fetched
        }
      } catch (error) {
        console.error('Error fetching bills:', error);
      }
    },
    []
  );

  return {
    billsList,
    getBillsList,
    setCurrentPage,
    currentPage,
    hasMore,
  };
}
