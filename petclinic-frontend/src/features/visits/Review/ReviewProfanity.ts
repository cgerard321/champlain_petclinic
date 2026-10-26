import { Filter } from 'bad-words';

const CUSTOM_BAD_WORDS = ['tabarnak', 'calice', 'crisse'];
const filter = new Filter();
filter.addWords(...CUSTOM_BAD_WORDS);

const FUZZY_BAD_WORDS = Array.from(
  new Set(filter.list.map(word => word.toLowerCase()))
).filter(word => word.length >= 5 && /^[a-z]+$/.test(word));

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

const hasOneExtraLetter = (word: string, candidate: string): boolean => {
  if (word.length !== candidate.length + 1) return false;

  let wordIndex = 0;
  let candidateIndex = 0;
  let skippedExtraLetter = false;

  while (wordIndex < word.length && candidateIndex < candidate.length) {
    if (word[wordIndex] === candidate[candidateIndex]) {
      wordIndex++;
      candidateIndex++;
    } else if (skippedExtraLetter) {
      return false;
    } else {
      skippedExtraLetter = true;
      wordIndex++;
    }
  }

  return true;
};

const isWithinEditDistance = (
  left: string,
  right: string,
  maxDistance: number
): boolean => {
  if (Math.abs(left.length - right.length) > maxDistance) return false;

  let previousRow = Array.from(
    { length: right.length + 1 },
    (_, index) => index
  );

  for (let leftIndex = 1; leftIndex <= left.length; leftIndex++) {
    const currentRow = [leftIndex];
    let minimumInRow = leftIndex;

    for (let rightIndex = 1; rightIndex <= right.length; rightIndex++) {
      const substitutionCost =
        left[leftIndex - 1] === right[rightIndex - 1] ? 0 : 1;
      const distance = Math.min(
        currentRow[rightIndex - 1] + 1,
        previousRow[rightIndex] + 1,
        previousRow[rightIndex - 1] + substitutionCost
      );
      currentRow.push(distance);
      minimumInRow = Math.min(minimumInRow, distance);
    }

    if (minimumInRow > maxDistance) return false;
    previousRow = currentRow;
  }

  return previousRow[right.length] <= maxDistance;
};

const isProfaneWord = (word: string): boolean => {
  if (filter.isProfane(word)) return true;
  if (!/^[a-z]+$/.test(word)) return false;

  return FUZZY_BAD_WORDS.some(candidate => {
    if (candidate.length === 5) return hasOneExtraLetter(word, candidate);

    const maxDistance = candidate.length >= 8 ? 1 : 0;
    return (
      Math.abs(word.length - candidate.length) <= maxDistance &&
      isWithinEditDistance(word, candidate, maxDistance)
    );
  });
};

const getProfaneSourceRanges = (input: string): SourceRange[] => {
  const { text, sourceRanges } = normalizeReview(input);
  const profaneRanges: SourceRange[] = [];
  const words = /[a-z0-9]+/g;
  let match: RegExpExecArray | null;

  while ((match = words.exec(text)) !== null) {
    if (!isProfaneWord(match[0])) continue;

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
