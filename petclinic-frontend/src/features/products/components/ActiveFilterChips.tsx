import { useTranslation } from 'react-i18next';

export type ActiveFilterChip = {
  key: string;
  value?: string;
  label: string;
};

type ActiveFilterChipsProps = {
  chips: ActiveFilterChip[];
  onRemove: (key: string, value?: string) => void;
};

export default function ActiveFilterChips({
  chips,
  onRemove,
}: ActiveFilterChipsProps): JSX.Element | null {
  const { t } = useTranslation('products');

  if (chips.length === 0) return null;

  return (
    <div className="active-filter-bar" aria-label={t('activeFilters')}>
      <span className="active-filter-title">{t('activeFilters')}</span>
      {chips.map(chip => (
        <button
          key={`${chip.key}-${chip.value ?? chip.label}`}
          type="button"
          className="active-filter-chip"
          onClick={() => onRemove(chip.key, chip.value)}
          aria-label={t('removeFilter', { label: chip.label })}
        >
          {chip.label} <span aria-hidden="true">&times;</span>
        </button>
      ))}
      <button
        type="button"
        className="active-filter-clear"
        onClick={() => onRemove('all')}
      >
        {t('clearAll')}
      </button>
    </div>
  );
}
