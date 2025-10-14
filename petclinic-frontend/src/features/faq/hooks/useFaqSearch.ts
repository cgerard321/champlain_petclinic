import { useMemo, useState } from 'react';
import type { FaqItem } from '../models/FaqItem';

import { FAQ_ITEMS, getFaqById } from '../data/FaqItems';

type Options = {
  category?: string | null;
  items?: readonly FaqItem[];
};

function normalize(s: string): string {
  return s
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase();
}

function matches(item: FaqItem, q: string): boolean {
  if (!q) return true;

  const haystack = normalize(
    `${item.question} ${item.answer} ${(item.tags ?? []).join(' ')} ${item.category ?? ''}`
  );

  return haystack.includes(q);
}

export function searchFaqs(query: string, opts: Options = {}): FaqItem[] {
  const q = normalize(query.trim());

  const source = (opts.items ?? FAQ_ITEMS).filter(i =>
    opts.category ? (i.category ?? '') === opts.category : true
  );

  return source.filter(i => matches(i, q));
}

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export default function useFaqSearch(initialQuery = '', opts: Options = {}) {
  const [query, setQuery] = useState(initialQuery);

  const [category, setCategory] = useState<string | null>(
    opts.category ?? null
  );

  const results = useMemo(
    () => searchFaqs(query, { ...opts, category }),
    [query, opts, category]
  );

  return {
    query,
    setQuery,
    category,
    setCategory,
    results,
    items: FAQ_ITEMS,
    getById: getFaqById,
  };
}
