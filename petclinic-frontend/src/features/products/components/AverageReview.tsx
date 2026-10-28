import { JSX, useId } from 'react';
import { useTranslation } from 'react-i18next';
import { RatingModel } from '../models/ProductModels/RatingModel';
import { averageRating, RatingCategory } from '../utils/averageRatings';
import StarRating from './StarRating';
import './AverageReview.css';

const categories: { field: RatingCategory; label: string }[] = [
  { field: 'qualityRating', label: 'qualityOfProduct' },
  { field: 'valueRating', label: 'valueOfProduct' },
  { field: 'petSatisfactionRating', label: 'petSatisfaction' },
  { field: 'ingredientsRating', label: 'ingredients' },
];

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
  const { average, count } = averageRating(ratings);
  return (
    <section
      className="average-review"
      aria-labelledby={headingId}
      aria-busy={status === 'loading'}
    >
      <div className="average-review-box">
        <h3 id={headingId}>{t('averageReview.title')}</h3>
        {status !== 'ready' ? (
          <p role="status">
            {t(status === 'loading' ? 'loading' : 'averageReview.error')}
          </p>
        ) : average === null ? (
          <p>{t('averageReview.empty')}</p>
        ) : (
          <>
            <div className="average-review-meter-row">
              <meter
                min={0}
                max={5}
                value={average}
                aria-labelledby={headingId}
              />
              <span>
                {t('averageReview.score', {
                  value: average.toLocaleString(
                    i18n.resolvedLanguage || i18n.language,
                    { minimumFractionDigits: 1, maximumFractionDigits: 1 }
                  ),
                })}
              </span>
            </div>
            <p className="average-review-count">
              {t('averageReview.count', { count })}
            </p>
          </>
        )}
        {status === 'ready' && (
          <div className="average-review-categories">
            {categories.map(({ field, label }) => {
              const category = averageRating(ratings, field);
              const labelId = `${headingId}-${field}`;
              return (
                <div className="average-review-category" key={field}>
                  <h4 id={labelId}>{t(label)}</h4>
                  {category.average === null ? (
                    <p className="average-review-count">
                      {t('averageReview.empty')}
                    </p>
                  ) : (
                    <>
                      <div className="average-review-meter-row">
                        <meter
                          min={0}
                          max={5}
                          value={category.average}
                          aria-labelledby={labelId}
                        />
                        <span>
                          {t('averageReview.score', {
                            value: category.average.toLocaleString(
                              i18n.resolvedLanguage || i18n.language,
                              {
                                minimumFractionDigits: 1,
                                maximumFractionDigits: 1,
                              }
                            ),
                          })}
                        </span>
                      </div>
                      <p className="average-review-count">
                        {t('averageReview.count', { count: category.count })}
                      </p>
                    </>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
      {status === 'ready' && average !== null && (
        <div className="average-review-stars" aria-hidden="true">
          <StarRating currentRating={average} viewOnly={true} />
        </div>
      )}
    </section>
  );
}
