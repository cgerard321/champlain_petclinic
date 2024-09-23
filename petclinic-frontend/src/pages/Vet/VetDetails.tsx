import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import './VetDetails.css';

interface VetResponseType {
  vetId: string;
  vetBillId: string;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  resume: string;
  workday: string[];
  workHoursJson: string;
  active: boolean;
  specialties: { name: string }[];
}

export default function VetDetails(): JSX.Element {
  const { vetId } = useParams<{ vetId: string }>();
  const [vet, setVet] = useState<VetResponseType | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchVetDetails = async (): Promise<void> => {
      try {
        const response = await fetch(
          `http://localhost:8080/api/gateway/vets/${vetId}`
        );

        if (!response.ok) {
          throw new Error(`Error: ${response.statusText}`);
        }
        const data: VetResponseType = await response.json();
        setVet(data);
        setLoading(false);
      } catch (error) {
        setError('Failed to fetch vet details');
        setLoading(false);
      }
    };

    fetchVetDetails();
  }, [vetId]);

  const renderWorkHours = (workHoursJson: string): JSX.Element => {
    try {
      const workHours: Record<string, string[]> = JSON.parse(workHoursJson);
      return (
        <div>
          {Object.entries(workHours).map(([day, hours], index) => (
            <div key={index}>
              <strong>{day}:</strong>
              <ul>
                {hours.map((hour, idx) => (
                  <li key={idx}>{hour}</li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      );
    } catch (error) {
      console.error('Error parsing work hours:', error);
      return <p>Invalid work hours data</p>;
    }
  };

  if (loading) return <p>Loading vet details...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div>
      <NavBar />
      <div className="vet-details-container">
        <h1>Vet Information</h1>
        {vet && (
          <>
            <section className="vet-info">
              <h2>Veterinarian</h2>
              <p>
                <strong>Name:</strong> {vet.firstName} {vet.lastName}
              </p>
            </section>

            <section className="contact-info">
              <h2>Contact Information</h2>
              <p>
                <strong>Email Address:</strong> {vet.email}
              </p>
              <p>
                <strong>Phone Number:</strong> {vet.phoneNumber}
              </p>
            </section>

            <section className="work-info">
              <h2>Work Information</h2>
              <p>
                <strong>Resume:</strong> {vet.resume}
              </p>
              <p>
                <strong>Workdays:</strong>
                {vet.workday && vet.workday.length > 0 ? (
                  <ul>
                    {vet.workday.map((workday, index) => (
                      <li key={index}>{workday}</li>
                    ))}
                  </ul>
                ) : (
                  <p>No workdays available</p>
                )}
              </p>
              <p>
                <strong>Work Hours:</strong>{' '}
                {renderWorkHours(vet.workHoursJson)}
              </p>
              <p>
                <strong>Active:</strong> {vet.active ? 'Yes' : 'No'}
              </p>
            </section>

            <section className="specialties-info">
              <h2>Specialties</h2>
              {vet.specialties && vet.specialties.length > 0 ? (
                <ul>
                  {vet.specialties.map((specialty, index) => (
                    <li key={index}>{specialty.name}</li>
                  ))}
                </ul>
              ) : (
                <p>No specialties available</p>
              )}
            </section>
          </>
        )}
      </div>
    </div>
  );
}
