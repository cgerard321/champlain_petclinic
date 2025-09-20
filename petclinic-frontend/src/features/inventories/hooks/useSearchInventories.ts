import { useState , useEffect, useCallback} from 'react';
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
    inventoryDescription: string
  ) => void;
  setCurrentPage: (currentPage: (prevPage: number) => number) => void;
  isLoading: boolean;
  updateFilters: (filters: {
    inventoryName?: string;
    inventoryType?: string;
    inventoryDescription?: string;
  }) => void;
  currentFilters: {
    inventoryName: string;
    inventoryType: string;
    inventoryDescription: string;
  };
}

export default function useSearchInventories(): useSearchInventoriesResponseModel {
  const [inventoryList, setInventoryList] = useState<Inventory[]>([]);
  const [currentPage, setCurrentPage] = useState<number>(0);
  const [realPage, setRealPage] = useState<number>(1);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [filters, setFilters] = useState({
    inventoryName: "",
    inventoryType: "",
    inventoryDescription: ""
  });
  const listSize: number = 10;

  const getInventoryList = async (
    inventoryName: string,
    inventoryType: string,
    inventoryDescription: string
  ): Promise<void> => {
    const data = await searchInventories(
      currentPage,
      listSize,
      inventoryName,
      inventoryType,
      inventoryDescription,
    );
    setInventoryList(data);
    setRealPage(currentPage + 1);
  };

  const updateFilters = useCallback((newFilters: {
    inventoryName?: string;
    inventoryType?: string;
    inventoryDescription?: string;
  }) => {
    setFilters((prev: typeof filters) => ({
      ...prev,
      ...newFilters
    }));
  }, []);

  useEffect(() => {
    setIsLoading(true);
    searchInventories(
        0,
        listSize,
        undefined,
        undefined,
        undefined
    ).then(data => {
      const clientFilteredData = data.filter(item => {
        const nameMatch = !filters.inventoryName ||
            item.inventoryName.toLowerCase().includes(filters.inventoryName.toLowerCase());
        const typeMatch = !filters.inventoryType ||
            item.inventoryType === filters.inventoryType;
        const descMatch = !filters.inventoryDescription ||
            (item.inventoryDescription || '').toLowerCase().trim().includes(filters.inventoryDescription.toLowerCase().trim());
        return nameMatch && typeMatch && descMatch;
      });

      setInventoryList(clientFilteredData);
      setRealPage(1);
      setCurrentPage(() => 0);
    }).catch(error => {
      console.error('Search failed:', error);
      setInventoryList([]);
    }).finally(() => {
      setIsLoading(false);
    });
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
