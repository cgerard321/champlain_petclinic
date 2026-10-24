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
import { useTranslation } from 'react-i18next';

import './Contact.css';

type Status = {
  type: 'idle' | 'submitting' | 'success' | 'error';
};

export default function ContactPage(): JSX.Element {
  const [validated, setValidated] = useState(false);
  const [status, setStatus] = useState<Status>({ type: 'idle' });
  const { t } = useTranslation('contact');

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
      });
    }
  };

  const submitting = status.type === 'submitting';

  return (
    <div className="contact-root">
      <NavBar />

      <header className="contact-intro">
        <div className="contact-intro-inner">
          <h1 className="contact-title">{t('intro.title')}</h1>
          <p className="contact-sub">{t('intro.subtitle')}</p>
        </div>
      </header>

      <Container className="py-4">
        <Row className="g-3 justify-content-center">
          <Col lg={4}>
            <Reveal>
              <Card className="shadow-soft contact-card h-100">
                <Card.Body>
                  <Card.Title className="mb-2">{t('details.title')}</Card.Title>
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

                  <Card.Title className="mb-2">
                    {t('details.hoursTitle')}
                  </Card.Title>
                  <div className="hours">
                    {contact.hours.map(h => (
                      <div key={h.id}>
                        <span>{t(`hours.day.${h.id}`)}</span>
                        <span>
                          {'note' in h && h.note
                            ? t('hours.closed')
                            : h.open && h.close
                              ? `${h.open}–${h.close}`
                              : t('hours.closed')}
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
                      {t('details.directions')}
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
                  <Card.Title className="mb-2">{t('form.title')}</Card.Title>

                  <div aria-live="polite" className="mb-2">
                    {status.type === 'success' && (
                      <Alert variant="success" className="py-2 mb-2">
                        {t('status.success')}
                      </Alert>
                    )}
                    {status.type === 'error' && (
                      <Alert variant="danger" className="py-2 mb-2">
                        {t('status.error')}
                      </Alert>
                    )}
                  </div>

                  <Form noValidate validated={validated} onSubmit={onSubmit}>
                    <Form.Group className="mb-2" controlId="contactName">
                      <Form.Label>{t('form.name.label')}</Form.Label>
                      <Form.Control
                        name="name"
                        value={formData.name}
                        onChange={onChange}
                        placeholder={t('form.name.placeholder')}
                        required
                      />
                      <Form.Control.Feedback type="invalid">
                        {t('form.name.error')}
                      </Form.Control.Feedback>
                    </Form.Group>

                    <Row className="g-2">
                      <Col md>
                        <Form.Group className="mb-2" controlId="contactEmail">
                          <Form.Label>{t('form.email.label')}</Form.Label>
                          <Form.Control
                            type="email"
                            name="email"
                            value={formData.email}
                            onChange={onChange}
                            placeholder={t('form.email.placeholder')}
                            required
                          />
                          <Form.Control.Feedback type="invalid">
                            {t('form.email.error')}
                          </Form.Control.Feedback>
                        </Form.Group>
                      </Col>
                      <Col md>
                        <Form.Group className="mb-2" controlId="contactPhone">
                          <Form.Label>{t('form.phone.label')}</Form.Label>
                          <Form.Control
                            name="phone"
                            value={formData.phone}
                            onChange={onChange}
                            placeholder={t('form.phone.placeholder')}
                          />
                        </Form.Group>
                      </Col>
                    </Row>

                    <Form.Group className="mb-2" controlId="contactSubject">
                      <Form.Label>{t('form.subject.label')}</Form.Label>
                      <Form.Control
                        name="subject"
                        value={formData.subject}
                        onChange={onChange}
                        placeholder={t('form.subject.placeholder')}
                      />
                    </Form.Group>

                    <Form.Group className="mb-3" controlId="contactMessage">
                      <Form.Label>{t('form.message.label')}</Form.Label>
                      <Form.Control
                        as="textarea"
                        rows={5}
                        name="message"
                        value={formData.message}
                        onChange={onChange}
                        placeholder={t('form.message.placeholder')}
                        required
                      />
                      <Form.Control.Feedback type="invalid">
                        {t('form.message.error')}
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
                        {submitting ? t('form.sending') : t('form.send')}
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
                        {t('form.clear')}
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
