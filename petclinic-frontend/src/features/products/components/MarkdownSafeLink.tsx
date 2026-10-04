import { JSX, ReactNode, useState } from 'react';
import { Button, Modal } from 'react-bootstrap';
import './MarkdownSafeLink.css';

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
    <>
      <Modal
        centered
        show={show}
        dialogClassName="markdown-link-safety-modal-dialog"
      >
        <button
          className="delete-modal-close-btn"
          onClick={() => setShow(false)}
        >
          ×
        </button>
        <Modal.Body className="markdown-link-safety-modal-body">
          <h4>External Link</h4>
          <p>
            You are about to leave this site. External links may{' '}
            <strong className="safety-accent">not be safe</strong>. Do you want
            to continue?
          </p>
          <p>
            <strong>Link: </strong>
            {href}
          </p>
          <div className="markdown-link-safety-modal-buttons">
            <Button
              variant="danger"
              onClick={() => window.open(href, '_blank', 'noopener,noreferrer')}
            >
              Continue
            </Button>
            <Button variant="secondary" onClick={() => setShow(false)}>
              Go back
            </Button>
          </div>
        </Modal.Body>
      </Modal>
      <a href={href} onClick={handleLinkClick}>
        {children}
      </a>
    </>
  );
}
