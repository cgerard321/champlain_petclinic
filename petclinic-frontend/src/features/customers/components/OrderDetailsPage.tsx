import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import './OrderHistory.css';

import { useUser } from '@/context/UserContext';
import { NavBar } from '@/layouts/AppNavBar';
import { getCustomerOrders } from '@/features/carts/api/getCustomerOrders';
import type { OrderModel } from '@/features/carts/models/OrderModel';
import { AppRoutePaths } from '@/shared/models/path.routes';

export default function OrderDetailsPage(): JSX.Element {
  const { orderId } = useParams<{ orderId: string }>();
  const { user } = useUser();
  const { t, i18n } = useTranslation('home');

  const [order, setOrder] = useState<OrderModel | null>(null);
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

    const loadOrder = async (): Promise<void> => {
      setLoading(true);
      setError(false);
      setOrder(null);

      if (!user.userId || !orderId) {
        setLoading(false);
        return;
      }

      try {
        const orders = await getCustomerOrders(user.userId);

        const matchingOrder = orders.find(
          item => item.orderId === orderId && item.customerId === user.userId
        );

        if (active) {
          setOrder(matchingOrder ?? null);
        }
      } catch (err) {
        console.error('Failed to load order details:', err);

        if (active) {
          setError(true);
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    void loadOrder();

    return () => {
      active = false;
    };
  }, [user.userId, orderId]);

  return (
    <>
      <NavBar />

      <main className="customer-orders">
        <Link
          to={AppRoutePaths.CustomerOrderHistory}
          className="btn btn-outline-secondary customer-orders__back"
        >
          ← {t('orderHistory.back')}
        </Link>

        {loading && <p>{t('orderHistory.loadingDetails')}</p>}

        {!loading && error && (
          <div className="alert alert-danger">
            {t('orderHistory.detailsError')}
          </div>
        )}

        {!loading && !error && !order && (
          <div className="alert alert-warning">
            {t('orderHistory.notFound')}
          </div>
        )}

        {!loading && !error && order && (
          <>
            <div className="customer-orders__heading">
              <h1>{t('orderHistory.detailsTitle')}</h1>

              <p className="customer-orders__order-id">
                {t('orderHistory.order')} #{order.orderId}
              </p>

              <p>
                {t('orderHistory.orderDate')}: {formatDate(order.orderDate)}
              </p>
            </div>

            <section className="customer-orders__card">
              <h4>{t('orderHistory.products')}</h4>

              {order.products.map((product, index) => (
                <div
                  className="customer-orders__product"
                  key={`${product.productId}-${index}`}
                >
                  <div className="customer-orders__product-info">
                    <h5>{product.productName}</h5>

                    <p className="customer-orders__muted">
                      {product.productDescription}
                    </p>

                    <p className="mb-0">
                      {t('orderHistory.quantity')}: {product.quantityInCart}
                    </p>
                  </div>

                  <div className="customer-orders__product-price">
                    <p className="customer-orders__muted mb-1">
                      {t('orderHistory.unitPrice')}:{' '}
                      {formatCurrency(product.productSalePrice)}
                    </p>

                    <strong>
                      {formatCurrency(
                        product.productSalePrice * product.quantityInCart
                      )}
                    </strong>
                  </div>
                </div>
              ))}
            </section>

            <section className="customer-orders__card">
              <h4 className="mb-4">{t('orderHistory.summary')}</h4>

              <div className="customer-orders__summary-row">
                <span>{t('orderHistory.subtotal')}</span>
                <span>{formatCurrency(order.subtotal)}</span>
              </div>

              {order.promoPercent != null && order.promoPercent > 0 && (
                <div className="customer-orders__summary-row">
                  <span>{t('orderHistory.promotion')}</span>
                  <span>{order.promoPercent}%</span>
                </div>
              )}

              <div className="customer-orders__summary-row">
                <span>{t('orderHistory.tvq')}</span>
                <span>{formatCurrency(order.tvq)}</span>
              </div>

              <div className="customer-orders__summary-row">
                <span>{t('orderHistory.tvc')}</span>
                <span>{formatCurrency(order.tvc)}</span>
              </div>

              {order.shippingCost != null && (
                <div className="customer-orders__summary-row">
                  <span>{t('orderHistory.shipping')}</span>
                  <span>{formatCurrency(order.shippingCost)}</span>
                </div>
              )}

              <div className="customer-orders__summary-row customer-orders__total">
                <span>{t('orderHistory.total')}</span>
                <span>{formatCurrency(order.total)}</span>
              </div>
            </section>

            <div className="customer-orders__actions">
              <div className="text-end">
                <button
                  type="button"
                  className="btn btn-primary"
                  disabled
                  title={t('orderHistory.invoicePending')}
                >
                  {t('orderHistory.invoice')}
                </button>

                <p className="customer-orders__muted mt-2">
                  {t('orderHistory.invoicePending')}
                </p>
              </div>
            </div>
          </>
        )}
      </main>
    </>
  );
}
