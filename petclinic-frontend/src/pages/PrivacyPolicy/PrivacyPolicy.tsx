import { Container, Row, Col, Card, Nav } from 'react-bootstrap';

import { NavBar } from '@/layouts/AppNavBar';
import { AppFooter } from '@/layouts/AppFooter';

import './PrivacyPolicy.css';

const LAST_UPDATED = 'September 27, 2025';

const TOC = [
  { id: 'introduction', label: 'Introduction' },
  { id: 'information-we-collect', label: 'Information We Collect' },
  { id: 'how-we-use-info', label: 'How We Use Information' },
  { id: 'legal-bases', label: 'Legal Bases' },
  { id: 'cookies', label: 'Cookies & Tracking' },
  { id: 'sharing', label: 'Sharing & Disclosure' },
  { id: 'retention', label: 'Data Retention' },
  { id: 'security', label: 'Data Security' },
  { id: 'your-rights', label: 'Your Rights' },
  { id: 'children', label: "Children's Privacy" },
  { id: 'changes', label: 'Changes to This Policy' },
  { id: 'contact', label: 'Contact Us' },
];

export default function PrivacyPolicyPage(): JSX.Element {
  return (
    <div className="privacy-root">
      <NavBar />

      <header className="privacy-intro">
        <div className="privacy-intro-inner">
          <h1 className="privacy-title">Privacy Policy</h1>
          <p className="privacy-sub">
            Your privacy matters. This page describes what we collect, how we
            use it, and the choices you have.
          </p>
          <div className="privacy-updated">Last updated: {LAST_UPDATED}</div>
        </div>
      </header>

      <Container className="py-4">
        <Row className="g-3 justify-content-center">
          <Col lg={3}>
            <Card className="shadow-soft policy-card policy-toc">
              <Card.Body>
                <Card.Title className="mb-2">On this page</Card.Title>
                <Nav className="flex-column">
                  {TOC.map(item => (
                    <Nav.Link
                      key={item.id}
                      href={`#${item.id}`}
                      className="toc-link"
                    >
                      {item.label}
                    </Nav.Link>
                  ))}
                </Nav>
              </Card.Body>
            </Card>
          </Col>

          <Col lg={8}>
            <Card className="shadow-soft policy-card">
              <Card.Body>
                <section id="introduction" className="policy-section">
                  <h2>Introduction</h2>
                  <p>
                    Champlain Pet Clinic (“we”, “us”, “our”) provides veterinary
                    services and related products. This Privacy Policy explains
                    how we collect, use, and safeguard your information when you
                    use our website, book appointments, shop, or contact us.
                    This policy is intended to be transparent and easy to read;
                    it is not legal advice.
                  </p>
                </section>

                <section id="information-we-collect" className="policy-section">
                  <h2>Information We Collect</h2>
                  <ul>
                    <li>
                      <strong>Contact & Account Info:</strong> name, email,
                      phone, address, account credentials.
                    </li>
                    <li>
                      <strong>Pet & Visit Details:</strong> pet name,
                      species/breed, medical history you provide, appointment
                      records.
                    </li>
                    <li>
                      <strong>Payments:</strong> billing and transaction details
                      processed via trusted payment providers.
                    </li>
                    <li>
                      <strong>Usage Data:</strong> device/browser info, IP
                      address, pages viewed, and interactions (for security and
                      analytics).
                    </li>
                    <li>
                      <strong>Communications:</strong> messages you send via
                      forms or email, and our replies.
                    </li>
                  </ul>
                </section>

                <section id="how-we-use-info" className="policy-section">
                  <h2>How We Use Information</h2>
                  <ul>
                    <li>
                      Provide veterinary care, schedule appointments, and manage
                      your account.
                    </li>
                    <li>
                      Process orders, payments, and deliver products or
                      services.
                    </li>
                    <li>Respond to messages and provide customer support.</li>
                    <li>Improve our website, services, and user experience.</li>
                    <li>
                      Protect against fraud, abuse, and security risks, and
                      comply with legal obligations.
                    </li>
                  </ul>
                </section>

                <section id="legal-bases" className="policy-section">
                  <h2>Legal Bases</h2>
                  <p>
                    Where applicable (e.g., GDPR), we rely on one or more of the
                    following bases: performance of a contract (appointments,
                    orders), legitimate interests (service improvement,
                    security), consent (where required), and legal obligations.
                  </p>
                </section>

                <section id="cookies" className="policy-section">
                  <h2>Cookies & Tracking</h2>
                  <p>
                    We use necessary cookies to run the site and optional
                    analytics to understand usage. You can control cookies in
                    your browser settings. If we use analytics, we configure
                    them to respect privacy where possible.
                  </p>
                </section>

                <section id="sharing" className="policy-section">
                  <h2>Sharing & Disclosure</h2>
                  <ul>
                    <li>
                      <strong>Service Providers:</strong> payment processors,
                      cloud hosting, email and analytics vendors.
                    </li>
                    <li>
                      <strong>Veterinary Partners:</strong> when coordinating
                      referrals or emergency care with your consent.
                    </li>
                    <li>
                      <strong>Legal & Safety:</strong> when required by law or
                      to protect rights, safety, and security.
                    </li>
                  </ul>
                  <p>We do not sell your personal information.</p>
                </section>

                <section id="retention" className="policy-section">
                  <h2>Data Retention</h2>
                  <p>
                    We keep information only as long as necessary for the
                    purposes described above, to comply with legal requirements,
                    and to resolve disputes. Retention periods may vary based on
                    record-keeping obligations for medical and financial
                    information.
                  </p>
                </section>

                <section id="security" className="policy-section">
                  <h2>Data Security</h2>
                  <p>
                    We implement administrative, technical, and physical
                    measures designed to protect your data. No method of
                    transmission or storage is 100% secure; we work to
                    continuously improve safeguards.
                  </p>
                </section>

                <section id="your-rights" className="policy-section">
                  <h2>Your Rights</h2>
                  <p>
                    Depending on your region, you may have rights to access,
                    correct, delete, or limit use of your data, and to withdraw
                    consent where processing is based on consent. To exercise
                    these rights, contact us using the details below.
                  </p>
                </section>

                <section id="children" className="policy-section">
                  <h2>Children’s Privacy</h2>
                  <p>
                    Our services are intended for adults. We do not knowingly
                    collect personal information from children without
                    appropriate consent.
                  </p>
                </section>

                <section id="changes" className="policy-section">
                  <h2>Changes to This Policy</h2>
                  <p>
                    We may update this Privacy Policy from time to time. We will
                    post the new effective date above and, when appropriate,
                    notify you through the website or by email.
                  </p>
                </section>

                <section id="contact" className="policy-section">
                  <h2>Contact Us</h2>
                  <address className="mb-0">
                    Champlain Pet Clinic
                    <br />
                    900 Rue Riverside, Saint-Lambert, QC
                    <br />
                    <a href="mailto:info@champlainpetclinic.com">
                      info@champlainpetclinic.com
                    </a>
                    <br />
                    <a href="tel:+4506727360">+1 (450) 672-7360</a>
                  </address>
                </section>
              </Card.Body>
            </Card>
          </Col>
        </Row>
      </Container>

      <AppFooter />
    </div>
  );
}
