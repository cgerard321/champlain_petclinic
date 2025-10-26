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

  useEffect(() => {
    if (debounceRef.current) {
      window.clearTimeout(debounceRef.current);
    }

    let cancelled = false;

    const fetchAllPages = async (): Promise<boolean | null> => {
      setIsLoading(true);
      setErrorMessage('');
      const aggregated: Inventory[] = [];
      let page = 0;
      while (true) {
        const res = await searchInventories(
          page,
          listSize,
          filters.inventoryName || undefined,
          filters.inventoryType || undefined,
          filters.inventoryDescription || undefined,
          filters.importantOnly
        );

        if (cancelled) return null;

        if (res.errorMessage) {
          setInventoryList([]);
          setRealPage(1);
          setCurrentPage(() => 0);
          setIsLoading(false);
          setErrorMessage(res.errorMessage);
          return null;
        }

        const data = res.data ?? [];
        // Append unique by inventoryId
        for (const item of data) {
          if (!aggregated.some(a => a.inventoryId === item.inventoryId)) {
            aggregated.push(item);
          }
        }

        if (data.length < listSize) break;
        page += 1;
      }

      if (cancelled) return null;

      // apply client-side filters as an extra safety (server-side already
      // received the filters but some fields may be normalized differently)
      const filtered = aggregated.filter(item => {
        const nameMatch =
          !filters.inventoryName ||
          item.inventoryName
            .toLowerCase()
            .includes(filters.inventoryName.toLowerCase());

        const typeMatch =
          !filters.inventoryType ||
          item.inventoryType === filters.inventoryType;

        const descMatch =
          !filters.inventoryDescription ||
          (item.inventoryDescription || '')
            .toLowerCase()
            .includes(filters.inventoryDescription.toLowerCase());

        const importantMatch =
          !filters.importantOnly || item.important === true;

        return nameMatch && typeMatch && descMatch && importantMatch;
      });

      setInventoryList(filtered);
      setRealPage(1);
      setCurrentPage(() => 0);
      setIsLoading(false);
      return true;
    };

    debounceRef.current = window.setTimeout(async () => {
      await fetchAllPages();
    }, 300);

    return () => {
      cancelled = true;
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
