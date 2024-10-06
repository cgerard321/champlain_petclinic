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
        const bills = await getAllBillsPaginated(page, size);

        setBillsList(bills);

        if (bills.length === size) {
          setHasMore(true);
        } else {
          setHasMore(false);
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
