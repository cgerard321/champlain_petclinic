export interface Badge {
  vetId: string;
  resourceBase64: string;
  badgeTitle: string;
  badgeDate: string;
}

export interface BadgeRequest {
  vetId: string;
  badgeTitle: string;
  badgeDate: string;
  resourceBase64?: string;
}
