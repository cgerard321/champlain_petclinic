import { JSX, useId } from 'react';
import { useTranslation } from 'react-i18next';
import type { RatingModel } from '../models/ProductModels/RatingModel';
import { averageRating } from '../utils/averageRatings';
import './AverageReview.css';

interface AverageReviewProps {
  ratings: RatingModel[];
  status: 'loading' | 'ready' | 'error';
}

export function AverageReview({
  ratings,
  status,
}: AverageReviewProps): JSX.Element {
  const { t, i18n } = useTranslation('products');
  const headingId = useId();
  const rows = [{ key: 'overall', ...averageRating(ratings) }];

  return (
    <section
      className="average-review"
      aria-labelledby={headingId}
      aria-busy={status === 'loading'}
    >
      <h3 id={headingId}>{t('averageReview.title')}</h3>
      {status !== 'ready' ? (
        <p role="status">
          {t(status === 'loading' ? 'loading' : 'averageReview.error')}
        </p>
      ) : (
        <div className="average-review-rows">
          {rows.map(({ key, average, count }) => (
            <div className="average-review-row" key={key}>
              <span className="average-review-label">
                {t(`averageReview.${key}`)}
              </span>
              {average === null ? (
                <span className="average-review-empty">
                  {t('averageReview.empty')}
                </span>
              ) : (
                <>
                  <meter
                    min={1}
                    max={5}
                    value={average}
                    aria-label={t(`averageReview.${key}`)}
                  />
                  <span className="average-review-score">
                    {t('averageReview.score', {
                      value: average.toLocaleString(
                        i18n.resolvedLanguage || i18n.language,
                        { minimumFractionDigits: 1, maximumFractionDigits: 1 }
                      ),
                    })}
                  </span>
                  <span className="average-review-count">
                    {t('averageReview.count', { count })}
                  </span>
                </>
              )}
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
