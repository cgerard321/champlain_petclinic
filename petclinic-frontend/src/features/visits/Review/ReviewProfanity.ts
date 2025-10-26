import { Filter } from 'bad-words';

const ENCODED: string[] = [
  'YmFkd29yZA==',
  'dGFiYXJuYWs=',
  'Y2FsaWNl',
  'Y3Jpc3Nl',
  'bWF1ZGl0',
];

const EXTRA_BAD_WORDS: string[] = ENCODED.map((b64): string => atob(b64));

const filter = new Filter();
if (EXTRA_BAD_WORDS.length) filter.addWords(...EXTRA_BAD_WORDS);

const ALL_BAD_WORDS = filter.list.slice();
const LONG = ALL_BAD_WORDS.filter(w => w.length >= 4).map(w => w.toLowerCase());
const SHORT = ALL_BAD_WORDS.filter(w => w.length <= 3).map(w =>
  w.toLowerCase()
);

const deaccentLower = (s: string): string =>
  s
    .toLowerCase()
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '');

const leet = (s: string): string =>
  s
    .replace(/[@]/g, 'a')
    .replace(/[0]/g, 'o')
    .replace(/[4]/g, 'a')
    .replace(/[1!]/g, 'i')
    .replace(/[3]/g, 'e')
    .replace(/[5\$]/g, 's')
    .replace(/[7]/g, 't')
    .replace(/[8]/g, 'b');

const flatLetters = (s: string): string =>
  leet(deaccentLower(s)).replace(/[^a-z]/g, '');

const boundaryText = (s: string): string => leet(deaccentLower(s));

const esc = (w: string): string => w.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

export function isProfaneLite(input: string): boolean {
  if (filter.isProfane(input)) return true;

  const flat = flatLetters(input);
  const text = boundaryText(input);

  if (LONG.some(w => flat.includes(w))) return true;

  if (
    SHORT.some(w => new RegExp(`(?<![a-z])${esc(w)}(?![a-z])`, 'i').test(text))
  )
    return true;

  return false;
}

export function cleanLite(input: string): string {
  if (filter.isProfane(input) || isProfaneLite(input)) {
    return '*'.repeat(input.length);
  }
  return input;
}
