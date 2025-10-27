import { useEffect } from 'react';

interface AutoDismissAlertProps {
  show: boolean;
  message: string;
  type?: 'success' | 'error' | 'info';
  duration?: number; // ms
  onClose: () => void;
}

const baseStyle: React.CSSProperties = {
  position: 'fixed',
  top: '8%', // moved to top area
  left: '50%',
  transform: 'translateX(-50%)', // center horizontally only
  zIndex: 2000,
  minWidth: '320px',
  maxWidth: '80vw',
  padding: '1rem 1.25rem',
  borderRadius: '8px',
  color: '#fff',
  boxShadow: '0 6px 18px rgba(0,0,0,0.25)',
  textAlign: 'center',
  pointerEvents: 'auto',
  transition: 'opacity 240ms ease, transform 240ms ease',
};

const typeStyles: Record<string, React.CSSProperties> = {
  success: { backgroundColor: '#28a745' },
  error: { backgroundColor: '#dc3545' },
  info: { backgroundColor: '#17a2b8' },
};

export default function AutoDismissAlert({
  show,
  message,
  type = 'info',
  duration = 5000,
  onClose,
}: AutoDismissAlertProps): JSX.Element | null {
  useEffect(() => {
    if (!show) return;
    const t = setTimeout(() => onClose(), duration);
    return () => clearTimeout(t);
  }, [show, duration, onClose]);

  if (!show) return null;

  return (
    <div
      role="alert"
      aria-live="polite"
      style={{ ...baseStyle, ...typeStyles[type] }}
    >
      {message}
    </div>
  );
}
