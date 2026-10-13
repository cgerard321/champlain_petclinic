import Markdown from 'react-markdown';
import { JSX, useEffect, useRef } from 'react';
import { MarkdownSafeLink } from '@/features/products/components/MarkdownSafeLink.tsx';
import './MarkdownReviewBody.css';
import remarkGfm from 'remark-gfm';
import remarkIns from 'remark-ins';
import { RehypeHighlightSearch } from '@/features/products/utils/rehypeHighlightSearch.ts';

interface MarkdownReviewBodyProps {
  unlimitedSize?: boolean;
  children: string;
  setHasOverflow?: (arg0: boolean) => void;
  searchQuery?: string;
}

export function MarkdownReviewBody({
  unlimitedSize = false,
  children,
  setHasOverflow,
  searchQuery,
}: MarkdownReviewBodyProps): JSX.Element {
  const ref = useRef<HTMLDivElement>(null);
  const components = {
    a: MarkdownSafeLink,
  };
  useEffect(() => {
    const el = ref.current;
    if (!el || !setHasOverflow) return;
    setHasOverflow(el.scrollHeight > el.clientHeight);
  });
  return (
    <div
      ref={ref}
      className={`markdown ${unlimitedSize ? 'unlimited-size' : ''}`}
    >
      <Markdown
        allowedElements={[
          'p',
          'strong',
          'em',
          'a',
          'ul',
          'ol',
          'li',
          'code',
          'h1',
          'h2',
          'h3',
          'thead',
          'th',
          'tbody',
          'tr',
          'td',
          'pre',
          'del',
          'table',
          'ins',
          'blockquote',
          'hr',
          'mark',
        ]}
        unwrapDisallowed
        components={components}
        remarkPlugins={[remarkGfm, remarkIns]}
        rehypePlugins={[[RehypeHighlightSearch, { searchQuery: searchQuery }]]}
      >
        {children}
      </Markdown>
    </div>
  );
}
