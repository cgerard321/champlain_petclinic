import { useState, useEffect } from 'react';
import './CardPromos.css';
import { Promo } from '@/features/promos/models/PromoModel.tsx';
import { PromoApi } from '@/features/promos/api/PromoApi.tsx';

export default function CardPromos(): JSX.Element {
  const [promos, setPromos] = useState<Promo[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const fetchPromos = async (): Promise<void> => {
    try {
      const data = await PromoApi.fetchActivePromos();
      setPromos(data);
    } catch (err) {
      console.error('Error fetching promos:', err);
      setError('Failed to fetch promos');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPromos();
  }, []);

  if (loading) {
    return <p>Loading promos...</p>;
  }
  if (error) {
    return <p>{error}</p>;
  }

  return (
      <div className="promo-cards-container">
        <h2>Available Promotions</h2>
        <div className="promo-cards-grid">
          {promos.length > 0 ? (
              promos.map((promo) => (
                  <div className="promo-card" key={promo.code}>
                    <h3>{promo.name}</h3>
                    <p>
                      <strong>Code:</strong> {promo.code}
                    </p>
                    <p>
                      <strong>Discount:</strong> {promo.discount}%
                    </p>
                    <p>
                      <strong>Expires on:</strong>{' '}
                      {new Date(promo.expirationDate).toLocaleDateString()}
                    </p>
                  </div>
              ))
          ) : (
              <div className="empty-promo-card">
                <p style={{ textAlign: 'center', fontStyle: 'italic' }}>Currently, there are no available promotions. Please check back later for exciting offers.</p>
              </div>
          )}
        </div>
      </div>
  );
}
