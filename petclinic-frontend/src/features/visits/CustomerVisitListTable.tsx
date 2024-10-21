import { useEffect, useState } from 'react';
import { useUser } from '@/context/UserContext';
import { Visit } from '@/features/visits/models/Visit.ts';
import { getAllOwnerVisits } from './api/getAllOwnerVisits';
import { AppRoutePaths } from '@/shared/models/path.routes';
import { useNavigate } from 'react-router-dom';

export default function CustomerVisitListTable(): JSX.Element {
  const { user } = useUser();
  const [visits, setVisits] = useState<Visit[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!user.userId) return;

    const fetchVisits = async (): Promise<void> => {
      try {
        const visitData = await getAllOwnerVisits(user.userId);
        if (Array.isArray(visitData)) {
          setVisits(visitData);
        } else {
          console.error('Fetched data is not an array', visitData);
        }
      } catch (err) {
        if (err instanceof Error) {
          setError(`Failed to fetch visits: ${err.message}`);
        } else {
          setError('Failed to fetch visits');
        }
      }
    };

    fetchVisits();
  }, [user.userId]);

  const navigate = useNavigate();

  return (
    <div>
      <div className="visit-actions">
        <button
          className="btn btn-warning"
          onClick={() => navigate(AppRoutePaths.CustomerAddReview)}
          title="Leave a Review"
        >
          Leave a Review
        </button>
        <button
          className="btn btn-dark"
          onClick={() => navigate(AppRoutePaths.CustomerReviews)}
          title="View Reviews"
        >
          View Reviews
        </button>
      </div>
      <button
        className="btn btn-primary mb-3"
        onClick={() => navigate(AppRoutePaths.AddVisitForOwner)}
      >
        Book Appointment
      </button>
      {error ? (
        <p>{error}</p>
      ) : (
        <table className="table table-striped">
          <thead>
            <tr>
              <th>Visit ID</th>
              <th>Pet Name</th>
              <th>Visit Date</th>
              <th>Visit Description</th>
              <th>Vet Name</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {visits.map(visit => (
              <tr key={visit.visitId}>
                <td>{visit.visitId}</td>
                <td>{visit.petName}</td>
                <td>{visit.visitDate}</td>
                <td>{visit.description}</td>
                <td>{`${visit.vetFirstName} ${visit.vetLastName}`}</td>
                <td
                  style={{
                    color:
                      visit.status === 'CONFIRMED'
                        ? 'green'
                        : visit.status === 'UPCOMING'
                          ? 'orange'
                          : visit.status === 'CANCELLED'
                            ? 'red'
                            : visit.status === 'COMPLETED'
                              ? 'blue'
                              : visit.status === 'ARCHIVED'
                                ? 'gray'
                                : 'inherit',
                    fontWeight: 'bold',
                  }}
                >
                  {visit.status}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
