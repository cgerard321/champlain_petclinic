import { useState, useCallback } from 'react';
import { getAllBillsPaginated } from '../api/getAllBillsPaginated';
import { Bill } from '../models/Bill';

interface UseGetAllBillsPaginatedReturn {
  billsList: Bill[];
  getBillsList: (
    page: number,
    size: number,
    billId?: string,
    customerId?: string,
    ownerFirstName?: string,
    ownerLastName?: string,
    visitType?: string,
    vetId?: string,
    vetFirstName?: string,
    vetLastName?: string
  ) => Promise<void>;
  setCurrentPage: (page: number) => void;
  currentPage: number;
  hasMore: boolean;
}

export default function useGetAllBillsPaginated(): UseGetAllBillsPaginatedReturn {
  const [billsList, setBillsList] = useState<Bill[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);

  const getBillsList = useCallback(
    async (
      page: number,
      size: number,
      billId?: string,
      customerId?: string,
      ownerFirstName?: string,
      ownerLastName?: string,
      visitType?: string,
      vetId?: string,
      vetFirstName?: string,
      vetLastName?: string
    ): Promise<void> => {
      try {
        const bills = await getAllBillsPaginated(
          page,
          size,
          billId,
          customerId,
          ownerFirstName,
          ownerLastName,
          visitType,
          vetId,
          vetFirstName,
          vetLastName
        );

        // NEW: always replace with the fetched page (don't append)
        setBillsList(bills);

        // keep currentPage in sync with the request
        setCurrentPage(page);

        // if the returned page is full, there may be more
        setHasMore(bills.length === size);
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
