export const USERNAME_MIN_LENGTH = 3;
export const USERNAME_MAX_LENGTH = 30;

export const USERNAME_PATTERN = /^[A-Za-z][A-Za-z0-9]*(?:_[A-Za-z0-9]+)*$/;

const DIACRITICS = new RegExp('[\\u0300-\\u036f]', 'g');

export const RESERVED_USERNAMES = [
  'admin',
  'administrator',
  'root',
  'system',
  'support',
  'null',
  'undefined',
] as const;

export type UsernameRuleId = 'length' | 'startsAlpha' | 'charset' | 'noSpaceNoAt' | 'noTrailing';

export interface UsernameRule {
  id: UsernameRuleId;
  met: boolean;
}

export function normalizeUsername(raw: string): string {
  return raw.trim();
}

export function checkUsernameRules(raw: string): UsernameRule[] {
  const value = normalizeUsername(raw);
  const filled = value.length > 0;

  return [
    {
      id: 'length',
      met: filled && value.length >= USERNAME_MIN_LENGTH && value.length <= USERNAME_MAX_LENGTH,
    },
    { id: 'startsAlpha', met: filled && /^[A-Za-z]/.test(value) },
    { id: 'charset', met: filled && /^[A-Za-z0-9_]+$/.test(value) },
    { id: 'noSpaceNoAt', met: filled && !/\s/.test(value) && !value.includes('@') },
    { id: 'noTrailing', met: filled && !value.endsWith('_') && !value.includes('__') },
  ];
}

export function matchesUsernameFormat(raw: string): boolean {
  const value = normalizeUsername(raw);
  return (
    value.length >= USERNAME_MIN_LENGTH &&
    value.length <= USERNAME_MAX_LENGTH &&
    USERNAME_PATTERN.test(value)
  );
}

export function isReserved(raw: string): boolean {
  const value = normalizeUsername(raw).toLowerCase();

  return RESERVED_USERNAMES.some((reserved) => reserved === value);
}

export function isSameAsCurrent(raw: string, current: string): boolean {
  return normalizeUsername(raw).toLowerCase() === normalizeUsername(current).toLowerCase();
}

export function isValidUsername(raw: string): boolean {
  return matchesUsernameFormat(raw) && !isReserved(raw);
}
function clampUsername(value: string): string {
  return value.slice(0, USERNAME_MAX_LENGTH).replace(/_+$/, '');
}

function toUsernameBase(raw: string): string {
  const folded = normalizeUsername(raw)
    .normalize('NFD')
    .replace(DIACRITICS, '')

    .replace(/[^A-Za-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')

    .replace(/^[^A-Za-z]+/, '');

  return clampUsername(folded);
}
function suggestFrom(candidates: string[], max: number): string[] {
  return [...new Set(candidates.map(clampUsername))].filter(isValidUsername).slice(0, max);
}
export function suggestUsernames(typed: string, max = 3): string[] {
  const base = toUsernameBase(typed);

  if (base.length === 0) {
    return [];
  }

  return suggestFrom([base, `${base}1`], max);
}
