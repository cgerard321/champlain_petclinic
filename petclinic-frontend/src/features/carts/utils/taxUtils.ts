export type TaxLine = {
  name: string; // PST, GST, HST
  rate: number; // decimal (e.g. 0.05)
  amount?: number; // computed amount in dollars
};

// Map of province/territory code => array of tax lines that apply
export const PROVINCE_TAX_MAP: Record<string, TaxLine[]> = {
  // Provinces with GST only
  AB: [{ name: 'GST', rate: 0.05 }],
  MB: [
    { name: 'GST', rate: 0.05 },
    { name: 'PST', rate: 0.07 },
  ],
  BC: [
    { name: 'GST', rate: 0.05 },
    { name: 'PST', rate: 0.07 },
  ],
  SK: [
    { name: 'GST', rate: 0.05 },
    { name: 'PST', rate: 0.06 },
  ],
  QC: [
    { name: 'GST', rate: 0.05 },
    { name: 'PST', rate: 0.09975 },
  ],
  ON: [{ name: 'HST', rate: 0.13 }],
  NB: [{ name: 'HST', rate: 0.15 }],
  NL: [{ name: 'HST', rate: 0.15 }],
  NS: [{ name: 'HST', rate: 0.15 }],
  PE: [{ name: 'HST', rate: 0.15 }],
  NT: [{ name: 'GST', rate: 0.05 }],
  NU: [{ name: 'GST', rate: 0.05 }],
  YT: [{ name: 'GST', rate: 0.05 }],
};

// Compute the combined tax rate for a province code
export function combinedRateForProvince(province?: string): number {
  if (!province) return averageCanadianCombinedTaxRate();
  const normalized = province.toUpperCase();
  const lines = PROVINCE_TAX_MAP[normalized];
  if (!lines || lines.length === 0) return averageCanadianCombinedTaxRate();
  return lines.reduce((s, t) => s + t.rate, 0);
}

// Compute tax lines (name, rate, amount) for a subtotal and an optional province code.
// If province is not provided or unknown, returns a single "Estimated Taxes" line using average Canadian combined tax rate.
export function computeTaxes(subtotal: number, province?: string): TaxLine[] {
  if (!province) {
    const avg = averageCanadianCombinedTaxRate();
    return [
      {
        name: 'Estimated Taxes',
        rate: avg,
        amount: roundToCents(subtotal * avg),
      },
    ];
  }
  const normalized = province.toUpperCase();
  const lines = PROVINCE_TAX_MAP[normalized];
  if (!lines || lines.length === 0) {
    const avg = averageCanadianCombinedTaxRate();
    return [
      {
        name: 'Estimated Taxes',
        rate: avg,
        amount: roundToCents(subtotal * avg),
      },
    ];
  }
  return lines.map(l => ({ ...l, amount: roundToCents(subtotal * l.rate) }));
}

function roundToCents(n: number): number {
  return Math.round(n * 100) / 100;
}

// Average combined tax rate across provinces/territories in our map.
export function averageCanadianCombinedTaxRate(): number {
  const rates = Object.values(PROVINCE_TAX_MAP).map(lines =>
    lines.reduce((s, t) => s + t.rate, 0)
  );
  if (rates.length === 0) return 0.13; // sensible default
  const sum = rates.reduce((s, r) => s + r, 0);
  return sum / rates.length;
}
