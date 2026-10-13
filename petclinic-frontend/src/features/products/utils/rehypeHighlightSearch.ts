import { Nodes } from 'hast';
import { findAndReplace } from 'hast-util-find-and-replace';

interface RehypeHighlightSearchProps {
  searchQuery: string;
}

const escapeSpecialCharacters = (s: string): string =>
  s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

export function RehypeHighlightSearch({
  searchQuery,
}: RehypeHighlightSearchProps) {
  return (tree: Nodes) => {
    if (!searchQuery) return;
    findAndReplace(tree, [
      [
        new RegExp(escapeSpecialCharacters(searchQuery), 'gi'),
        match => ({
          type: 'element',
          tagName: 'mark',
          properties: {},
          children: [{ type: 'text', value: match }],
        }),
      ],
    ]);
  };
}
