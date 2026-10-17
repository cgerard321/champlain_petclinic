import { Nodes } from 'hast';

interface RehypeHighlightSearchProps {
  searchQuery: string;
}

type MutableHastNode = {
  type?: string;
  value?: string;
  children?: MutableHastNode[];
  [key: string]: unknown;
};

const escapeSpecialCharacters = (value: string): string =>
  value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

const highlightTextNodes = (
  nodes: MutableHastNode[],
  expression: RegExp
): void => {
  for (let index = 0; index < nodes.length; index += 1) {
    const node = nodes[index];
    if (node.type === 'text' && typeof node.value === 'string') {
      const children: MutableHastNode[] = [];
      let lastIndex = 0;
      let match = expression.exec(node.value);
      while (match) {
        if (match.index > lastIndex) {
          children.push({
            type: 'text',
            value: node.value.slice(lastIndex, match.index),
          });
        }
        children.push({
          type: 'element',
          tagName: 'mark',
          properties: {},
          children: [{ type: 'text', value: match[0] }],
        });
        lastIndex = match.index + match[0].length;
        match = expression.exec(node.value);
      }
      if (children.length > 0) {
        if (lastIndex < node.value.length) {
          children.push({ type: 'text', value: node.value.slice(lastIndex) });
        }
        nodes.splice(index, 1, ...children);
        index += children.length - 1;
      }
    } else if (node.children) {
      highlightTextNodes(node.children, expression);
    }
  }
};

export function RehypeHighlightSearch({
  searchQuery,
}: RehypeHighlightSearchProps) {
  return (tree: Nodes): void => {
    if (!searchQuery) return;
    const expression = new RegExp(escapeSpecialCharacters(searchQuery), 'gi');
    const root = tree as unknown as MutableHastNode;
    if (root.children) highlightTextNodes(root.children, expression);
  };
}
