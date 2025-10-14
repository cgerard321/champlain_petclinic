import { Container, Row, Col, Accordion, Form, Button } from 'react-bootstrap';
import { NavBar } from '@/layouts/AppNavBar';
import { AppFooter } from '@/layouts/AppFooter';
import useFaqSearch from '@/features/faq/hooks/useFaqSearch';

import './FAQ.css';

import { Reveal } from '@/shared/components';

export default function FAQ(): JSX.Element {
  const { query, setQuery, results } = useFaqSearch();

  return (
    <div className="faq-root">
      <NavBar />

      <header className="faq-intro">
        <div className="faq-intro-inner">
          <h1 className="faq-title">Frequently Asked Questions</h1>
          <p className="faq-sub">
            Quick answers to the questions we hear most often.
          </p>

          <Form className="faq-search" onSubmit={e => e.preventDefault()}>
            <Form.Control
              type="search"
              placeholder="Search FAQs…"
              value={query}
              onChange={e => setQuery(e.target.value)}
              aria-label="Search FAQs"
            />
          </Form>
        </div>
      </header>

      <Container className="py-4">
        <Row className="justify-content-center">
          <Col lg={8}>
            <Accordion
              alwaysOpen
              flush
              className="faq-accordion"
              defaultActiveKey={results[0]?.id}
            >
              {results.map(item => (
                <Reveal key={item.id} delay={results.indexOf(item) * 50}>
                  <Accordion.Item eventKey={item.id} id={item.id} key={item.id}>
                    <Accordion.Header>{item.question}</Accordion.Header>
                    <Accordion.Body>{item.answer}</Accordion.Body>
                  </Accordion.Item>
                </Reveal>
              ))}
              {results.length === 0 && (
                <div className="text-center text-muted py-4">
                  No results. Try a different search.
                </div>
              )}
            </Accordion>

            <div className="text-center mt-3">
              <Button variant="outline-secondary" href="/contact">
                Still need help? Contact us
              </Button>
            </div>
          </Col>
        </Row>
      </Container>

      <AppFooter />
    </div>
  );
}
