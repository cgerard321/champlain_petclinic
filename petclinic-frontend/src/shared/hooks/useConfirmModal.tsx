// src/shared/hooks/useConfirmModal.tsx
import { useCallback, useEffect, useRef, useState, type FC } from 'react';

type ConfirmOptions = {
  title?: string;
  message?: React.ReactNode;
  confirmText?: string;
  cancelText?: string;
  variant?: 'danger' | 'primary';
  destructive?: boolean;
};

type InternalState = {
  open: boolean;
  opts: ConfirmOptions;
  resolver?: (v: boolean) => void;
};
type UseConfirmModalReturn = {
  confirm: (opts?: ConfirmOptions) => Promise<boolean>;
  ConfirmModal: React.FC;
};

export function useConfirmModal(): UseConfirmModalReturn {
  const [state, setState] = useState<InternalState>({ open: false, opts: {} });
  const lastFocused = useRef<HTMLElement | null>(null);
  const cancelBtnRef = useRef<HTMLButtonElement | null>(null);

  const hasDom: boolean =
    typeof window !== 'undefined' && typeof document !== 'undefined';

  const close = useCallback(
    (value: boolean): void => {
      setState(s => {
        if (s.resolver) s.resolver(value);
        return { open: false, opts: {}, resolver: undefined };
      });

      if (hasDom && lastFocused.current) {
        (lastFocused.current as HTMLElement)?.focus?.();
      }
    },
    [hasDom, setState]
  );

  const confirm = useCallback(
    (opts: ConfirmOptions = {}): Promise<boolean> => {
      if (hasDom) {
        lastFocused.current = document.activeElement as HTMLElement | null;
      }
      return new Promise<boolean>(resolve => {
        setState({ open: true, opts, resolver: resolve });
      });
    },
    [hasDom, setState]
  );

  const ConfirmDialog: FC = useCallback((): JSX.Element | null => {
    if (!state.open) return null;

    const {
      title = 'Are you sure?',
      message,
      confirmText = 'Yes',
      cancelText = 'No',
      destructive = false,
      variant,
    } = state.opts;

    const isDanger = destructive || variant === 'danger';

    return (
      <div
        role="dialog"
        aria-modal="true"
        className="confirm-backdrop"
        onClick={() => close(false)}
        style={{
          position: 'fixed',
          inset: 0,
          background: 'rgba(0,0,0,0.35)',
          display: 'grid',
          placeItems: 'center',
          zIndex: 1000,
        }}
      >
        <div
          className="confirm-card"
          onClick={e => e.stopPropagation()}
          style={{
            width: 'min(520px, 92vw)',
            background: '#fff',
            borderRadius: 12,
            boxShadow: '0 10px 30px rgba(0,0,0,.2)',
            padding: 20,
          }}
        >
          <h3 style={{ margin: '0 0 8px 0', fontSize: 18, fontWeight: 600 }}>
            {title}
          </h3>

          {message && <div style={{ marginBottom: 16 }}>{message}</div>}

          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <button
              ref={cancelBtnRef}
              type="button"
              onClick={() => close(false)}
              style={{
                padding: '10px 20px',
                borderRadius: 6,
                border: 'none',
                background: 'gray',
                color: 'white',
                fontWeight: 'bold',
                cursor: 'pointer',
                transition: 'background-color 0.3s, transform 0.2s',
              }}
            >
              {cancelText}
            </button>
            <button
              type="button"
              onClick={() => close(true)}
              style={{
                padding: '8px 12px',
                borderRadius: 8,
                border: '1px solid transparent',
                background: isDanger ? '#e53935' : '#0d6efd',
                color: '#fff',
                cursor: 'pointer',
              }}
            >
              {confirmText}
            </button>
          </div>
        </div>
      </div>
    );
  }, [state.open, state.opts, close]);

  useEffect(() => {
    if (!state.open || !hasDom) return;

    const onKey = (e: KeyboardEvent): void => {
      if (e.key === 'Escape') close(false);
    };

    window.addEventListener('keydown', onKey);
    cancelBtnRef.current?.focus?.();

    return () => window.removeEventListener('keydown', onKey);
  }, [state.open, hasDom, close]);

  return { confirm, ConfirmModal: ConfirmDialog };
}
