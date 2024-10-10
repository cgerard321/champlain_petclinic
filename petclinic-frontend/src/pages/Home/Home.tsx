import { useEffect, useState } from 'react';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import './Home.css';
import { getAllVets } from '@/features/veterinarians/api/getAllVets.ts';
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel.ts';
import { useParams, useNavigate } from 'react-router-dom';

const vetServices = [
  '✔️ General Check-ups',
  '✔️ Vaccinations',
  '✔️ Dental Care',
  '✔️ Surgery',
  '✔️ Emergency Services',
  '✔️ Pet Grooming',
  '✔️ Nutritional Advice',
];

function getRandomServices(): string[] {
  const shuffled = vetServices.sort(() => 0.5 - Math.random());
  return shuffled.slice(0, 2);
}

function getRandomVets(vets: VetResponseModel[]): VetResponseModel[] {
  const shuffled = vets.sort(() => 0.5 - Math.random());
  return shuffled.slice(0, 3);
}

export default function Home(): JSX.Element {
  useParams<{ vetId: string }>();
  const navigate = useNavigate();
  const [, setVets] = useState<VetResponseModel[]>([]);
  const [randomVets, setRandomVets] = useState<VetResponseModel[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [vetPhotos, setVetPhotos] = useState<Record<string, string>>({});

  const fetchVetPhoto = async (vetId: string): Promise<void> => {
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
      setVetPhotos(prevPhotos => ({
        ...prevPhotos,
        [vetId]: imageUrl,
      }));
    } catch (error) {
      console.error('Failed to fetch vet photo:', error);
      setVetPhotos(prevPhotos => ({
        ...prevPhotos,
        [vetId]: '/images/vet_default.jpg',
      }));
    }
  };

  useEffect(() => {
    // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
    const fetchVets = async () => {
      try {
        const fetchedVets = await getAllVets();
        setVets(fetchedVets);
        const selectedVets = getRandomVets(fetchedVets);
        setRandomVets(selectedVets);
        selectedVets.forEach(vet => {
          fetchVetPhoto(vet.vetId);
        });
      } catch (err) {
        setError('Failed to fetch vets');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchVets();
  }, []);

  const handleCardClick = (vetId: string): void => {
    navigate(`/vets/${vetId}`);
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>{error}</div>;

  return (
    <div className="home-container">
      <NavBar />
      <div className="home-content">
        <h1>Welc🐶me to Champlain Pet Clinic</h1>
        <p>
          At Champlain Pet Clinic, we offer a wide range of services to ensure
          the health and well-being of your beloved pets. Our experienced
          veterinarians and staff are dedicated to providing the best care
          possible.
        </p>

        <div>
          <h1>Meet Our Vets 🐩</h1>
          <div className="vets-list">
            {randomVets.map(vet => {
              const services = getRandomServices();
              const vetPhoto =
                vetPhotos[vet.vetId] || '/images/vet_default.jpg';
              return (
                <div
                  key={vet.vetId}
                  className="vet-card"
                  onClick={() => handleCardClick(vet.vetId)}
                  style={{ cursor: 'pointer' }}
                >
                  <h2>
                    {vet.firstName} {vet.lastName}
                  </h2>
                  <p>🐾 Your friendly vet</p>
                  <div className="vet-photo-container">
                    <img src={vetPhoto} alt="Vet" className="vet-photo" />
                  </div>
                  <ul>
                    {services.map((service, index) => (
                      <li key={index}>{service}</li>
                    ))}
                  </ul>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
