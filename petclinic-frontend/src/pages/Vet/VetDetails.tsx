import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import './VetDetails.css';
import axios from 'axios';

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
  const [photo, setPhoto] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isFormOpen, setIsFormOpen] = useState(false); // To handle form visibility
  const [specialtyId, setSpecialtyId] = useState('');
  const [specialtyName, setSpecialtyName] = useState('');

  useEffect(() => {
    const fetchVetDetails = async (): Promise<void> => {
      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/vets/${vetId}`
        );

        if (!response.ok) {
          throw new Error(`Error: ${response.statusText}`);
        }
        const data: VetResponseType = await response.json();
        setVet(data);
      } catch (error) {
        setError('Failed to fetch vet details');
      }
    };

    const fetchVetPhoto = async (): Promise<void> => {
      try {
        const response = await fetch(
          `http://localhost:8080/api/v2/gateway/vets/${vetId}/photo`,
          {
            method: 'GET',
            headers: {
              Accept: 'image/*',
            },
          }
        );

        if (!response.ok) {
          throw new Error(`Error: ${response.statusText}`);
        }

        const blob = await response.blob();
        const imageUrl = URL.createObjectURL(blob);
        setPhoto(imageUrl);
      } catch (error) {
        setError('Failed to fetch vet photo');
      }
    };

    //fetch both vet details and photo
    fetchVetDetails().then(() => {
      fetchVetPhoto();
      setLoading(false);
    });
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

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleAddSpecialty = async () => {
    const specialtyDTO = {
      specialtyId,
      name: specialtyName,
    };

    try {
      await axios.post(
        `http://localhost:8080/api/v2/gateway/vets/${vetId}/specialties`,
        specialtyDTO
      );
      alert('Specialty added successfully!');
      setIsFormOpen(false); // Close form on success
      setSpecialtyId(''); // Clear fields
      setSpecialtyName('');

      // Update the vet data after adding the specialty
      setVet(prevVet =>
        prevVet
          ? {
              ...prevVet,
              specialties: [...prevVet.specialties, specialtyDTO], // Update specialties locally
            }
          : null
      );
    } catch (error) {
      setError('Failed to add specialty');
    }
  };

  if (loading) return <p>Loading vet details...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div>
      <NavBar />
      <div className="vet-details-container">
        <h1>Vet Information</h1>

        {photo && (
          <section className="vet-photo-container">
            <img src={photo} alt="Vet" className="vet-photo" />
          </section>
        )}

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

              {/* Button to open the form */}
              <button onClick={() => setIsFormOpen(true)}>Add Specialty</button>

              {/* Conditionally render the form */}
              {isFormOpen && (
                <div className="specialty-form-popup">
                  <form
                    onSubmit={e => {
                      e.preventDefault();
                      handleAddSpecialty();
                    }}
                  >
                    <div>
                      <label htmlFor="specialtyId">Specialty ID:</label>
                      <input
                        type="text"
                        id="specialtyId"
                        value={specialtyId}
                        onChange={e => setSpecialtyId(e.target.value)}
                        required
                      />
                    </div>
                    <div>
                      <label htmlFor="specialtyName">Specialty Name:</label>
                      <input
                        type="text"
                        id="specialtyName"
                        value={specialtyName}
                        onChange={e => setSpecialtyName(e.target.value)}
                        required
                      />
                    </div>
                    <div>
                      <button type="submit">Submit</button>
                      <button
                        type="button"
                        onClick={() => setIsFormOpen(false)}
                      >
                        Cancel
                      </button>
                    </div>
                  </form>
                </div>
              )}
            </section>
          </>
        )}
      </div>
    </div>
  );
}
