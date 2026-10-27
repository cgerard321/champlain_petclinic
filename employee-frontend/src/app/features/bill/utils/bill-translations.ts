export const VISIT_TYPES_TRANSLATION: Record<string, string> = {
  'Check-Up': $localize`:@@bill.visitTypes.checkup:Examen vétérinaire`,
  Vaccine: $localize`:@@bill.visitTypes.vaccine:Vaccin`,
  Surgery: $localize`:@@bill.visitTypes.surgery:Chirurgie`,
  Dental: $localize`:@@bill.visitTypes.dental:Dentaire`,
  Emergency: $localize`:@@bill.visitTypes.emergency:Urgence`,
  Regular: $localize`:@@bill.visitTypes.regular:Régulier`,
};

export function translateVisitType(type: string): string {
  return VISIT_TYPES_TRANSLATION[type] ?? type;
}

export const BILL_STATUS_TRANSLATION: Record<string, string> = {
  PAID: $localize`:@@bill.statusTypes.paid:PAYÉE`,
  UNPAID: $localize`:@@bill.statusTypes.unpaid:IMPAYÉE`,
  OVERDUE: $localize`:@@bill.statusTypes.overdue:EN RETARD`,
};

export function translateBillStatus(type: string): string {
  return BILL_STATUS_TRANSLATION[type] ?? type;
}
