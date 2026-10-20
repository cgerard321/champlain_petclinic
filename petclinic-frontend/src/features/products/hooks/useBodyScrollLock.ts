import { RefObject, useEffect, useRef } from 'react';

export const useBodyScrollLock = (
  isLocked: boolean,
  onEscape: () => void,
  focusRef: RefObject<HTMLElement | null>
): void => {
  const onEscapeRef = useRef(onEscape);

  useEffect(() => {
    onEscapeRef.current = onEscape;
  }, [onEscape]);

  useEffect(() => {
    if (!isLocked) return;

    const previousPosition = document.body.style.position;
    const previousTop = document.body.style.top;
    const previousLeft = document.body.style.left;
    const previousRight = document.body.style.right;
    const previousWidth = document.body.style.width;
    const previousOverflowY = document.documentElement.style.overflowY;
    const scrollPosition = window.scrollY;

    document.body.style.position = 'fixed';
    document.body.style.top = `-${scrollPosition}px`;
    document.body.style.left = '0';
    document.body.style.right = '0';
    document.body.style.width = '100%';
    document.documentElement.style.overflowY = 'scroll';

    focusRef.current?.focus();
    const handleEscape = (event: KeyboardEvent): void => {
      if (event.key === 'Escape') onEscapeRef.current();
    };
    document.addEventListener('keydown', handleEscape);

    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.position = previousPosition;
      document.body.style.top = previousTop;
      document.body.style.left = previousLeft;
      document.body.style.right = previousRight;
      document.body.style.width = previousWidth;
      document.documentElement.style.overflowY = previousOverflowY;
      window.scrollTo({
        top: scrollPosition,
        left: 0,
        behavior: 'instant',
      });
    };
  }, [focusRef, isLocked]);
};
