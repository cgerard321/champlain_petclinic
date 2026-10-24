import { type FormEvent, useState } from 'react';
import { useTranslation } from 'react-i18next';
import getErrorMessage from '@/shared/api/getErrorMessage';
import {
  createOrderReport,
  type OrderReportReason,
  type OrderReportSummary,
} from '../api/orderReports';
import type { OrderModel } from '../models/OrderModel';
import { useProductText } from '../utils/useProductText';

interface ReportOrderModalProps {
  customerId: string;
  order: OrderModel;
  onClose: () => void;
  onSubmitted: (report: OrderReportSummary) => void;
}

const REASONS: OrderReportReason[] = [
  'DAMAGED_ITEM',
  'MISSING_ITEM',
  'WRONG_ITEM',
  'PACKAGE_NOT_RECEIVED',
  'OTHER',
];

const REASONS_REQUIRING_PRODUCTS = new Set<OrderReportReason>([
  'DAMAGED_ITEM',
  'MISSING_ITEM',
  'WRONG_ITEM',
]);

export default function ReportOrderModal({
  customerId,
  order,
  onClose,
  onSubmitted,
}: ReportOrderModalProps): JSX.Element {
  const { t } = useTranslation('carts');
  const { localizedName } = useProductText(order.products);
  const [reason, setReason] = useState<OrderReportReason | ''>('');
  const [selectedProductIds, setSelectedProductIds] = useState<string[]>([]);
  const [description, setDescription] = useState('');
  const [submissionError, setSubmissionError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const toggleProduct = (productId: string): void => {
    setSelectedProductIds(current =>
      current.includes(productId)
        ? current.filter(id => id !== productId)
        : [...current, productId]
    );
  };

  const handleSubmit = async (
    event: FormEvent<HTMLFormElement>
  ): Promise<void> => {
    event.preventDefault();
    setSubmissionError(null);

    if (!reason) {
      setSubmissionError(t('reports.errors.reasonRequired'));
      return;
    }

    if (
      REASONS_REQUIRING_PRODUCTS.has(reason) &&
      selectedProductIds.length === 0
    ) {
      setSubmissionError(t('reports.errors.productRequired'));
      return;
    }

    const cleanedDescription = description.trim();
    if (cleanedDescription.length < 15 || cleanedDescription.length > 500) {
      setSubmissionError(t('reports.errors.descriptionLength'));
      return;
    }

    setSubmitting(true);

    try {
      const report = await createOrderReport(customerId, order.orderId, {
        reason,
        productIds: selectedProductIds,
        description: cleanedDescription,
      });

      setSubmitting(false);
      onSubmitted(report);
    } catch (error: unknown) {
      setSubmissionError(
        getErrorMessage(error, {
          defaultMessage: t('reports.errors.submitFailed'),
        })
      );
      setSubmitting(false);
    }
  };

  return (
    <div className="report-order-backdrop">
      <section
        className="report-order-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="report-order-title"
      >
        <div className="report-order-heading">
          <div>
            <h2 id="report-order-title">{t('reports.title')}</h2>
            <p>{t('reports.orderNumber', { id: order.orderId })}</p>
          </div>
        </div>

        <form onSubmit={handleSubmit} noValidate>
          <fieldset disabled={submitting} className="report-order-fieldset">
            <label htmlFor="report-order-reason">{t('reports.reason')}</label>

            <select
              id="report-order-reason"
              value={reason}
              onChange={event =>
                setReason(event.target.value as OrderReportReason | '')
              }
              required
            >
              <option value="">{t('reports.selectReason')}</option>

              {REASONS.map(reasonOption => (
                <option key={reasonOption} value={reasonOption}>
                  {t(`reports.reasons.${reasonOption}`)}
                </option>
              ))}
            </select>

            <fieldset className="report-order-products">
              <legend>{t('reports.affectedProducts')}</legend>

              {order.products.map(product => (
                <label
                  key={product.productId}
                  className="report-order-product-option"
                >
                  <input
                    type="checkbox"
                    checked={selectedProductIds.includes(product.productId)}
                    onChange={() => toggleProduct(product.productId)}
                  />

                  <span>
                    <strong>{localizedName(product)}</strong>
                    <small>
                      {t('reports.orderedQuantity', {
                        quantity: product.quantityInCart,
                      })}
                    </small>
                  </span>
                </label>
              ))}
            </fieldset>

            <label htmlFor="report-order-description">
              {t('reports.description')}
            </label>

            <textarea
              id="report-order-description"
              value={description}
              onChange={event => setDescription(event.target.value)}
              minLength={15}
              maxLength={500}
              rows={5}
              required
            />

            <div className="report-order-character-count">
              {description.length}/500
            </div>
          </fieldset>

          {submissionError && (
            <div className="report-order-error" role="alert">
              {submissionError}
            </div>
          )}

          <div className="report-order-actions">
            <button
              type="button"
              className="cart-button cart-button--outline"
              onClick={onClose}
              disabled={submitting}
            >
              {t('buttons.cancel')}
            </button>

            <button
              type="submit"
              className="cart-button cart-button--brand"
              disabled={submitting}
              aria-busy={submitting}
            >
              {submitting && (
                <span className="report-order-spinner" aria-hidden="true" />
              )}

              {submitting ? t('reports.submitting') : t('reports.submit')}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}
