import { useTranslation } from 'react-i18next';
import type { OrderReportSummary } from '../api/orderReports';

interface OrderReportBannerProps {
  report: OrderReportSummary;
}

export default function OrderReportBanner({
  report,
}: OrderReportBannerProps): JSX.Element {
  const { t } = useTranslation('carts');

  return (
    <div
      className={`order-report-banner report-status-${report.status}`}
      role="status"
    >
      <div className="order-report-banner-header">
        <span className="order-report-banner-title">
          {t('reports.bannerTitle')}
        </span>
        <span className="order-report-status">
          {t(`reports.status.${report.status}`)}
        </span>
      </div>

      <div className="order-report-reference">
        {t('reports.reference')}: <span>{report.reportId}</span>
      </div>

      {report.resolutionNote && (
        <p className="order-report-note">
          <strong>{t('reports.note')}:</strong> {report.resolutionNote}
        </p>
      )}
    </div>
  );
}
