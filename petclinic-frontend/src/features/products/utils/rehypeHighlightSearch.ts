import { Nodes } from 'hast';
type MutableHastNode = {
  type?: string;
  value?: string;
  children?: MutableHastNode[];
  [key: string]: unknown;
};

const highlightTextNodes = (
  nodes: MutableHastNode[],
  expression: RegExp
): void => {
  for (let index = 0; index < nodes.length; index += 1) {
    const node = nodes[index];
    if (node.type === 'text' && typeof node.value === 'string') {
      const replacements: MutableHastNode[] = [];
      let lastIndex = 0;
      let match = expression.exec(node.value);
      while (match) {
        if (match.index > lastIndex)
          replacements.push({
            type: 'text',
            value: node.value.slice(lastIndex, match.index),
          });
        replacements.push({
          type: 'element',
          tagName: 'mark',
          properties: {},
          children: [{ type: 'text', value: match[0] }],
        });
        lastIndex = match.index + match[0].length;
        match = expression.exec(node.value);
      }
      if (replacements.length > 0) {
        if (lastIndex < node.value.length)
          replacements.push({
            type: 'text',
            value: node.value.slice(lastIndex),
          });
        nodes.splice(index, 1, ...replacements);
        index += replacements.length - 1;
      }
    } else if (node.children) {
      highlightTextNodes(node.children, expression);
    }
  }
};

interface RehypeHighlightSearchProps {
  searchQuery: string;
}

const escapeSpecialCharacters = (s: string): string =>
  s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

export function RehypeHighlightSearch({
  searchQuery,
}: RehypeHighlightSearchProps) {
  return (tree: Nodes): void => {
    if (!searchQuery) return;
    const root = tree as unknown as MutableHastNode;
    if (root.children) {
      highlightTextNodes(
        root.children,
        new RegExp(escapeSpecialCharacters(searchQuery), 'gi')
      );
    }
  };
}
