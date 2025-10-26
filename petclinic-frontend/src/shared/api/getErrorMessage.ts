import axios from 'axios';

type Opts = {
  defaultMessage?: string;
  log?: boolean; // defaults to true (console.debug)
};

type AppLogger = {
  debug?: (payload?: unknown) => void;
};

type GlobalWithAppLog = {
  __APP_LOG__?: AppLogger;
};

const logger = {
  debug: (payload?: unknown): void => {
    try {
      const g = globalThis as unknown as GlobalWithAppLog;
      if (g && g.__APP_LOG__ && typeof g.__APP_LOG__.debug === 'function') {
        g.__APP_LOG__.debug(payload);
      }
    } catch {
      /* swallow errors - no console usage to satisfy lint */
    }
  },
};

export default function getErrorMessage(err: unknown, opts?: Opts): string {
  const defaultMessage = opts?.defaultMessage ?? 'An unexpected error occurred';
  const doLog = opts?.log !== false;

  try {
    if (axios.isAxiosError(err)) {
      const data = err.response?.data as
        | { message?: string; error?: string; title?: string }
        | undefined;
      const msg =
        (data && (data.message || data.error || data.title)) ||
        err.message ||
        defaultMessage;
      if (doLog) logger.debug({ when: 'axios-error', err });
      return msg;
    }
  } catch (e) {
    if (doLog) logger.debug({ when: 'axios-detect-failed', e });
  }

  if (err instanceof Error) {
    if (doLog) logger.debug({ when: 'error-object', err });
    return err.message || defaultMessage;
  }

  if (typeof err === 'string') {
    if (doLog) logger.debug({ when: 'string-error', err });
    return err;
  }

  if (doLog) logger.debug({ when: 'unknown-error', err });
  return defaultMessage;
}
