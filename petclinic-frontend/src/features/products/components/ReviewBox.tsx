import { useTranslation } from 'react-i18next';
import { JSX, useEffect, useRef, useState } from 'react';
import {
  Alert,
  Button,
  ButtonGroup,
  Form,
  ToggleButton,
} from 'react-bootstrap';
import Stack from 'react-bootstrap/Stack';
import { RatingModel } from '../models/ProductModels/RatingModel';
import './ReviewBox.css';
import { MarkdownReviewBody } from '@/features/products/components/MarkdownReviewBody.tsx';
import {
  FaBold,
  FaItalic,
  FaLink,
  FaList,
  FaListOl,
  FaStrikethrough,
  FaUnderline,
} from 'react-icons/fa';
import { BsTypeH1, BsTypeH2, BsTypeH3 } from 'react-icons/bs';
import { TbBlockquote } from 'react-icons/tb';
import { flushSync } from 'react-dom';

function ReviewBox({
  updateFunc,
  rating,
}: {
  updateFunc: (newReview: string) => void;
  rating: RatingModel;
}): JSX.Element {
  const { t } = useTranslation('products');
  const [reviewText, setReviewText] = useState<string>(rating.review);
  const [isError, setError] = useState<string | null>(null);
  const [previewToggle, setPreviewToggle] = useState<boolean>(true);
  const selectionRef = useRef({ start: 0, end: 0 });
  const textAreaRef = useRef<HTMLTextAreaElement>(null);
  const [hasSelection, setHasSelection] = useState(false);

  useEffect(() => {
    setReviewText(rating.review);
  }, [rating.review]);
  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleLocalChange = (text: string) => {
    setError(text.length > 10000 ? 'reviewTooLong' : null);
    updateFunc(text);
    setReviewText(text);
  };

  const handleToolbarButtonClick = (
    markdownTagStart: string,
    markdownTagEnd?: string,
    lineBased: boolean = false
  ): void => {
    //if (!hasSelection) return;
    flushSync(() =>
      handleLocalChange(
        wrapTextWithMarkdown(
          reviewText,
          markdownTagStart,
          markdownTagEnd ?? '',
          lineBased
        )
      )
    );

    if (!textAreaRef?.current || !hasSelection) return;
    textAreaRef.current.focus();
    textAreaRef.current.setSelectionRange(
      selectionRef.current.start,
      selectionRef.current.end
    );
  };

  const isWrapped = (str: string, start: string, end: string): boolean =>
    str.length >= start.length + end.length &&
    str.startsWith(start) &&
    str.endsWith(end);

  const wrapTextWithMarkdown = (
    text: string,
    markdownTagStart: string,
    markdownTagEnd: string,
    lineBased: boolean
  ): string => {
    if (!textAreaRef?.current) return text;

    let selStart = selectionRef.current.start;
    const selEnd = selectionRef.current.end;

    const markdownStartLen = markdownTagStart.length;

    if (selStart === selEnd) {
      const curLineStartPos = text.lastIndexOf('\n', selStart - 1) + 1;
      let curLineEndPos = text.indexOf('\n', curLineStartPos);
      curLineEndPos = curLineEndPos === -1 ? text.length : curLineEndPos;
      const curLine = text.slice(curLineStartPos, curLineEndPos);

      if (isWrapped(curLine, markdownTagStart, markdownTagEnd)) return text;

      return (
        text.slice(0, curLineStartPos) +
        markdownTagStart +
        curLine +
        markdownTagEnd +
        text.slice(curLineEndPos)
      );
    } else {
      if (lineBased) selStart = text.lastIndexOf('\n', selStart - 1) + 1;

      while (text.charAt(selStart) === '\n') selStart++;

      const raw = text.slice(selStart, selEnd);
      selStart += raw.length - raw.trimStart().length;
      const trimmedEnd = selEnd - (raw.length - raw.trimEnd().length);
      const lines = text.slice(selStart, trimmedEnd).split('\n');

      if (
        !lineBased &&
        text.slice(selStart - markdownStartLen, selStart) ===
          markdownTagStart &&
        text.slice(trimmedEnd, trimmedEnd + markdownTagEnd.length) ===
          markdownTagEnd
      ) {
        selectionRef.current = {
          start: selStart - markdownStartLen,
          end: trimmedEnd - markdownStartLen,
        };
        return (
          text.slice(0, selStart - markdownStartLen) +
          text.slice(selStart, trimmedEnd) +
          text.slice(trimmedEnd + markdownTagEnd.length)
        );
      }

      const linesWithMarkdown = lines
        .map(line => {
          const clean = lineBased
            ? line.replace(/^(#{1,6} |[-*] |\d+\. |> )+/, '')
            : line;
          if (
            clean === '' ||
            isWrapped(clean, markdownTagStart, markdownTagEnd)
          )
            return clean;
          return markdownTagStart + clean + markdownTagEnd;
        })
        .join('\n');

      selectionRef.current = {
        start: selStart + markdownStartLen,
        end: selStart + lines[0].length + markdownStartLen,
      };

      return (
        text.slice(0, selStart) + linesWithMarkdown + text.slice(trimmedEnd)
      );
    }
  };

  return (
    <div className="reviewbox-container">
      <div className="toolbar">
        <ToggleButton
          id="preview-toggle"
          type="checkbox"
          variant={previewToggle ? 'secondary' : 'primary'}
          checked={previewToggle}
          value="1"
          onChange={e => setPreviewToggle(e.target.checked)}
        >
          {previewToggle ? t('hidePreview') : t('showPreview')}
        </ToggleButton>
        <ButtonGroup>
          <Button
            variant="light"
            aria-label={t('boldSelection')}
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('**', '**');
            }}
          >
            <FaBold size={18} />
          </Button>
          <Button
            variant="light"
            aria-label={t('strikeSelection')}
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('~', '~');
            }}
          >
            <FaStrikethrough size={18} />
          </Button>
          <Button
            variant="light"
            aria-label={t('underlineSelection')}
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('++', '++');
            }}
          >
            <FaUnderline size={18} />
          </Button>
          <Button
            variant="light"
            aria-label={t('italicSelection')}
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('*', '*');
            }}
          >
            <FaItalic size={18} />
          </Button>
          <Button
            variant="light"
            aria-label={t('linkSelection')}
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('[', '](https://yourlink.com)');
            }}
          >
            <FaLink size={18} />
          </Button>
          <Button
            variant="light"
            aria-label={t('listSelection')}
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('- ', '', true);
            }}
          >
            <FaList size={18} />
          </Button>
          <Button
            variant="light"
            aria-label={t('numberedListSelection')}
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('1. ', '', true);
            }}
          >
            <FaListOl size={18} />
          </Button>
          <Button
            variant="light"
            aria-label={t('quoteSelection')}
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('> ', '', true);
            }}
          >
            <TbBlockquote size={18} />
          </Button>
          <Button
            variant="light"
            aria-label={t('headingSelection')}
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('# ', '', true);
            }}
          >
            <BsTypeH1 size={18} />
          </Button>
          <Button
            variant="light"
            aria-label={t('subheadingSelection')}
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('## ', '', true);
            }}
          >
            <BsTypeH2 size={18} />
          </Button>
          <Button
            variant="light"
            aria-label={t('subsubheadingSelection')}
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('### ', '', true);
            }}
          >
            <BsTypeH3 size={18} />
          </Button>
        </ButtonGroup>
      </div>
      {isError && <Alert variant="warning">{t(isError)}</Alert>}
      <div className="reviewbox-input-container">
        <Stack direction="horizontal" gap={3}>
          <Form.Control
            as="textarea"
            ref={textAreaRef}
            className={`review-box-textarea ${!previewToggle ? 'full' : ''}`}
            placeholder={t('reviewPlaceholder')}
            value={reviewText}
            onChange={e => handleLocalChange(e.target.value)}
            onSelect={e => {
              setHasSelection(
                e.currentTarget.selectionStart !== e.currentTarget.selectionEnd
              );

              selectionRef.current = {
                start: e.currentTarget.selectionStart ?? 0,
                end: e.currentTarget.selectionEnd ?? 0,
              };
            }}
          />
          {previewToggle && (
            <div className="reviewbox-preview-body">
              <MarkdownReviewBody>
                {reviewText != '' ? reviewText : t('reviewPreviewHint')}
              </MarkdownReviewBody>
            </div>
          )}
        </Stack>
      </div>
    </div>
  );
}

export default ReviewBox;
