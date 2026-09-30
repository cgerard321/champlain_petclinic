export const validateUsername = (username: string): string | null => {
  if (!username.trim()) {
    return 'Username is required';
  }
  if (username.length < 3) {
    return 'Username must be at least 3 characters long';
  }
  if (username.length > 30) {
    return 'Username must be less than 30 characters';
  }
  if (!/^(?=.*[a-zA-Z])[a-zA-Z0-9_]+$/.test(username)) {
    return 'Username must contain at least one letter and can only contain letters, numbers, and underscores';
  }
  return null;
};

export const validateUsernameAvailability = async (
  username: string
): Promise<string | null> => {
  try {
    const { checkUsernameAvailability } = await import(
      '../api/checkUsernameAvailability'
    );
    const response = await checkUsernameAvailability(username);
    if (!response.data) {
      return 'Username is already taken';
    }
    return null;
  } catch (error: unknown) {
    console.error('Error checking username availability:', error);

    const isAxiosError = (
      err: unknown
    ): err is { response?: { status: number } } => {
      return typeof err === 'object' && err !== null && 'response' in err;
    };

    if (isAxiosError(error) && error.response?.status === 403) {
      return 'You do not have permission to check username availability';
    } else if (isAxiosError(error) && error.response?.status === 401) {
      return 'Please log in to check username availability';
    } else if (
      isAxiosError(error) &&
      error.response?.status &&
      error.response.status >= 500
    ) {
      return 'Server error. Please try again later';
    } else if (
      (typeof error === 'object' &&
        error !== null &&
        'code' in error &&
        (error as { code: string }).code === 'NETWORK_ERROR') ||
      !navigator.onLine
    ) {
      return 'Network error. Please check your connection';
    } else {
      return 'Unable to verify username availability. Please try again';
    }
  }
};
