// Currency conversion utility

export type Currency = 'CAD' | 'USD';

const rates: Record<Currency, number> = {
  CAD: 1,
  USD: 0.73, // Example rate, update as needed
};

export function convertCurrency(
  amount: number,
  from: Currency,
  to: Currency
): number {
  if (from === to) return amount;
  // Convert from source to CAD, then to target
  const amountInCAD = amount / rates[from];
  return amountInCAD * rates[to];
}
