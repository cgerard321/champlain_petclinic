import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import './OrderHistory.css';

import { useUser } from '@/context/UserContext';
import { NavBar } from '@/layouts/AppNavBar';
import { getCustomerOrders } from '@/features/carts/api/getCustomerOrders';
import type { OrderModel } from '@/features/carts/models/OrderModel';
import { AppRoutePaths } from '@/shared/models/path.routes';

export default function OrderHistoryPage(): JSX.Element {
  const { t, i18n } = useTranslation('home');
  const { user } = useUser();

  const [orders, setOrders] = useState<OrderModel[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const locale = i18n.language.startsWith('fr') ? 'fr-CA' : 'en-CA';

  const formatCurrency = (value: number): string =>
    new Intl.NumberFormat(locale, {
      style: 'currency',
      currency: 'CAD',
    }).format(value);

  const formatDate = (date: string): string =>
    new Date(date).toLocaleDateString(locale);

  useEffect(() => {
    let active = true;

    const loadOrders = async (): Promise<void> => {
      if (!user.userId) {
        setOrders([]);
        setLoading(false);
        return;
      }

      setLoading(true);
      setError(false);

      try {
        const result = await getCustomerOrders(user.userId);

        if (active) {
          const customerOrders = result
            .filter(order => order.customerId === user.userId)
            .sort(
              (a, b) =>
                new Date(b.orderDate).getTime() -
                new Date(a.orderDate).getTime()
            );

          setOrders(customerOrders);
        }
      } catch (err) {
        console.error('Failed to load orders:', err);

        if (active) {
          setError(true);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    void loadOrders();

    return () => {
      active = false;
    };
  }, [user.userId]);

  return (
    <>
      <NavBar />

      <main className="customer-orders">
        <div className="customer-orders__heading">
          <h1>{t('orderHistory.title')}</h1>
          <p>{t('orderHistory.subtitle')}</p>
        </div>

        {loading && <p>{t('orderHistory.loading')}</p>}

        {!loading && error && (
          <div className="alert alert-danger">
            {t('orderHistory.loadError')}
          </div>
        )}

        {!loading && !error && orders.length === 0 && (
          <div className="alert alert-info">{t('orderHistory.empty')}</div>
        )}

        {!loading &&
          !error &&
          orders.map(order => (
            <article className="customer-orders__card" key={order.orderId}>
              <div className="customer-orders__card-header">
                <div style={{ minWidth: 0, flex: 1 }}>
                  <h5 className="customer-orders__order-id">
                    {t('orderHistory.order')} #{order.orderId}
                  </h5>

                  <p className="customer-orders__muted mb-1">
                    {formatDate(order.orderDate)}
                  </p>

                  <p className="customer-orders__muted mb-0">
                    {t('orderHistory.productCount', {
                      count: order.products.length,
                    })}
                  </p>
                </div>

                <div className="customer-orders__price">
                  {formatCurrency(order.total)}
                </div>
              </div>

              <div className="customer-orders__card-footer">
                <Link
                  className="btn btn-primary"
                  to={AppRoutePaths.CustomerOrderDetails.replace(
                    ':orderId',
                    encodeURIComponent(order.orderId)
                  )}
                >
                  {t('orderHistory.viewDetails')}
                </Link>
              </div>
            </article>
          ))}
      </main>
    </>
  );
}
