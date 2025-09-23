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
}

export default function useSearchInventories(): useSearchInventoriesResponseModel {
  const [inventoryList, setInventoryList] = useState<Inventory[]>([]);
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [realPage, setRealPage] = useState<number>(1);
  const [isLoading, setIsLoading] = useState<boolean>(false);
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
    importantOnly: boolean = false,
  ): Promise<void> => {
    const data = await searchInventories(
      currentPage,
      listSize,
      inventoryName,
      inventoryType,
      inventoryDescription,
        importantOnly,
    );
    setInventoryList(data);
    setRealPage(currentPage + 1);
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

  const debounceRef = useRef<number>();

  useEffect(() => {
    if (debounceRef.current) {
      clearTimeout(debounceRef.current);
    }

    debounceRef.current = setTimeout(async () => {
      setIsLoading(true);
      try {
        const data = await searchInventories(
          0,
          listSize,
          filters.inventoryName || undefined,
          filters.inventoryType || undefined,
          filters.inventoryDescription || undefined,
            filters.importantOnly || undefined,
        );
        setInventoryList(data);
        setRealPage(1);
        setCurrentPage(() => 0);
      } catch (error) {
        console.error('Search failed:', error);
        setInventoryList([]);
      } finally {
        setIsLoading(false);
      }
    }, 300);

    return () => {
      if (debounceRef.current) {
        clearTimeout(debounceRef.current);
      }
    };
  }, [filters]);

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
  };
}
