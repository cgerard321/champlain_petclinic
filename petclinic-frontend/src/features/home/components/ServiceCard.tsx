import { Card } from 'react-bootstrap';

import './ServiceCard.css';

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export default function ServiceCard({
  icon,
  title,
  desc,
}: {
  icon: React.ReactNode;
  title: string;
  desc: string;
}) {
  return (
    <Card className="shadow-soft compact-card h-100 service-card">
      <Card.Body className="d-flex gap-3">
        <div className="service-emoji" aria-hidden>
          {icon}
        </div>
        <div>
          <Card.Title className="service-title">{title}</Card.Title>
          <Card.Text className="service-desc">{desc}</Card.Text>
        </div>
      </Card.Body>
    </Card>
  );
}
