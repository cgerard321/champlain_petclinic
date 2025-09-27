import { Card, Badge } from 'react-bootstrap';
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel';
import './VetCard.css';

type Props = {
  vet: VetResponseModel;
  photo: string;
  tags: string[];
  onClick: () => void;
};

// eslint-disable-next-line @typescript-eslint/explicit-function-return-type
export default function VetCard({ vet, photo, tags, onClick }: Props) {
  return (
    <Card
      className="vet-card shadow-soft h-100"
      role="button"
      tabIndex={0}
      onClick={onClick}
      onKeyDown={e => (e.key === 'Enter' || e.key === ' ') && onClick()}
      aria-label={`View ${vet.firstName} ${vet.lastName}`}
    >
      <div
        className="vet-media-bg"
        role="img"
        aria-label={`Dr. ${vet.firstName} ${vet.lastName}`}
        style={{ backgroundImage: `url("${photo}")` }}
      />

      <Card.Body className="vet-body text-center">
        <div className="vet-name">
          {vet.firstName} {vet.lastName}
        </div>
        <div className="vet-subtitle">Your friendly vet</div>

        <div className="vet-tags">
          {tags.map(t => (
            <Badge key={t} bg="light" text="dark" className="vet-chip">
              {t}
            </Badge>
          ))}
        </div>
      </Card.Body>
    </Card>
  );
}
