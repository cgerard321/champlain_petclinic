/* eslint-disable @typescript-eslint/explicit-function-return-type */
import { useEffect, useState } from 'react';
import { Promo } from '@/features/promos/models/PromoModel.tsx';
import { useNavigate } from 'react-router-dom';
import './PromoTable.css';
import { PromoApi } from '../api/PromoApi';

export default function PromoListTable(): JSX.Element {
  const [promos, setPromos] = useState<Promo[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const navigate = useNavigate();

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

  const handleUpdate = (promoId: string) => {
    navigate(`/promos/${promoId}/edit`);
  };

  const handleDelete = async (promoCode: string) => {
    await PromoApi.deletePromo(promoCode);
  };

  if (loading) {
    return <p>Loading promos...</p>;
  }

  if (error) {
    return <p>{error}</p>;
  }

  return (
    <div className="container">
      <h2>Promo List</h2>
      <div className="actions">
        <button className="add-button" onClick={() => navigate('/promos/add')}>
          Add
        </button>
      </div>
      <table>
        <thead>
          <tr>
            <th>Name</th>
            <th>Code</th>
            <th>Discount (%)</th>
            <th>Expiration Date</th>
            <th>Active</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {promos.map(promo => (
            <tr key={promo.code}>
              <td>{promo.name}</td>
              <td>{promo.code}</td>
              <td>{promo.discount}</td>
              <td>{new Date(promo.expirationDate).toLocaleDateString()}</td>
              <td>{promo.active ? 'Yes' : 'No'}</td>
              <td>
                <button
                  onClick={() => handleUpdate(promo.id)}
                  className="update"
                  style={{ marginRight: '10px' }}
                >
                  Update
                </button>
                <button
                  onClick={() => handleDelete(promo.id)}
                  className="delete"
                >
                  Delete
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
