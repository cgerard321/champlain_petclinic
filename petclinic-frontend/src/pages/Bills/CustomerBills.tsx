import { useState } from 'react';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import BillsListTable from '@/features/bills/BillsListTable.tsx';
import CurrentBalance from '@/features/bills/CurrentBalance';
import { Currency } from '@/features/bills/utils/convertCurrency';
import '@/features/bills/AdminBillsListTable.css';

export default function CustomerBillingPage(): JSX.Element {
  const [currency, setCurrency] = useState<Currency>('CAD');
  const [currencyOpen, setCurrencyOpen] = useState(false);

  const dispatchSidebarEvent = (name: string): void => {
    try {
      window.dispatchEvent(new CustomEvent(name));
    } catch (e) {
      // ignore in non-browser envs
    }
  };

  return (
    <div className="customer-bills-page">
      <NavBar />
      <div style={{ display: 'flex', gap: 18, alignItems: 'flex-start' }}>
        <aside className="modern-sidebar">
          <div className="sidebar-title">Options</div>
          <div className="sidebar-button-container">
            <button
              onClick={() => dispatchSidebarEvent('customerToggleStatusFilter')}
            >
              Filter by Status
            </button>
            <button
              onClick={() => dispatchSidebarEvent('customerToggleAmountFilter')}
            >
              Filter by Amount
            </button>
            <button
              onClick={() => dispatchSidebarEvent('customerToggleDateFilter')}
            >
              Filter by Date
            </button>
          </div>

          <div style={{ marginTop: '12px' }}>
            <div
              className="currency-dropdown"
              tabIndex={0}
              onBlur={() => setCurrencyOpen(false)}
            >
              <button
                type="button"
                className="currency-btn"
                aria-haspopup="true"
                aria-expanded={currencyOpen}
                aria-label="Select currency"
                onClick={() => setCurrencyOpen(prev => !prev)}
              >
                <span className="currency-label">
                  <span className="currency-prefix">Currency:</span>
                  <span className="currency-value">{currency}</span>
                </span>
                <span className="caret">▾</span>
              </button>

              {currencyOpen && (
                <ul className="currency-menu" role="menu">
                  <li>
                    <button
                      type="button"
                      onMouseDown={() => {
                        setCurrency('CAD');
                        setCurrencyOpen(false);
                      }}
                      role="menuitem"
                    >
                      CAD
                    </button>
                  </li>
                  <li>
                    <button
                      type="button"
                      onMouseDown={() => {
                        setCurrency('USD');
                        setCurrencyOpen(false);
                      }}
                      role="menuitem"
                    >
                      USD
                    </button>
                  </li>
                </ul>
              )}
            </div>
          </div>
        </aside>

        <main style={{ flex: 1 }}>
          <div style={{ maxWidth: 820, marginLeft: '200px', width: '100%' }}>
            <h1 style={{ textAlign: 'center' }}>Your Bills</h1>
            <div
              style={{
                display: 'flex',
                justifyContent: 'center',
                width: '100%',
              }}
            >
              <CurrentBalance currency={currency} />
            </div>
            <div style={{ marginTop: 18 }}>
              <BillsListTable currency={currency} setCurrency={setCurrency} />
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
