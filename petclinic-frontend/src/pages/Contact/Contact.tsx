import { useState } from 'react';
import {
  Container,
  Row,
  Col,
  Card,
  Form,
  Button,
  Alert,
} from 'react-bootstrap';
import { NavBar } from '@/layouts/AppNavBar';
import { AppFooter } from '@/layouts/AppFooter';
import { Reveal } from '@/shared/components';
import { contact, clinic } from '@/shared/content';

import './Contact.css';

type Status = {
  type: 'idle' | 'submitting' | 'success' | 'error';
  message?: string;
};

export default function ContactPage(): JSX.Element {
  const [validated, setValidated] = useState(false);
  const [status, setStatus] = useState<Status>({ type: 'idle' });

  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phone: '',
    subject: '',
    message: '',
    website: '',
  });

  const onChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
    // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  ) => {
    const { name, value } = e.target;
    setFormData(fd => ({ ...fd, [name]: value }));
  };

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const onSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    e.stopPropagation();

    const form = e.currentTarget;
    setValidated(true);
    if (!form.checkValidity()) return;

    if (formData.website) return;

    try {
      /* Try implementing the mailer GO service call here in the future */

      setStatus({
        type: 'success',
        message: 'Thanks! Your message has been sent.',
      });
      setFormData({
        name: '',
        email: '',
        phone: '',
        subject: '',
        message: '',
        website: '',
      });
      setValidated(false);
    } catch (err) {
      console.error(err);
      setStatus({
        type: 'error',
        message: 'Something went wrong. Please try again.',
      });
    }
  };

  const submitting = status.type === 'submitting';

  return (
    <div className="contact-root">
      <NavBar />

      <header className="contact-intro">
        <div className="contact-intro-inner">
          <h1 className="contact-title">Contact Us</h1>
          <p className="contact-sub">
            We’ll get back to you as soon as we can.
          </p>
        </div>
      </header>

      <Container className="py-4">
        <Row className="g-3 justify-content-center">
          <Col lg={4}>
            <Reveal>
              <Card className="shadow-soft contact-card h-100">
                <Card.Body>
                  <Card.Title className="mb-2">Clinic Details</Card.Title>
                  <div className="contact-detail">
                    <span className="icon" aria-hidden="true">
                      {'\uD83D\uDCCD'}
                    </span>
                    <div>
                      <div>{clinic.address.street}</div>
                      <div>
                        {clinic.address.city}, {clinic.address.province}
                      </div>
                    </div>
                  </div>
                  <div className="contact-detail">
                    <span className="icon" aria-hidden="true">
                      {'\uD83D\uDCDE'}
                    </span>
                    <a href={`tel:${contact.phone.href}`}>
                      {contact.phone.display}
                    </a>
                  </div>
                  <div className="contact-detail">
                    <span className="icon" aria-hidden="true">
                      {'\uD83D\uDCE7'}
                    </span>
                    <a href={`mailto:${contact.email}`}>{contact.email}</a>
                  </div>

                  <hr className="my-3" />

                  <Card.Title className="mb-2">Hours</Card.Title>
                  <div className="hours">
                    {contact.hours.map(h => (
                      <div key={h.days}>
                        <span>{h.days}</span>
                        <span>
                          {'note' in h && h.note
                            ? h.note
                            : h.open && h.close
                              ? `${h.open}–${h.close}`
                              : 'Closed'}
                        </span>
                      </div>
                    ))}
                  </div>

                  <div className="mt-3">
                    <a
                      href={clinic.address.mapsUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="map-link"
                    >
                      Get directions →
                    </a>
                  </div>
                </Card.Body>
              </Card>
            </Reveal>
          </Col>

          <Col lg={6}>
            <Reveal>
              <Card className="shadow-soft contact-card h-100">
                <Card.Body>
                  <Card.Title className="mb-2">Send a Message</Card.Title>

                  <div aria-live="polite" className="mb-2">
                    {status.type === 'success' && (
                      <Alert variant="success" className="py-2 mb-2">
                        {status.message}
                      </Alert>
                    )}
                    {status.type === 'error' && (
                      <Alert variant="danger" className="py-2 mb-2">
                        {status.message}
                      </Alert>
                    )}
                  </div>

                  <Form noValidate validated={validated} onSubmit={onSubmit}>
                    <Form.Group className="mb-2" controlId="contactName">
                      <Form.Label>Name</Form.Label>
                      <Form.Control
                        name="name"
                        value={formData.name}
                        onChange={onChange}
                        placeholder="Jane Doe"
                        required
                      />
                      <Form.Control.Feedback type="invalid">
                        Please enter your name.
                      </Form.Control.Feedback>
                    </Form.Group>

                    <Row className="g-2">
                      <Col md>
                        <Form.Group className="mb-2" controlId="contactEmail">
                          <Form.Label>Email</Form.Label>
                          <Form.Control
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={onChange}
                            placeholder="jane@example.com"
                            required
                          />
                          <Form.Control.Feedback type="invalid">
                            Please enter a valid email.
                          </Form.Control.Feedback>
                        </Form.Group>
                      </Col>
                      <Col md>
                        <Form.Group className="mb-2" controlId="contactPhone">
                          <Form.Label>Phone (optional)</Form.Label>
                          <Form.Control
                            name="phone"
                            value={formData.phone}
                            onChange={onChange}
                            placeholder="(555) 555-5555"
                          />
                        </Form.Group>
                      </Col>
                    </Row>

                    <Form.Group className="mb-2" controlId="contactSubject">
                      <Form.Label>Subject</Form.Label>
                      <Form.Control
                        name="subject"
                        value={formData.subject}
                        onChange={onChange}
                        placeholder="How can we help?"
                      />
                    </Form.Group>

                    <Form.Group className="mb-3" controlId="contactMessage">
                      <Form.Label>Message</Form.Label>
                      <Form.Control
                        as="textarea"
                        rows={5}
                        name="message"
                        value={formData.message}
                        onChange={onChange}
                        placeholder="Write your message…"
                        required
                      />
                      <Form.Control.Feedback type="invalid">
                        Please enter a message.
                      </Form.Control.Feedback>
                    </Form.Group>

                    <input
                      type="text"
                      name="website"
                      value={formData.website}
                      onChange={onChange}
                      className="hp"
                      tabIndex={-1}
                      autoComplete="off"
                    />

                    <div className="d-flex gap-2">
                      <Button type="submit" disabled={submitting}>
                        {submitting ? 'Sending…' : 'Send Message'}
                      </Button>
                      <Button
                        variant="outline-secondary"
                        type="button"
                        onClick={() => {
                          setFormData({
                            name: '',
                            email: '',
                            phone: '',
                            subject: '',
                            message: '',
                            website: '',
                          });
                          setValidated(false);
                        }}
                        disabled={submitting}
                      >
                        Clear
                      </Button>
                    </div>
                  </Form>
                </Card.Body>
              </Card>
            </Reveal>
          </Col>
        </Row>
      </Container>

      <AppFooter />
    </div>
  );
}
