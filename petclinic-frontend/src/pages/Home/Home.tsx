import { useEffect, useState } from 'react';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import './Home.css';
import { getAllVets } from '@/features/veterinarians/api/getAllVets.ts';
import { VetResponseModel } from '@/features/veterinarians/models/VetResponseModel.ts';

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

export default function Home(): JSX.Element {
  const [vets, setVets] = useState<VetResponseModel[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchVets = async () => {
      try {
        const fetchedVets = await getAllVets();
        setVets(fetchedVets);
      } catch (err) {
        setError('Failed to fetch vets');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchVets();
  }, []);

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
          <h1>Vets 🐩</h1>
          <div className="vets-list">
            {vets.map(vet => {
              const services = getRandomServices();
              return (
                <div key={vet.vetId} className="vet-card">
                  <h2>{vet.firstName} {vet.lastName}</h2>
                  <p>🐾 Your friendly vet</p>
                    {services.map((service, index) => (
                      <li key={index}>{service}</li>
                    ))}
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
