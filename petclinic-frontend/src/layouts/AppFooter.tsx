import { Container, Row, Col } from 'react-bootstrap';
import { Link } from 'react-router-dom';

import { contact, clinic } from '@/shared/content';
import { useTranslation } from 'react-i18next';
import './AppFooter.css';

export function AppFooter(): JSX.Element {
  const { t } = useTranslation('home');
  return (
    <footer className="app-footer">
      <Container>
        <Row className="gy-4">
          <Col md={4}>
            <h5 className="footer-brand">
              <span className="icon" aria-hidden="true">
                {'\uD83D\uDC3E'}
              </span>
              {clinic.name}
            </h5>
            <p className="footer-desc">{t('clinicMessage')}</p>
          </Col>

          <Col md={2}>
            <h6>{t('explore')}</h6>
            <ul className="footer-links">
              <li>
                <Link to="/">{t('nav.home')}</Link>
              </li>
              <li>
                <Link to="/vets">{t('nav.vets')}</Link>
              </li>
              <li>
                <Link to="/products">{t('nav.shop')}</Link>
              </li>
            </ul>
          </Col>

          <Col md={3}>
            <h6>{t('support')}</h6>
            <ul className="footer-links">
              <li>
                <Link to="/faq">{t('faq.title')}</Link>
              </li>
              <li>
                <Link to="/privacy">{t('nav.privacy')}</Link>
              </li>
              <li>
                <Link to="/contact">{t('nav.contact')}</Link>
              </li>
            </ul>
          </Col>

          <Col md={3}>
            <h6>{t('contactUs')}</h6>
            <ul className="footer-contact">
              <li>
                <span className="icon" aria-hidden="true">
                  {'\uD83D\uDCCD'}
                </span>
                <span>{clinic.address.street}</span>
              </li>
              <li>
                <span className="icon" aria-hidden="true">
                  {'\uD83D\uDCDE'}
                </span>
                <a href={`tel:+${contact.phone.href}`}>
                  {contact.phone.display}
                </a>
              </li>
              <li>
                <span className="icon" aria-hidden="true">
                  {'\uD83D\uDCE7'}
                </span>
                <a href={`mailto:${contact.email}`}>{contact.email}</a>
              </li>
            </ul>
          </Col>
        </Row>

        <Row className="pt-4 mt-4 border-top border-secondary">
          <Col className="text-center text-muted small">
            © {new Date().getFullYear()} {t('allRightsReserved')}
          </Col>
        </Row>
      </Container>
    </footer>
  );
}
