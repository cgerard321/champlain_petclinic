import { useState, useEffect } from 'react';
import './CardPromos.css';
import ImageContainer from '@/features/products/components/ImageContainer';
import { Promo } from '@/features/promos/models/PromoModel.tsx';
import { PromoApi } from '@/features/promos/api/PromoApi.tsx';
import { getProduct } from '@/features/products/api/getProduct';

function CopyPromoCode({ code }: { code: string }): JSX.Element {
  const [feedback, setFeedback] = useState('');
  const [copying, setCopying] = useState(false);

  const copy = async (): Promise<void> => {
    setCopying(true);
    setFeedback('');
    try {
      await navigator.clipboard.writeText(code);
      setFeedback('Promo code copied!');
    } catch {
      setFeedback(
        'Could not copy the code. Please select and copy it manually.'
      );
    } finally {
      setCopying(false);
    }
  };

  return (
    <div className="promo-code-section">
      <div className="promo-code-row">
        <code>{code}</code>
        <button
          type="button"
          disabled={copying}
          onClick={copy}
          aria-label={`Copy promo code ${code}`}
        >
          {copying ? 'Copying...' : 'Copy code'}
        </button>
      </div>
      <p className="promo-copy-feedback" role="status" aria-live="polite">
        {feedback}
      </p>
    </div>
  );
}

export default function CardPromos(): JSX.Element {
  const [promos, setPromos] = useState<Promo[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [productNames, setProductNames] = useState<Record<string, string>>({});

  const eligibility = (promo: Promo): string => {
    if (promo.scope === 'PRODUCT') {
      return (
        promo.productIds?.map(id => productNames[id] || id).join(', ') ||
        'No eligible products specified'
      );
    }
    if (promo.scope === 'PRODUCT_TYPE') {
      return promo.productTypes?.length
        ? `Product types: ${promo.productTypes.join(', ')}`
        : 'No eligible product types specified';
    }
    return 'All items in your cart';
  };

  const fetchPromos = async (): Promise<void> => {
    try {
      const data = await PromoApi.fetchActivePromos();
      setPromos(data);
      const productIds = [
        ...new Set(
          data.flatMap(promo =>
            promo.scope === 'PRODUCT' ? promo.productIds ?? [] : []
          )
        ),
      ];
      const products = await Promise.allSettled(
        productIds.map(id => getProduct(id, true))
      );
      setProductNames(
        Object.fromEntries(
          products.flatMap(result =>
            result.status === 'fulfilled' && result.value
              ? [[result.value.productId, result.value.productName]]
              : []
          )
        )
      );
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
          promos.map(promo => (
            <div className="promo-card" key={promo.code}>
              <div className="promo-card-image">
                <ImageContainer image={promo.image} alt={promo.name} />
              </div>
              <h3>{promo.name}</h3>
              <CopyPromoCode code={promo.code} />
              <p>
                <strong>Discount:</strong> {promo.discount}%
              </p>
              <p>
                <strong>Applies to:</strong> {eligibility(promo)}
              </p>
              <p>
                <strong>Expires on:</strong>{' '}
                {new Date(promo.expirationDate).toLocaleDateString()}
              </p>
            </div>
          ))
        ) : (
          <div className="empty-promo-card">
            <p style={{ textAlign: 'center', fontStyle: 'italic' }}>
              Currently, there are no available promotions. Please check back
              later for exciting offers.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
