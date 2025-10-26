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
    importantOnly?: boolean
  ) => void;
  setCurrentPage: (currentPage: (prevPage: number) => number) => void;
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

  const getInventoryList = async (
    inventoryName: string,
    inventoryType: string,
    inventoryDescription: string,
    importantOnly: boolean = false
  ): Promise<void> => {
    setIsLoading(true);
    setErrorMessage('');
    const res = await searchInventories(
      currentPage,
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
      setRealPage(currentPage + 1);
    }
    setIsLoading(false);
  };

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

    const fetchAllPages = async (runId: number): Promise<boolean | null> => {
      setIsLoading(true);
      setErrorMessage('');
      const aggregated: Inventory[] = [];
      const seen = new Set<string>();
      let page = 0;
      // Safety cap to avoid infinite loops if the backend misbehaves.
      const MAX_PAGES = 100;
      while (page < MAX_PAGES) {
        const res = await searchInventories(
          page,
          listSize,
          filters.inventoryName || undefined,
          filters.inventoryType || undefined,
          filters.inventoryDescription || undefined,
          filters.importantOnly
        );

        // if a newer run started, abort processing this one
        if (runId !== runIdRef.current) return null;

        if (res.errorMessage) {
          setInventoryList([]);
          setRealPage(1);
          setCurrentPage(() => 0);
          setIsLoading(false);
          setErrorMessage(res.errorMessage);
          return null;
        }

        const data = res.data ?? [];
        // Append unique by inventoryId using a Set for O(n) deduplication
        for (const item of data) {
          if (!seen.has(item.inventoryId)) {
            seen.add(item.inventoryId);
            aggregated.push(item);
          }
        }

        if (data.length < listSize) break;
        page += 1;
        if (page >= MAX_PAGES) {
          // Warning in case of unexpected infinite loop
          console.warn(
            `Reached MAX_PAGES (${MAX_PAGES}) while aggregating inventories; stopping to avoid an infinite loop.`
          );
          break;
        }
      }

      // double-check still current
      if (runId !== runIdRef.current) return null;

      // Server-side filtering already applied in the paged requests.
      // Use the aggregated results directly to avoid double-filtering
      // and extra CPU/network work. If we later find normalization gaps
      // we can add a lightweight normalization step here.
      setInventoryList(aggregated);
      setRealPage(1);
      setCurrentPage(() => 0);
      setIsLoading(false);
      return true;
    };

    debounceRef.current = window.setTimeout(async () => {
      // start a new run and capture its id
      runIdRef.current += 1;
      const myRun = runIdRef.current;
      await fetchAllPages(myRun);
    }, 300);

    return () => {
      // invalidate any in-flight run by bumping the run id
      runIdRef.current += 1;
      if (debounceRef.current) {
        window.clearTimeout(debounceRef.current);
      }
    };
  }, [filters, listSize]);

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
