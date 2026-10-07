
export const USERNAME_MIN_LENGTH = 3;
export const USERNAME_MAX_LENGTH = 30;

//start with a letter,can have numbers and underscores, no two underscore at the same time
export const USERNAME_PATTERN = /^[A-Za-z][A-Za-z0-9]*(?:_[A-Za-z0-9]+)*$/;

// Convert accented letters to plain letters (é → e)...
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


//structure of username
export type UsernameRuleId = 'length' | 'startsAlpha' | 'charset' | 'noSpaceNoAt' | 'noTrailing';

export interface UsernameRule {
  id: UsernameRuleId;
  met: boolean;
}


export function normalizeUsername(raw: string): string {
  return raw.trim();
}

export function checkUsernameRules(raw: string): UsernameRule[] {
  //remove the spaces before and after the username
  const value = normalizeUsername(raw);
  //an empty field ticks nothing
  const filled = value.length > 0;

  //all rules of username in this return
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

  // The pattern is applied to the trimmed name too, for the same reason as the rule above: the
  // gate has to judge the name that will actually be sent, not the one still being typed.
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
//cut to the max length, never leaving a trailing underscore
function clampUsername(value: string): string {
  return value.slice(0, USERNAME_MAX_LENGTH).replace(/_+$/, '');
}

function toUsernameBase(raw: string): string {
  //this constant transform it into valid username
  const folded = normalizeUsername(raw)

    .normalize('NFD')
    .replace(DIACRITICS, '')

    .replace(/[^A-Za-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')

    .replace(/^[^A-Za-z]+/, '');

  return clampUsername(folded);
}
//keeps the valid candidates only, without duplicates
function suggestFrom(candidates: string[], max: number): string[] {
  return [...new Set(candidates.map(clampUsername))].filter(isValidUsername).slice(0, max);
}
//this function ngenerates the 3 suggestions
export function suggestUsernames(typed: string, max = 3): string[] {
  const base = toUsernameBase(typed);

  if (base.length === 0) {
    return [];
  }

  return suggestFrom([base, `${base}1`], max);
}
//generate suggestioin based on the name and full name of the employee
export function suggestFromName(first: string, last: string, max = 3): string[] {
  const firstBase = toUsernameBase(first).toLowerCase();
  const lastBase = toUsernameBase(last).toLowerCase();

  if (firstBase.length === 0 || lastBase.length === 0) {
    return [];
  }

  return suggestFrom(
    [
      //name + full name
      `${firstBase}_${lastBase}`,
      //first letter of name + _ + last name
      `${firstBase.charAt(0)}_${lastBase}`,
      //name + _ + first letter of last name
      `${firstBase}_${lastBase.charAt(0)}`,
    ],
    max,
  );
}
