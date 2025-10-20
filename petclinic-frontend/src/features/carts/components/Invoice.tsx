import { useEffect } from 'react';
import './Invoice.css';

export interface InvoiceItem {
  productId: number;
  productName: string;
  productSalePrice: number;
  quantity: number;
}

export interface BillingStored {
  fullName: string;
  email: string;
  address: string;
  city: string;
  province?: string;
  postalCode: string;
}

export interface InvoiceFull {
  invoiceId: string;
  userId: string;
  billing: BillingStored | null;
  date: string; // ISO
  items: InvoiceItem[];
  subtotal: number;
  tvq: number;
  tvc: number;
  discount: number;
  total: number;
}

type Props = {
  invoices: InvoiceFull[];
  index: number;
  onIndexChange: (i: number) => void;
  onClose: () => void;
};

function Invoice({
  invoices,
  index,
  onIndexChange,
  onClose,
}: Props): JSX.Element | null {
  useEffect(() => {
    const onKey = (e: KeyboardEvent): void => {
      if (e.key === 'Escape') onClose();
      if (e.key === 'ArrowLeft') onIndexChange(Math.max(0, index - 1));
      if (e.key === 'ArrowRight')
        onIndexChange(Math.min(invoices.length - 1, index + 1));
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [index, invoices.length, onClose, onIndexChange]);

  if (
    !invoices ||
    invoices.length === 0 ||
    index < 0 ||
    index >= invoices.length
  ) {
    return null;
  }

  const inv = invoices[index];

  return (
    <div
      className="invoice-modal-backdrop"
      onClick={onClose}
      role="dialog"
      aria-modal="true"
      aria-label="Invoice receipt"
    >
      <div className="invoice-modal-content" onClick={e => e.stopPropagation()}>
        <div className="invoice-modal-header" />

        <div className="invoice-body">
          {inv.billing && (
            <div className="invoice-client-info">
              <h3>Client Information</h3>
              <p>
                <strong>Name:</strong> {inv.billing.fullName}
              </p>
              <p>
                <strong>Email:</strong> {inv.billing.email}
              </p>
              <p>
                <strong>Address:</strong> {inv.billing.address}
              </p>
              <p>
                <strong>City:</strong> {inv.billing.city}
              </p>
              <p>
                <strong>Province:</strong> {inv.billing.province}
              </p>
              <p>
                <strong>Postal code:</strong> {inv.billing.postalCode}
              </p>
            </div>
          )}

          <div className="invoice-date">
            <strong>Date:</strong> {new Date(inv.date).toLocaleString()}
          </div>

          <h3>Items</h3>
          {inv.items.map((item, idx) => (
            <div
              key={`${item.productId ?? 'item'}-${idx}`}
              className="invoice-card"
            >
              <h4>{item.productName}</h4>
              <p>Price: ${item.productSalePrice.toFixed(2)}</p>
              <p>Quantity: {item.quantity}</p>
              <p>
                Total: ${(item.productSalePrice * item.quantity).toFixed(2)}
              </p>
            </div>
          ))}

          <div className="invoice-taxes">
            <p>Subtotal: ${inv.subtotal.toFixed(2)}</p>
            <p>TVQ (9.975%): ${inv.tvq.toFixed(2)}</p>
            <p>TVC (5%): ${inv.tvc.toFixed(2)}</p>
            <p>Discount: -${inv.discount.toFixed(2)}</p>
            <h3>Total: ${inv.total.toFixed(2)}</h3>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Invoice;
