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
  refreshAllInventories: () => Promise<void>;
  errorMessage?: string;
}

export default function useSearchInventories(): useSearchInventoriesResponseModel {
  const [inventoryList, setInventoryList] = useState<Inventory[]>([]);
  const [allInventories, setAllInventories] = useState<Inventory[]>([]);
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

  // Fetch all inventory pages and store in allInventories
  const refreshAllInventories = useCallback(async (): Promise<void> => {
    setIsLoading(true);
    setErrorMessage('');
    let page = 0;
    let allItems: Inventory[] = [];
    let keepFetching = true;
    while (keepFetching) {
      const res = await searchInventories(page, listSize);
      if (res.errorMessage) {
        setErrorMessage(res.errorMessage);
        break;
      }
      const data = res.data ?? [];
      allItems = allItems.concat(data);
      if (data.length < listSize) {
        keepFetching = false;
      } else {
        page += 1;
      }
    }
    setAllInventories(allItems);
    setIsLoading(false);
  }, [listSize]);

  useEffect(() => {
    void refreshAllInventories();
  }, [refreshAllInventories]);

  // Reset to first page whenever filters change
  useEffect(() => {
    setCurrentPage(() => 0);
  }, [
    filters.inventoryName,
    filters.inventoryType,
    filters.inventoryDescription,
    filters.importantOnly,
  ]);

  // Filter full dataset and expose only the current page slice
  useEffect(() => {
    if (debounceRef.current) {
      window.clearTimeout(debounceRef.current);
    }
    debounceRef.current = window.setTimeout(() => {
      const normalizedName = (filters.inventoryName || '').trim().toLowerCase();
      const rawType = (filters.inventoryType || '').trim();
      const normalizedType = rawType.toLowerCase() === 'none' ? '' : rawType;
      const normalizedDesc = (filters.inventoryDescription || '')
        .trim()
        .toLowerCase();

      const filtered = allInventories.filter(item => {
        const itemNameLc = (item.inventoryName || '').toLowerCase();
        const nameMatch =
          !normalizedName || itemNameLc.includes(normalizedName);

        const typeMatch =
          !normalizedType || item.inventoryType === normalizedType;

        const itemDescLc = (item.inventoryDescription || '').toLowerCase();
        const descMatch =
          !normalizedDesc || itemDescLc.includes(normalizedDesc);

        const importantMatch =
          !filters.importantOnly || item.important === true;

        return nameMatch && typeMatch && descMatch && importantMatch;
      });
      const start = currentPage * listSize;
      const end = start + listSize;
      const pageSlice = filtered.slice(start, end);
      setInventoryList(pageSlice);
      setRealPage(currentPage + 1);
    }, 300);
    return () => {
      if (debounceRef.current) {
        window.clearTimeout(debounceRef.current);
      }
    };
  }, [filters, allInventories, currentPage, listSize]);

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
    refreshAllInventories,
    errorMessage,
  };
}
