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
    setError(
      text.length > 10000 ? 'Review cannot exceed 10000 characters!' : null
    );
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

      const lines = text.slice(selStart, selEnd).trim().split('\n');

      const linesWithMarkdown = lines
        .map(line => {
          if (line === '' || isWrapped(line, markdownTagStart, markdownTagEnd))
            return line;
          return markdownTagStart + line + markdownTagEnd;
        })
        .join('\n');

      selectionRef.current = {
        start: selStart + markdownStartLen,
        end: selStart + lines[0].length + markdownStartLen,
      };

      return text.slice(0, selStart) + linesWithMarkdown + text.slice(selEnd);
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
          {previewToggle ? 'Hide preview' : 'Show preview'}
        </ToggleButton>
        <ButtonGroup>
          <Button
            variant="light"
            aria-label="Apply bold to selection"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('**', '**');
            }}
          >
            <FaBold size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Apply strikethrough to selection"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('~', '~');
            }}
          >
            <FaStrikethrough size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Apply underline to selection"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('++', '++');
            }}
          >
            <FaUnderline size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Apply italic to selection"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('*', '*');
            }}
          >
            <FaItalic size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Make selection a link"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('[', '](https://yourlink.com)');
            }}
          >
            <FaLink size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Make selection a list"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('- ', '', true);
            }}
          >
            <FaList size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Make selection a numbered list"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('1. ', '', true);
            }}
          >
            <FaListOl size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Put the selection in a blockquote"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('> ', '', true);
            }}
          >
            <TbBlockquote size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Make the selection a header"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('# ', '', true);
            }}
          >
            <BsTypeH1 size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Make the selection a sub-header"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('## ', '', true);
            }}
          >
            <BsTypeH2 size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Make the selection a sub-sub header"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('### ', '', true);
            }}
          >
            <BsTypeH3 size={18} />
          </Button>
        </ButtonGroup>
      </div>
      {isError && <Alert variant="warning">{isError}</Alert>}
      <div className="reviewbox-input-container">
        <Stack direction="horizontal" gap={3}>
          <Form.Control
            as="textarea"
            ref={textAreaRef}
            className={`review-box-textarea ${!previewToggle ? 'full' : ''}`}
            placeholder="Leave your review here..."
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
                {reviewText != ''
                  ? reviewText
                  : '**Your preview will appear here**, ++markdown is supported++'}
              </MarkdownReviewBody>
            </div>
          )}
        </Stack>
      </div>
    </div>
  );
}

export default ReviewBox;
