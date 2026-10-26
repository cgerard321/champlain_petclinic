import { Filter } from 'bad-words';

const CUSTOM_BAD_WORDS = ['tabarnak', 'calice', 'crisse'];
const filter = new Filter();
filter.addWords(...CUSTOM_BAD_WORDS);

const normalizeCharacter = (character: string): string =>
  character
    .toLowerCase()
    .normalize('NFKD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[@4]/g, 'a')
    .replace(/[0]/g, 'o')
    .replace(/[1]/g, 'i')
    .replace(/[3]/g, 'e')
    .replace(/[5$]/g, 's')
    .replace(/[7]/g, 't')
    .replace(/[8]/g, 'b');

interface SourceRange {
  start: number;
  end: number;
}

interface NormalizedReview {
  text: string;
  sourceRanges: SourceRange[];
}


const normalizeReview = (input: string): NormalizedReview => {
  let text = '';
  const sourceRanges: SourceRange[] = [];

  for (let offset = 0; offset < input.length; ) {
    const codePoint = input.codePointAt(offset);
    if (codePoint === undefined) break;

    const character = String.fromCodePoint(codePoint);
    const end = offset + character.length;
    const normalized = normalizeCharacter(character).replace(
      /[^a-z0-9\s]/g,
      ''
    );

    for (const normalizedCharacter of normalized) {
      text += normalizedCharacter;
      sourceRanges.push({ start: offset, end });
    }

    offset = end;
  }

  return { text, sourceRanges };
};

const getProfaneSourceRanges = (input: string): SourceRange[] => {
  const { text, sourceRanges } = normalizeReview(input);
  const profaneRanges: SourceRange[] = [];
  const words = /[a-z0-9]+/g;
  let match: RegExpExecArray | null;

  while ((match = words.exec(text)) !== null) {
    if (!filter.isProfane(match[0])) continue;

    const start = sourceRanges[match.index]?.start;
    const lastCharacter = sourceRanges[match.index + match[0].length - 1];
    if (start !== undefined && lastCharacter) {
      profaneRanges.push({ start, end: lastCharacter.end });
    }
  }

  return profaneRanges;
};

export function isReviewProfane(input: string): boolean {
  return getProfaneSourceRanges(input).length > 0;
}

/** Masks only detected words, preserving harmless text and surrounding punctuation. */
export function cleanReviewText(input: string): string {
  const ranges = getProfaneSourceRanges(input);
  let cleaned = input;

  for (const { start, end } of ranges.reverse()) {
    cleaned = `${cleaned.slice(0, start)}${'*'.repeat(end - start)}${cleaned.slice(end)}`;
  }

  return cleaned;
}
