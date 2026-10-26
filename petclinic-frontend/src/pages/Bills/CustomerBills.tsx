import { useState } from 'react';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import BillsListTable from '@/features/bills/BillsListTable.tsx';
import CurrentBalance from '@/features/bills/CurrentBalance';
import { Currency } from '@/features/bills/utils/convertCurrency';
import '@/features/bills/AdminBillsListTable.css';
import './CustomerBills.css';
import { useTranslation } from 'react-i18next';

export default function CustomerBillingPage(): JSX.Element {
  const { t } = useTranslation('bills');
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
        <aside className="modern-sidebar" aria-label={t('sidebar.ariaLabel')}>
          <div className="sidebar-title">{t('sidebar.title')}</div>

          <div className="sidebar-button-container">
            <button
              type="button"
              onClick={() => dispatchSidebarEvent('customerToggleStatusFilter')}
            >
              {t('buttons.filterStatus')}
            </button>

            <button
              type="button"
              onClick={() => dispatchSidebarEvent('customerToggleAmountFilter')}
            >
              {t('buttons.filterAmount')}
            </button>

            <button
              type="button"
              onClick={() => dispatchSidebarEvent('customerToggleDateFilter')}
            >
              {t('buttons.filterDate')}
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
                aria-label={t('currency.ariaLabel')}
                onClick={() => setCurrencyOpen(prev => !prev)}
              >
                <span className="currency-label">
                  <span className="currency-prefix">
                    {t('buttons.currency')}
                  </span>
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
            <h1 className="customer-bills-title">{t('title')}</h1>

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
