import { useState, useEffect } from 'react';
import './CardPromos.css';

const mockPromos = [
  {
    name: 'Summer Sale',
    code: 'SUMMER2024',
    discount: 20,
    expirationDate: '2024-12-31',
  },
  {
    name: 'Black Friday Deal',
    code: 'BLACKFRIDAY',
    discount: 50,
    expirationDate: '2024-11-29',
  },
  {
    name: 'New Year Offer',
    code: 'NEWYEAR2025',
    discount: 25,
    expirationDate: '2025-01-01',
  },
];

export default function CardPromos(): JSX.Element {
  const [promos] = useState(mockPromos);

  useEffect(() => {}, []);

  return (
    <div className="promo-cards-container">
      <h2>Available Promotions</h2>
      <div className="promo-cards-grid">
        {promos.map(promo => (
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
        ))}
      </div>
    </div>
  );
}
