import { useState } from 'react';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import BillsListTable from '@/features/bills/BillsListTable.tsx';
import CurrentBalance from '@/features/bills/CurrentBalance';
import { Currency } from '@/features/bills/utils/convertCurrency';
import '@/features/bills/AdminBillsListTable.css';
import './CustomerBills.css';

export default function CustomerBillingPage(): JSX.Element {
  const [currency, setCurrency] = useState<Currency>('CAD');
  const [currencyOpen, setCurrencyOpen] = useState(false);

  const dispatchSidebarEvent = (name: string): void => {
    try {
      window.dispatchEvent(new CustomEvent(name));
    } catch (e) {
      // Ignore in non-browser environments.
    }
  };

  return (
    <div className="customer-bills-page">
      <NavBar />

      <div className="customer-bills-layout">
        <aside className="modern-sidebar" aria-label="Billing options">
          <div className="sidebar-title">Options</div>

          <div className="sidebar-button-container">
            <button
              type="button"
              onClick={() => dispatchSidebarEvent('customerToggleStatusFilter')}
            >
              Filter by Status
            </button>

            <button
              type="button"
              onClick={() => dispatchSidebarEvent('customerToggleAmountFilter')}
            >
              Filter by Amount
            </button>

            <button
              type="button"
              onClick={() => dispatchSidebarEvent('customerToggleDateFilter')}
            >
              Filter by Date
            </button>
          </div>

          <div className="customer-bills-currency">
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

        <main className="customer-bills-main">
          <div className="customer-bills-content">
            <h1 className="customer-bills-title">Your Bills</h1>

            <div className="customer-bills-balance">
              <CurrentBalance currency={currency} />
            </div>

            <div className="customer-bills-list">
              <BillsListTable currency={currency} setCurrency={setCurrency} />
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}
