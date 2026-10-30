import axiosInstance from '@/shared/api/axiosInstance';

export type OrderReportReason =
  | 'DAMAGED_ITEM'
  | 'MISSING_ITEM'
  | 'WRONG_ITEM'
  | 'PACKAGE_NOT_RECEIVED'
  | 'OTHER';

export type OrderReportStatus = 'NEW' | 'IN_REVIEW' | 'RESOLVED' | 'REJECTED';

export interface CreateOrderReportRequest {
  reason: OrderReportReason;
  productIds: string[];
  description: string;
}

export interface OrderReportAffectedItem {
  productId: string;
  productName: string;
}

export interface OrderReportSummary {
  reportId: string;
  orderId: string;
  customerId: string;
  customerName: string;
  reason: OrderReportReason;
  affectedItems: OrderReportAffectedItem[];
  description: string;
  status: OrderReportStatus;
  submittedAt: string;
  resolutionNote?: string | null;
  resolvedAt?: string | null;
}

export async function createOrderReport(
  customerId: string,
  orderId: string,
  request: CreateOrderReportRequest
): Promise<OrderReportSummary> {
  const { data } = await axiosInstance.post<OrderReportSummary>(
    `/orders/customer/${encodeURIComponent(customerId)}/${encodeURIComponent(orderId)}/reports`,
    request,
    {
      useV2: false,
      handleLocally: true,
    }
  );

  return data;
}

export async function getCustomerOrderReports(
  customerId: string
): Promise<OrderReportSummary[]> {
  const { data } = await axiosInstance.get<OrderReportSummary[]>(
    `/orders/customer/${encodeURIComponent(customerId)}/reports`,
    {
      useV2: false,
      handleLocally: true,
    }
  );

  return data;
}

export function isOpenOrderReport(report: OrderReportSummary): boolean {
  return report.status === 'NEW' || report.status === 'IN_REVIEW';
}
