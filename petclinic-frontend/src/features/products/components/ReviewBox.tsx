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
    markdownTagEnd?: string
  ): void => {
    if (!hasSelection) return;
    flushSync(() =>
      handleLocalChange(
        wrapTextWithMarkdown(reviewText, markdownTagStart, markdownTagEnd ?? '')
      )
    );

    if (!textAreaRef.current) return;
    textAreaRef.current.focus();
    textAreaRef.current.setSelectionRange(
      selectionRef.current.start,
      selectionRef.current.end
    );
  };

  const wrapTextWithMarkdown = (
    text: string,
    markdownTagStart: string,
    markdownTagEnd?: string
  ): string => {
    if (!textAreaRef || !textAreaRef.current) return text;
    let ss = selectionRef.current.start;

    while (text.charAt(ss) == '\n' && ss != text.length) ss++;

    const se = selectionRef.current.end;

    const lines = text.slice(ss, se).split('\n');

    const linesWithMarkdown = lines
      .map(line => {
        if (line == '') return line;
        return markdownTagStart + line + markdownTagEnd;
      })
      .join('\n');

    selectionRef.current = {
      start: ss + markdownTagStart.length,
      end: ss + lines[0].length + markdownTagStart.length,
    };

    return text.slice(0, ss) + linesWithMarkdown + text.slice(se, text.length);
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
              handleToolbarButtonClick('- ');
            }}
          >
            <FaList size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Make selection a numbered list"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('1. ');
            }}
          >
            <FaListOl size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Put the selection in a blockquote"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('> ');
            }}
          >
            <TbBlockquote size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Make the selection a header"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('# ');
            }}
          >
            <BsTypeH1 size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Make the selection a sub-header"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('## ');
            }}
          >
            <BsTypeH2 size={18} />
          </Button>
          <Button
            variant="light"
            aria-label="Make the selection a sub-sub header"
            onMouseDown={e => {
              e.preventDefault();
              handleToolbarButtonClick('### ');
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
