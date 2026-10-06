
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

  // An empty field has nothing to tick. Without this, `charset` would report a success on it.
  if (value.length === 0) {
    return [
      { id: 'length', met: false },
      { id: 'startsAlpha', met: false },
      { id: 'charset', met: false },
      { id: 'noSpaceNoAt', met: false },
      { id: 'noTrailing', met: false },
    ];
  }
//all rules of username in this return
  return [
    {
      id: 'length',
      met: value.length >= USERNAME_MIN_LENGTH && value.length <= USERNAME_MAX_LENGTH,
    },
    { id: 'startsAlpha', met: /^[A-Za-z]/.test(value) },
    { id: 'charset', met: /^[A-Za-z0-9_]+$/.test(value) },
    { id: 'noSpaceNoAt', met: !/\s/.test(raw) && !raw.includes('@') },
    { id: 'noTrailing', met: !value.endsWith('_') && !value.includes('__') },
  ];
}

export function matchesUsernameFormat(raw: string): boolean {
  const value = normalizeUsername(raw);

  return (
    value.length >= USERNAME_MIN_LENGTH &&
    value.length <= USERNAME_MAX_LENGTH &&
    USERNAME_PATTERN.test(raw)
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
//after all the modifications we stock the information here
function clampUsername(value: string): string {
  if (value.length === 0) {
    return '';
  }

  let clamped = value.slice(0, USERNAME_MAX_LENGTH).replace(/_+$/, '');

  // Rule 1: pad a too-short base so it can still be offered as a suggestion.
  while (clamped.length > 0 && clamped.length < USERNAME_MIN_LENGTH) {
    clamped += '1';
  }

  return clamped;
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
//for suggestions of username
function uniqueValidUsernames(candidates: string[], max: number): string[] {
  const seen = new Set<string>();
  const kept: string[] = [];

  for (const candidate of candidates) {
    const value = clampUsername(candidate);

   //if it has 1 pf these condition it is skipped as false candidate
    if (value.length === 0 || seen.has(value) || !isValidUsername(value)) {
      continue;
    }
// added as seen candidate
    seen.add(value);
    //added to the list
    kept.push(value);

    if (kept.length === max) {
      break;
    }
  }

  return kept;
}
//this function ngenerates the 3 suggestions
export function suggestUsernames(typed: string, max = 3): string[] {
  const base = toUsernameBase(typed);

  if (base.length === 0) {
    return [];
  }

  return uniqueValidUsernames([base, base.replace(/_/g, ''), `${base}1`], max);
}
//generate suggestioin based on the name and full name of the employee
export function suggestFromName(first: string, last: string, max = 3): string[] {
  const firstBase = toUsernameBase(first).toLowerCase();
  const lastBase = toUsernameBase(last).toLowerCase();

  if (firstBase.length === 0 || lastBase.length === 0) {
    return [];
  }

  return uniqueValidUsernames(
    [
    //name + full name
      `${firstBase}_${lastBase}`,
      //first letter of name  + _ +Last Name
      `${firstBase.charAt(0)}_${lastBase}`,
     // name + _+ fist letter of last name
      `${firstBase}_${lastBase.charAt(0)}`,
    ],
    max,
  );
}
