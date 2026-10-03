import Markdown from 'react-markdown';
import { JSX } from 'react';
import { MarkdownSafeLink } from '@/features/products/components/MarkdownSafeLink.tsx';

interface MarkdownReviewBodyProps {
  children: string;
}

export function MarkdownReviewBody({
  children,
}: MarkdownReviewBodyProps): JSX.Element {
  const components = {
    a: MarkdownSafeLink,
  };
  return (
    <div>
      <Markdown components={components}>{children}</Markdown>
    </div>
  );
}
