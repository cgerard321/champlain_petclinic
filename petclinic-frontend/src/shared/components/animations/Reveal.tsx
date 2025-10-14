import { useEffect, useRef, useState, ReactNode, CSSProperties } from 'react';

import './Reveal.css';

type Props = {
  children: ReactNode;
  as?: keyof JSX.IntrinsicElements;
  className?: string;
  style?: CSSProperties;
  threshold?: number;
  rootMargin?: string;
  delay?: number;
  once?: boolean;
};

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export default function Reveal({
  children,
  as = 'div',
  className = '',
  style,
  threshold = 0.15,
  rootMargin = '0px 0px -60px 0px',
  delay = 0,
  once = true,
}: Props) {
  const [visible, setVisible] = useState(false);
  const ref = useRef<HTMLElement | null>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    const obs = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setVisible(true);
          if (once) obs.unobserve(entry.target);
        }
      },
      { threshold, rootMargin }
    );

    obs.observe(el);
    return () => obs.disconnect();
  }, [threshold, rootMargin, once]);

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const Tag = as as any;

  return (
    <Tag
      ref={ref}
      className={`reveal ${visible ? 'reveal-visible' : ''} ${className}`}
      style={{ ...style, transitionDelay: visible ? `${delay}ms` : undefined }}
    >
      {children}
    </Tag>
  );
}
