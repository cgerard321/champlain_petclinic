import { useMemo } from 'react';
import { NavBar } from '@/layouts/AppNavBar';

import { useNavigate } from 'react-router-dom';
import { AppFooter } from '@/layouts/AppFooter';
import { Container, Row, Col, Card, Button, Accordion } from 'react-bootstrap';

import ServiceCard from '@/features/home/components/ServiceCard';
import VetCard from '@/features/home/components/VetCard';
import useFeaturedVets from '@/features/home/hooks/useFeaturedVets';
import { FAQ_ITEMS } from '@/features/faq/data/FaqItems';
import type { FaqItem } from '@/features/faq/models/FaqItem';

import { Reveal } from '@/shared/components';
import { clinic } from '@/shared/content';

import './Home.css';

export default function Home(): JSX.Element {
  const navigate = useNavigate();

  const { vets, photos, tagsByVet, loading, error } = useFeaturedVets(3);

  const highlights = useMemo(
    () => [
      {
        icon: '\uD83C\uDF1F',
        title: '4.9/5 average rating',
        label: 'from pet parents',
      },
      {
        icon: '\uD83D\uDC69\u200D\u2695\uFE0F',
        title: 'Experienced team',
        label: 'Board-certified vets',
      },
      {
        icon: '\u23F0',
        title: 'Same-day appointments',
        label: 'When available',
      },
    ],
    []
  );

  // eslint-disable-next-line react-hooks/exhaustive-deps
  const HOME_FAQ_IDS = ['hours', 'walkins', 'payment'];

  const HOME_FAQ: FaqItem[] = useMemo(
    () => FAQ_ITEMS.filter(i => HOME_FAQ_IDS.includes(i.id)),
    [HOME_FAQ_IDS]
  );

  if (loading) return <div className="page-loading">Loading…</div>;
  if (error) return <div className="page-error">{error}</div>;

  return (
    <div className="home-root">
      <NavBar />

      {/* Intro */}
      <header className="intro">
        <div className="intro-inner">
          <Reveal delay={80}>
            <h1 className="intro-title">
              Welcome to <span>Champlain Pet Clinic</span> 🐾
            </h1>
          </Reveal>
          <Reveal delay={240}>
            <p className="intro-sub">
              At Champlain Pet Clinic, we offer a wide range of services to
              ensure the health and well-being of your beloved pets. Our
              experienced veterinarians and staff are dedicated to providing the
              best care possible.
            </p>
          </Reveal>
          <Reveal delay={500}>
            <div className="intro-ctas">
              <Button variant="primary" onClick={() => navigate('/visits/add')}>
                Book Appointment
              </Button>
              <Button variant="light" onClick={() => navigate('/contact')}>
                Contact Us
              </Button>
            </div>
          </Reveal>
        </div>
      </header>

      {/* Center content */}
      <Container className="py-4">
        {/* Reviews & Info or something like that. Extra info? */}
        <section className="home-section mb-2" aria-label="Highlights">
          <Reveal delay={500}>
            <Row xs={1} md={3} className="g-1">
              {highlights.map(h => (
                <Col key={h.title}>
                  <Card className="shadow-soft stat-card stat-compact h-100">
                    <Card.Body className="d-flex align-items-center gap-2 p-0">
                      <div className="stat-icon">{h.icon}</div>
                      <div>
                        <div className="stat-title fw-semibold">{h.title}</div>
                        <div className="eyebrow">{h.label}</div>
                      </div>
                    </Card.Body>
                  </Card>
                </Col>
              ))}
            </Row>
          </Reveal>
        </section>

        {/* Services at a glance */}
        <section className="home-section" aria-label="Services">
          <Reveal delay={500}>
            <h2 className="section-title text-center mb-2">
              Services at a Glance
            </h2>
          </Reveal>
          <Row xs={1} sm={2} md={3} className="g-2">
            {clinic.services.map((s, i) => (
              <Col key={s.title}>
                <Reveal delay={i * 80 + 500}>
                  <ServiceCard icon={s.icon} title={s.title} desc={s.desc} />
                </Reveal>
              </Col>
            ))}
          </Row>
        </section>

        {/* Featured veterinarians */}
        <section className="home-section" aria-label="Featured veterinarians">
          <h2 className="section-title text-center mb-2">
            Featured Veterinarians
          </h2>
          <Row xs={1} md={3} className="g-2">
            {vets.map((v, i) => (
              <Col key={v.vetId}>
                <Reveal delay={i * 80 + 650}>
                  <VetCard
                    vet={v}
                    photo={photos[v.vetId] || '/images/vet_default.jpg'}
                    tags={tagsByVet[v.vetId] || []}
                    onClick={() => navigate(`/vets/${v.vetId}`)}
                  />
                </Reveal>
              </Col>
            ))}
          </Row>
        </section>

        {/* FAQ */}
        <section id="faq" className="home-section" aria-label="FAQ">
          <h2 className="section-title text-center mb-3">FAQ</h2>
          <Row className="justify-content-center">
            <Col lg={8}>
              <Reveal delay={80}>
                <Accordion alwaysOpen defaultActiveKey={HOME_FAQ[0]?.id}>
                  {HOME_FAQ.map(item => (
                    <Accordion.Item eventKey={item.id} key={item.id}>
                      <Accordion.Header>{item.question}</Accordion.Header>
                      <Accordion.Body>{item.answer}</Accordion.Body>
                    </Accordion.Item>
                  ))}
                </Accordion>
              </Reveal>
            </Col>
          </Row>
          <div className="text-center mt-3">
            <Button
              variant="outline-secondary"
              onClick={() => navigate('/faq')}
            >
              View all FAQs
            </Button>
          </div>
        </section>

        {/* Contact band */}
        <section
          id="contact"
          className="home-section cta-band"
          aria-label="Contact"
        >
          <Row className="align-items-center g-3">
            <Col lg>
              <Reveal delay={80}>
                <h3 className="mb-1">Have questions or need help?</h3>
                <p className="text-muted mb-0">
                  Our team is just a message away.
                </p>
              </Reveal>
            </Col>

            <Col xs="auto" className="d-flex gap-2 ms-lg-auto">
              <Reveal delay={100}>
                <Button
                  variant="outline-secondary"
                  onClick={() => navigate('/contact')}
                >
                  Contact Us
                </Button>
                <Button
                  variant="primary"
                  onClick={() => navigate('/visits/add')}
                >
                  Book Appointment
                </Button>
              </Reveal>
            </Col>
          </Row>
        </section>
      </Container>

      <AppFooter />
    </div>
  );
}
