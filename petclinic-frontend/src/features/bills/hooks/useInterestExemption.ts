import { useState } from 'react';
import { setInterestExempt } from '../api/setInterestExempt';

interface UseInterestExemptionResult {
  isLoading: boolean;
  toggleExemption: (billId: string, currentStatus: boolean) => Promise<void>;
  error: string | null;
  clearError: () => void;
}

export function useInterestExemption(): UseInterestExemptionResult {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const toggleExemption = async (
    billId: string,
    currentStatus: boolean
  ): Promise<void> => {
    setIsLoading(true);
    setError(null);

    try {
      await setInterestExempt(billId, !currentStatus);
    } catch (err) {
      const errorMessage =
        err instanceof Error
          ? err.message
          : 'Failed to update interest exemption';
      setError(errorMessage);
      throw err;
    } finally {
      setIsLoading(false);
    }
  };

  const clearError = (): void => {
    setError(null);
  };

  return {
    isLoading,
    toggleExemption,
    error,
    clearError,
  };
}
