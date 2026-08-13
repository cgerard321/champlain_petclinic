import { useState, useEffect, useCallback, useRef } from 'react';
import { searchInventories } from '@/features/inventories/api/searchInventories.ts';
import { Inventory } from '@/features/inventories/models/Inventory.ts';

interface useSearchInventoriesResponseModel {
  inventoryList: Inventory[];
  setInventoryList: (inventoryList: Inventory[]) => void;
  currentPage: number;
  realPage: number;
  getInventoryList: (
    inventoryName: string,
    inventoryType: string,
    inventoryDescription: string,
    importantOnly?: boolean,
    page?: number
  ) => void;
  setCurrentPage: (value: number | ((prevPage: number) => number)) => void;
  isLoading: boolean;
  updateFilters: (filters: {
    inventoryName?: string;
    inventoryType?: string;
    inventoryDescription?: string;
    importantOnly?: boolean;
  }) => void;
  currentFilters: {
    inventoryName: string;
    inventoryType: string;
    inventoryDescription: string;
    // currentInventory: boolean;
    importantOnly?: boolean;
  };
  errorMessage?: string;
}

export default function useSearchInventories(): useSearchInventoriesResponseModel {
  const [inventoryList, setInventoryList] = useState<Inventory[]>([]);
  const [currentPage, setCurrentPage] = useState<number>(0);
  const currentPageRef = useRef<number>(currentPage);
  const [realPage, setRealPage] = useState<number>(1);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string>('');
  const [filters, setFilters] = useState({
    inventoryName: '',
    inventoryType: '',
    inventoryDescription: '',
    importantOnly: false,
  });
  const listSize: number = 10;

  const getInventoryList = useCallback(
    async (
      inventoryName: string,
      inventoryType: string,
      inventoryDescription: string,
      importantOnly: boolean = false,
      page?: number
    ): Promise<void> => {
      setIsLoading(true);
      setErrorMessage('');
      // Use the ref to avoid recreating this callback when `currentPage` state changes.
      const pageToFetch =
        typeof page === 'number' ? page : currentPageRef.current;
      const res = await searchInventories(
        pageToFetch,
        listSize,
        inventoryName,
        inventoryType,
        inventoryDescription,
        importantOnly
      );
      if (res.errorMessage) {
        setInventoryList([]);
        setErrorMessage(res.errorMessage);
      } else {
        setInventoryList(res.data ?? []);
        setRealPage(pageToFetch + 1);
        setCurrentPage(() => pageToFetch);
      }
      setIsLoading(false);
    },
    [listSize]
  );

  // keep ref in sync with state; this avoids recreating getInventoryList when the page changes
  useEffect(() => {
    currentPageRef.current = currentPage;
  }, [currentPage]);

  const updateFilters = useCallback(
    (newFilters: {
      inventoryName?: string;
      inventoryType?: string;
      inventoryDescription?: string;
      importantOnly?: boolean;
    }) => {
      setFilters((prev: typeof filters) => ({
        ...prev,
        ...newFilters,
      }));
    },
    []
  );

  const debounceRef = useRef<number | null>(null);
  // runIdRef is used to invalidate in-flight fetches when filters change.
  // Each fetch run captures a unique id; if that id no longer matches the
  // current ref value the response is discarded. This avoids closure-based
  // cancellation race conditions.
  const runIdRef = useRef<number>(0);

  useEffect(() => {
    if (debounceRef.current) {
      window.clearTimeout(debounceRef.current);
    }

    debounceRef.current = window.setTimeout(async () => {
      // start a new run id (used only to invalidate overlapping calls)
      runIdRef.current += 1;

      // Fetch only the first page for new filters instead of aggregating all pages.
      // This preserves the paged UI expectation and avoids duplicating page data.
      await getInventoryList(
        filters.inventoryName || '',
        filters.inventoryType || '',
        filters.inventoryDescription || '',
        filters.importantOnly ?? false,
        0
      );
    }, 300);

    return () => {
      // invalidate any in-flight run by bumping the run id
      runIdRef.current += 1;
      if (debounceRef.current) {
        window.clearTimeout(debounceRef.current);
      }
    };
  }, [filters, listSize, getInventoryList]);

  return {
    inventoryList,
    setInventoryList,
    currentPage,
    realPage,
    getInventoryList,
    setCurrentPage,
    isLoading,
    updateFilters,
    currentFilters: filters,
    errorMessage,
  };
}
