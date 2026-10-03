import { JSX, ReactNode, useState } from 'react';
import { Button, ButtonGroup, Modal } from 'react-bootstrap';
import { redirect } from 'react-router-dom';

interface MarkdownSafeLinkProps {
  href: string;
  children: ReactNode;
}

export function MarkdownSafeLink({
  href,
  children,
}: MarkdownSafeLinkProps): JSX.Element {
  const [show, setShow] = useState(false);
  const handleLinkClick = e => {
    e.preventDefault();
    setShow(true);
  };
  return (
    <div>
      <Modal
        centered
        show={show}
        dialogClassName="markdown-link-safety-modal-dialog"
      >
        <Modal.Body className="markdown-link-safety-modal-body">
          <h4>External Link</h4>
          <span>
            You're about to leave this site. External links may{' '}
            <strong>not be safe</strong>. Do you want to continue?
          </span>
        </Modal.Body>
        <div className="markdown-link-safety-modal-buttons">
          <Button
            variant="danger"
            onClick={() => window.open(href, '_blank', 'noopener,noreferrer')}
          >
            Continue
          </Button>
          <Button
            variant="secondary"
            className="cancel-btn"
            onClick={() => setShow(false)}
          >
            Go back
          </Button>
        </div>
      </Modal>
      <a href={href} onClick={handleLinkClick}>
        {children}
      </a>
    </div>
  );
}
