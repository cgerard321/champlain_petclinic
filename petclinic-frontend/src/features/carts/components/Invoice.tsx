import { useEffect } from 'react';
import './Invoice.css';
import { computeTaxes, formatTaxRate, roundToCents } from '../utils/taxUtils';

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
  interface WindowWithHtml2Pdf extends Window {
    html2pdf?: unknown;
  }
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

  const downloadInvoice = async (): Promise<void> => {
    // Build smaller pieces to avoid nested template literals
    const billingHtml = inv.billing
      ? `<p><strong>Name:</strong> ${inv.billing.fullName}</p>
         <p><strong>Email:</strong> ${inv.billing.email}</p>
         <p><strong>Address:</strong> ${inv.billing.address}</p>
         <p><strong>City:</strong> ${inv.billing.city}</p>
         <p><strong>Province:</strong> ${inv.billing.province ?? ''}</p>
         <p><strong>Postal code:</strong> ${inv.billing.postalCode}</p>`
      : '<p>Customer information not available</p>';

    const itemsHtml = inv.items
      .map(it => {
        const unitCents = Math.round(it.productSalePrice * 100);
        const lineCents = unitCents * it.quantity;
        const unit = (unitCents / 100).toFixed(2);
        const line = (lineCents / 100).toFixed(2);
        return `
          <tr>
            <td>${it.productName}</td>
            <td>${it.quantity}</td>
            <td>$${unit}</td>
            <td>$${line}</td>
          </tr>`;
      })
      .join('');

    // Build a clean HTML representation of the invoice for printing / PDF
    const invoiceHtml = `<!doctype html>
      <html>
      <head>
        <meta charset="utf-8" />
        <title>Invoice ${inv.invoiceId}</title>
        <style>
          body { font-family: Arial, Helvetica, sans-serif; margin: 24px; color: #222; }
          .header { text-align: center; margin-bottom: 20px; }
          .title { color: #0b5fff; font-size: 28px; margin: 0; }
          .meta { margin-top: 8px; font-size: 12px; color: #555; }
          .section { margin-top: 18px; }
          .client p { margin: 4px 0; }
          table { width: 100%; border-collapse: collapse; margin-top: 8px; }
          th, td { padding: 8px 10px; border: 1px solid #ddd; text-align: left; }
          th { background: #f7f7f7; }
          .totals { margin-top: 12px; float: right; width: 320px; }
          .totals p { display:flex; justify-content:space-between; margin:6px 0; }
          .total { font-size: 18px; font-weight: 700; }
          .footer { margin-top: 40px; font-size: 12px; color: #444; }
          @media print {
            body { margin: 12mm; }
          }
        </style>
      </head>
      <body>
        <div class="header">
          <h1 class="title">Champlain Pet Clinic</h1>
          <div class="meta">Invoice ID: ${inv.invoiceId} · Date: ${new Date(inv.date).toLocaleString()}</div>
        </div>

        <div class="section client">
          <h3>Customer</h3>
          ${billingHtml}
        </div>

        <div class="section">
          <h3>Items</h3>
          <table>
            <thead>
              <tr><th>Description</th><th>Quantity</th><th>Unit</th><th>Line Total</th></tr>
            </thead>
            <tbody>
              ${itemsHtml}
            </tbody>
          </table>
        </div>

        <div class="totals">
          <p><span>Subtotal</span><span>$${inv.subtotal.toFixed(2)}</span></p>
          ${(() => {
            // compute tax lines if billing province available
            try {
              const prov = inv.billing?.province;
              if (prov) {
                const lines = computeTaxes(inv.subtotal, prov);
                return lines
                  .map(
                    l =>
                      `<p><span>${l.name} (${formatTaxRate(l.rate)}%)</span><span>$${(l.amount ?? roundToCents(inv.subtotal * l.rate)).toFixed(2)}</span></p>`
                  )
                  .join('');
              }
            } catch (e) {
              // Log tax computation errors to help debugging while falling back
              // to legacy invoice fields.
              console.error('Tax calculation failed for printable invoice:', e);
            }
            // fallback to legacy fields
            return `<p><span>TVQ (9.975%)</span><span>$${inv.tvq.toFixed(2)}</span></p><p><span>TVC (5%)</span><span>$${inv.tvc.toFixed(2)}</span></p>`;
          })()}
          <p><span>Discount</span><span>-$${inv.discount.toFixed(2)}</span></p>
          <p class="total"><span>Total</span><span>$${inv.total.toFixed(2)}</span></p>
        </div>

        <div style="clear:both"></div>

        <div class="footer">
          <h4>Payment terms</h4>
          <p>Please make payment within 30 days. For any questions regarding this invoice, contact Champlain Pet Clinic at (555) 555-5555 or billing@champlainpetclinic.example</p>
        </div>
      </body>
      </html>`;

    // Try to load html2pdf.js at runtime (bundled html2canvas + jsPDF) and generate a PDF
    try {
      await new Promise<void>((resolve, reject) => {
        const win = window as WindowWithHtml2Pdf;
        if (win.html2pdf) return resolve();
        const s = document.createElement('script');
        s.src =
          'https://unpkg.com/html2pdf.js@0.9.3/dist/html2pdf.bundle.min.js';
        s.onload = () => resolve();
        s.onerror = () => reject(new Error('Failed to load html2pdf'));
        document.head.appendChild(s);
      });

      const el =
        document.getElementById(`invoice-print-${inv.invoiceId}`) ||
        document.querySelector('.invoice-modal-content');
      if (!el) throw new Error('Invoice content not found');

      const opt = {
        margin: 12,
        filename: `invoice-${inv.invoiceId}.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2 },
        jsPDF: { unit: 'pt', format: 'a4', orientation: 'portrait' },
      };

      const win = window as WindowWithHtml2Pdf;
      if (win.html2pdf && typeof win.html2pdf === 'function') {
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const html2pdfFn = win.html2pdf as unknown as any;
        html2pdfFn().set(opt).from(el).save();
      }
      return;
    } catch (err) {
      // Fallback: inject an invisible iframe, write HTML into it and call print
      try {
        const iframe = document.createElement('iframe');
        iframe.style.position = 'fixed';
        iframe.style.right = '0';
        iframe.style.bottom = '0';
        iframe.style.width = '0px';
        iframe.style.height = '0px';
        iframe.style.border = '0';
        document.body.appendChild(iframe);
        const win = iframe.contentWindow;
        const idoc =
          win?.document || (iframe.contentDocument as Document | null);
        if (!idoc) throw new Error('Iframe not accessible');
        idoc.open();
        idoc.write(invoiceHtml);
        idoc.close();
        // Wait for content to render then print and remove iframe
        setTimeout(() => {
          try {
            if (win) {
              win.focus();
              win.print();
            }
          } catch (e) {
            // ignore
          } finally {
            setTimeout(() => document.body.removeChild(iframe), 500);
          }
        }, 500);
      } catch (e) {
        alert(
          'Unable to open print window. Please check your popup blocker or browser settings.'
        );
      }
      return;
    }
  };

  return (
    <div
      className="invoice-modal-backdrop"
      onClick={onClose}
      role="dialog"
      aria-modal="true"
      aria-label="Invoice receipt"
    >
      <div
        id={`invoice-print-${inv.invoiceId}`}
        className="invoice-modal-content"
        onClick={e => e.stopPropagation()}
      >
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
            {/* Render tax lines based on billing province when available */}
            {(() => {
              try {
                const prov = inv.billing?.province;
                if (prov) {
                  const lines = computeTaxes(inv.subtotal, prov);
                  return (
                    <div>
                      {lines.map((l, i) => {
                        const displayAmount = (
                          (l.amount ??
                            roundToCents(inv.subtotal * l.rate)) as number
                        ).toFixed(2);
                        return (
                          <p key={i}>
                            {l.name} ({formatTaxRate(l.rate)}%): $
                            {displayAmount}
                          </p>
                        );
                      })}
                    </div>
                  );
                }
              } catch (e) {
                // Log error to aid debugging and fall back to legacy fields
                console.error('Tax calculation failed for invoice modal:', e);
              }

              // fallback to legacy fields
              return (
                <>
                  <p>TVQ (9.975%): ${inv.tvq.toFixed(2)}</p>
                  <p>TVC (5%): ${inv.tvc.toFixed(2)}</p>
                </>
              );
            })()}
            <p>Discount: -${inv.discount.toFixed(2)}</p>
            <h3>Total: ${inv.total.toFixed(2)}</h3>
          </div>
          <div className="invoice-modal-actions">
            <button className="download-invoice-btn" onClick={downloadInvoice}>
              Download Invoice
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Invoice;
