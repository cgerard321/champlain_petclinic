import { Container, Row, Col } from 'react-bootstrap';
import { Link } from 'react-router-dom';
import './AppFooter.css';

export function AppFooter(): JSX.Element {
  return (
    <footer className="app-footer">
      <Container>
        <Row className="gy-4">
          <Col md={4}>
            <h5 className="footer-brand">
              <span className="icon" aria-hidden="true">
                {'\uD83D\uDC3E'}
              </span>
              PetClinic
            </h5>
            <p className="footer-desc">
              Providing compassionate veterinary care to keep your pets healthy
              and happy.
            </p>
          </Col>

          <Col md={2}>
            <h6>Explore</h6>
            <ul className="footer-links">
              <li>
                <Link to="/">Home</Link>
              </li>
              <li>
                <Link to="/vets">Veterinarians</Link>
              </li>
              <li>
                <Link to="/products">Shop</Link>
              </li>
            </ul>
          </Col>

          <Col md={3}>
            <h6>Support</h6>
            <ul className="footer-links">
              <li>
                <Link to="/faq">FAQ</Link>
              </li>
              <li>
                <Link to="/privacy">Privacy Policy</Link>
              </li>
              <li>
                <Link to="/contact">Contact</Link>
              </li>
            </ul>
          </Col>

          <Col md={3}>
            <h6>Contact Us</h6>
            <ul className="footer-contact">
              <li>
                <span className="icon" aria-hidden="true">
                  {'\uD83D\uDCCD'}
                </span>
                <span>900 Rue Riverside</span>
              </li>
              <li>
                <span className="icon" aria-hidden="true">
                  {'\uD83D\uDCDE'}
                </span>
                <a href="tel:+14506727360">(450) 672-7360</a>
              </li>
              <li>
                <span className="icon" aria-hidden="true">
                  {'\uD83D\uDCE7'}
                </span>
                <a href="mailto:info@champlainpetclinic.com">
                  info@champlainpetclinic.com
                </a>
              </li>
            </ul>
          </Col>
        </Row>

        <Row className="pt-4 mt-4 border-top border-secondary">
          <Col className="text-center text-muted small">
            © {new Date().getFullYear()} Champlain Pet Clinic. All rights
            reserved.
          </Col>
        </Row>
      </Container>
    </footer>
  );
}
