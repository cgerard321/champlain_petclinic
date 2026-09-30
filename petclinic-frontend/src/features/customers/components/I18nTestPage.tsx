import { useTranslation } from 'react-i18next';
import { I18nTestNavBar } from '@/layouts/I18nTestNavBar.tsx';

export function I18nTestPage(): JSX.Element {
  const { t } = useTranslation('customers');

  return (
    <div>
      <I18nTestNavBar />

      <div className="container mt-4">
        <div className="card shadow-sm p-4">
          <h2 className="card-title">{t('title')}</h2>
          <p className="card-text text-muted">{t('subtitle')}</p>

          <div className="d-flex gap-2 mt-3">
            <button type="button" className="btn btn-primary">
              {t('buttons.save')}
            </button>
            <button type="button" className="btn btn-outline-secondary">
              {t('buttons.cancel')}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
