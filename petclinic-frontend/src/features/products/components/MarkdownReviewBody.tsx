import Markdown from 'react-markdown';
import { JSX, useEffect, useRef } from 'react';
import { MarkdownSafeLink } from '@/features/products/components/MarkdownSafeLink.tsx';
import './MarkdownReviewBody.css';
import remarkGfm from 'remark-gfm';
import remarkIns from 'remark-ins';

interface MarkdownReviewBodyProps {
  unlimitedSize?: boolean;
  children: string;
  setHasOverflow?: (arg0: boolean) => void;
}

export function MarkdownReviewBody({
  unlimitedSize = false,
  children,
  setHasOverflow,
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
          'img',
          'hr',
        ]}
        unwrapDisallowed
        components={components}
        remarkPlugins={[remarkGfm, remarkIns]}
      >
        {children}
      </Markdown>
    </div>
  );
}
