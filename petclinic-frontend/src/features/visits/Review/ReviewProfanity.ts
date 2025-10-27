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

// The following is used to separate the words by length to avoid false positives:

// SHORT words (3 letters or less) are matched only as full words since they
// often appear inside harmless words (Take a look at "passenger" or "class" for example).

// LONG words (4+ letters) are checked as substrings because accidental matches
// inside innocent words are less likely to happen.

const LONG = ALL_BAD_WORDS.filter(w => w.length >= 4).map(w => w.toLowerCase());
const SHORT = ALL_BAD_WORDS.filter(w => w.length <= 3).map(w =>
  w.toLowerCase()
);

// This converts accented characters to their base ASCII form (For example: "é" to "e").
// This is used to prevent evasion through accented spellings of bad words.

const deaccentLower = (s: string): string =>
  s
    .toLowerCase()
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '');

// For context, Leet (or “1337”) replaces letters with similar-looking symbols to evade filters.
// The following converts common leetspeak substitutions
// back to their letter equivalents (For example: "@" to "a", "0" to "o")

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
  const profane = isProfaneLite(input);
  if (profane) {
    return '*'.repeat(input.length);
  }

  return input;
}
