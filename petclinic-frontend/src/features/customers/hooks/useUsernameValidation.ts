import { useState, useCallback } from 'react';
import {
  validateUsername,
  validateUsernameAvailability,
} from '../utils/validation';

interface UseUsernameValidationReturn {
  validateUsernameField: (
    username: string,
    currentUsername?: string
  ) => Promise<string | null>;
  isValidationInProgress: boolean;
}

export const useUsernameValidation = (): UseUsernameValidationReturn => {
  const [isValidationInProgress, setIsValidationInProgress] = useState(false);

  const validateUsernameField = useCallback(
    async (
      username: string,
      currentUsername?: string
    ): Promise<string | null> => {
      const basicError = validateUsername(username);
      if (basicError) {
        return basicError;
      }

      if (currentUsername && username === currentUsername) {
        return null;
      }

      setIsValidationInProgress(true);
      try {
        const availabilityError = await validateUsernameAvailability(username);
        return availabilityError;
      } finally {
        setIsValidationInProgress(false);
      }
    },
    []
  );

  return {
    validateUsernameField,
    isValidationInProgress,
  };
};
