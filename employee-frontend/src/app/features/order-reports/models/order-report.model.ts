export type OrderReportStatus = 'NEW' | 'IN_REVIEW' | 'RESOLVED' | 'REJECTED';

export type OrderReportReason =
  'DAMAGED_ITEM' | 'MISSING_ITEM' | 'WRONG_ITEM' | 'PACKAGE_NOT_RECEIVED' | 'OTHER';

export interface AffectedItem {
  productId: string;
  productName: string;
}

export interface OrderReport {
  reportId: string;
  orderId: string;
  customerId: string;
  customerName: string;
  customerEmail: string;
  reason: OrderReportReason;
  affectedItems: AffectedItem[];
  description: string;
  status: OrderReportStatus;
  submittedAt: string;
}

export interface OrderProduct {
  productId: string;
  productName: string;
  productSalePrice: number;
  quantityInCart: number;
}

export interface Order {
  orderId: string;
  customerId: string;
  customerName: string;
  email: string;
  orderDate: string;
  address: string;
  city: string;
  province: string;
  postalCode: string;
  products: OrderProduct[];
  subtotal: number;
  total: number;
  shippingMethod: string;
}

export const REPORT_STATUS_LABELS: Record<OrderReportStatus, string> = {
  NEW: $localize`:@@orderReports.status.NEW:Nouveau`,
  IN_REVIEW: $localize`:@@orderReports.status.IN_REVIEW:En cours d'examen`,
  RESOLVED: $localize`:@@orderReports.status.RESOLVED:Résolu`,
  REJECTED: $localize`:@@orderReports.status.REJECTED:Rejeté`,
};

export const REPORT_REASON_LABELS: Record<OrderReportReason, string> = {
  DAMAGED_ITEM: $localize`:@@orderReports.reasons.DAMAGED_ITEM:Article(s) endommagé(s)`,
  MISSING_ITEM: $localize`:@@orderReports.reasons.MISSING_ITEM:Article(s) manquant(s)`,
  WRONG_ITEM: $localize`:@@orderReports.reasons.WRONG_ITEM:Mauvais article reçu`,
  PACKAGE_NOT_RECEIVED: $localize`:@@orderReports.reasons.PACKAGE_NOT_RECEIVED:Colis non reçu`,
  OTHER: $localize`:@@orderReports.reasons.OTHER:Autre`,
};
