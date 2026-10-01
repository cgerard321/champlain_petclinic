export const validateUsername = (username: string): string | null => {
  if (!username.trim()) {
    return 'validation.usernameRequired';
  }
  if (username.length < 3) {
    return 'validation.usernameTooShort';
  }
  if (username.length > 30) {
    return 'validation.usernameTooLong';
  }
  if (!/^(?=.*[a-zA-Z])[a-zA-Z0-9_]+$/.test(username)) {
    return 'validation.usernameInvalid';
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
      return 'validation.usernameTaken';
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
      return 'validation.noPermission';
    } else if (isAxiosError(error) && error.response?.status === 401) {
      return 'validation.loginRequired';
    } else if (
      isAxiosError(error) &&
      error.response?.status &&
      error.response.status >= 500
    ) {
      return 'validation.serverError';
    } else if (
      (typeof error === 'object' &&
        error !== null &&
        'code' in error &&
        (error as { code: string }).code === 'NETWORK_ERROR') ||
      !navigator.onLine
    ) {
      return 'validation.networkError';
    } else {
      return 'validation.usernameCheckFailed';
    }
  }
};
export const validateTelephone = (telephone: string): string | null => {
  if (!telephone.trim()) {
    return 'validation.telephoneRequired';
  }
  if (!/^[0-9]{10}$/.test(telephone)) {
    return 'validation.telephoneTenDigits';
  }
  return null;
};
