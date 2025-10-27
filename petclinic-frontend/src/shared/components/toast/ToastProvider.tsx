import { createContext, useContext, useMemo } from 'react';
import type { PropsWithChildren } from 'react';
import { Toaster, toast } from 'sonner';

type ToastKind = 'info' | 'success' | 'error';

type ToastApi = {
  showToast: (message: string, type?: ToastKind, durationMs?: number) => void;
};

const ToastCtx = createContext<ToastApi | null>(null);

// Helper to map kind to bar color using your own CSS variables
function barColorFor(kind: ToastKind): string {
  switch (kind) {
    case 'success':
      return 'var(--cart-accent, #22c55e)'; // green
    case 'error':
      return 'var(--cart-danger, #ef4444)'; // red
    default:
      return 'var(--cart-brand, #2563eb)'; // brand blue for info
  }
}

// Helper message renderer with progress bar
function ToastWithProgress({
  message,
  kind,
  durationMs,
}: {
  message: string;
  kind: ToastKind;
  durationMs: number;
}): JSX.Element {
  const bar = barColorFor(kind);
  return (
    <div className="pc-toast-wrap">
      <span className="pc-toast-text">{message}</span>
      <div className="pc-toast-track" aria-hidden="true">
        <div
          className="pc-toast-bar"
          style={{ background: bar, animationDuration: `${durationMs}ms` }}
        />
      </div>
    </div>
  );
}

export function ToastProvider({ children }: PropsWithChildren): JSX.Element {
  const api = useMemo<ToastApi>(
    () => ({
      showToast: (message, type = 'info', durationMs = 4000) => {
        const content = (
          <ToastWithProgress
            message={message}
            kind={type}
            durationMs={durationMs}
          />
        );

        // Neutral card styling comes from toastOptions below, here we just choose the variant
        const opt = { duration: durationMs };
        if (type === 'success') return toast.success(content, opt);
        if (type === 'error') return toast.error(content, opt);
        return toast(content, opt);
      },
    }),
    []
  );

  return (
    <>
      <Toaster
        position="top-center"
        closeButton
        toastOptions={{
          className: 'pc-toast', // for targeting if you ever need to
          style: {
            background: 'var(--cart-surface, #fff)',
            color: 'var(--cart-text, #0f172a)',
            border: '1px solid var(--cart-ring, #e5e7eb)',
            boxShadow: 'var(--cart-shadow, 0 12px 32px rgba(15, 23, 42, 0.08))',
            borderRadius: '12px',
            padding: '12px 14px',
          },
        }}
      />

      <style>{`
        /* Wrapper so message sits over a progress track */
        .pc-toast-wrap {
          display: flex;
          flex-direction: column;
          gap: 8px;
          width: 100%
        }

        .pc-toast-text {
          color: var(--cart-text, #0f172a);
          font-size: 0.95rem;
          line-height: 1.35;
        }

        .pc-toast-track {
          position: relative;
          height: 4px;
          background: var(--cart-ring, #e5e7eb);
          border-radius: 999px;
          overflow: hidden;
          align-self: stretch;
          width: 100%;
        }

        /* Bar shrinks from full width to 0 over the toast lifetime */
        .pc-toast-bar {
          position: absolute;
          left: 0; top: 0; bottom: 0;
          width: 100%;
          animation-name: pc-toast-shrink;
          animation-timing-function: linear;
          animation-fill-mode: forwards;
        }

        @keyframes pc-toast-shrink { from { width: 100%; } to { width: 0%; } }

        .sonner-toast button[aria-label="Close"] {
          outline: none;
        }
        .sonner-toast button[aria-label="Close"]:focus-visible {
          box-shadow: 0 0 0 3px color-mix(in srgb, var(--cart-ring, #e5e7eb) 60%, transparent);
          border-radius: 6px;
        }
      `}</style>

      <ToastCtx.Provider value={api}>{children}</ToastCtx.Provider>
    </>
  );
}

export function useToast(): ToastApi {
  const ctx = useContext(ToastCtx);
  if (!ctx) throw new Error('useToast must be used within <ToastProvider>');
  return ctx;
}
