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
  FaImage,
  FaItalic,
  FaLink,
  FaList,
  FaListOl,
  FaUnderline,
} from 'react-icons/fa';
import { BsTypeH1, BsTypeH2, BsTypeH3 } from 'react-icons/bs';
import { TbBlockquote } from 'react-icons/tb';

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
  const ref = useRef(null);
  const [hasSelection, setHasSelection] = useState(false);

  useEffect(() => {
    setReviewText(rating.review);
  }, [rating.review]);
  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleLocalChange = (text: string) => {
    if (text.length > 10000) {
      setError('Review cannot exceed 10000 characters!');
    } else {
      setError(null);
      updateFunc(text); // propagate changes to parent
    }
    setReviewText(text);
  };

  const handleToolbarButtonClick = (
    markdownTagStart: string,
    markdownTagEnd?: string
  ) => {
    if (!hasSelection) return;
    handleLocalChange(
      wrapTextWithMarkdown(reviewText, markdownTagStart, markdownTagEnd ?? '')
    );
  };

  const wrapTextWithMarkdown = (
    text: string,
    markdownTagStart: string,
    markdownTagEnd?: string
  ): string => {
    if (!ref) return text;
    let ss = ref.current.selectionStart;

    while (text.charAt(ss) == '\n' && ss != text.length) ss++;

    const se = ref.current.selectionEnd;

    return (
      text.slice(0, ss) +
      markdownTagStart +
      text.slice(ss, se) +
      markdownTagEnd +
      text.slice(se, text.length)
    );
  };

  return (
    <div className="reviewbox-container">
      <div className="toolbar">
        <ToggleButton
          id="preview-toggle"
          type="checkbox"
          variant={previewToggle ? 'primary' : 'secondary'}
          checked={previewToggle}
          value="1"
          onChange={e => setPreviewToggle(e.target.checked)}
        >
          Show Preview
        </ToggleButton>
        <ButtonGroup>
          <Button
            variant="light"
            onMouseDown={() => handleToolbarButtonClick('**', '**')}
          >
            <FaBold size={18} />
          </Button>
          <Button
            variant="light"
            onMouseDown={() => handleToolbarButtonClick('++', '++')}
          >
            <FaUnderline size={18} />
          </Button>
          <Button
            variant="light"
            onMouseDown={() => handleToolbarButtonClick('*', '*')}
          >
            <FaItalic size={18} />
          </Button>
          <Button
            variant="light"
            onMouseDown={() =>
              handleToolbarButtonClick('[', '](https://yourlink.com)')
            }
          >
            <FaLink size={18} />
          </Button>
          <Button
            variant="light"
            onMouseDown={() =>
              handleToolbarButtonClick('![', '](https://yourimage.com)')
            }
          >
            <FaImage size={18} />
          </Button>
          <Button
            variant="light"
            onMouseDown={() => handleToolbarButtonClick('- ')}
          >
            <FaList size={18} />
          </Button>
          <Button
            variant="light"
            onMouseDown={() => handleToolbarButtonClick('1. ')}
          >
            <FaListOl size={18} />
          </Button>
          <Button
            variant="light"
            onMouseDown={() => handleToolbarButtonClick('> ')}
          >
            <TbBlockquote size={18} />
          </Button>
          <Button
            variant="light"
            onMouseDown={() => handleToolbarButtonClick('# ')}
          >
            <BsTypeH1 size={18} />
          </Button>
          <Button
            variant="light"
            onMouseDown={() => handleToolbarButtonClick('## ')}
          >
            <BsTypeH2 size={18} />
          </Button>
          <Button
            variant="light"
            onMouseDown={() => handleToolbarButtonClick('### ')}
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
            className="review-box-textarea"
            placeholder="Leave your review here..."
            value={reviewText}
            ref={ref}
            onChange={e => handleLocalChange(e.target.value)}
            onSelect={e =>
              setHasSelection(e.target.selectionStart !== e.target.selectionEnd)
            }
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
