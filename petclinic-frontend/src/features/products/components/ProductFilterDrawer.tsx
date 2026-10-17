import { RefObject } from 'react';
import { useTranslation } from 'react-i18next';
import { ProductTypeModel } from '../models/ProductModels/ProductTypeModel';
import { ProductFilters, FilterKey } from '../utils/productFilters';

export type PricePreset = {
  key: string;
  translationKey: string;
  minPrice?: number;
  maxPrice?: number;
};

export const DELIVERY_OPTIONS = [
  { value: '', labelKey: 'any' },
  { value: 'DELIVERY', labelKey: 'delivery' },
  { value: 'PICKUP', labelKey: 'pickup' },
  { value: 'DELIVERY_AND_PICKUP', labelKey: 'deliveryPickup' },
  { value: 'NO_DELIVERY_OPTION', labelKey: 'noDelivery' },
] as const;

export const AVAILABILITY_OPTIONS = [
  { value: 'in-stock', labelKey: 'inStockFilter' },
  { value: 'pre-order', labelKey: 'preOrderFilter' },
  { value: 'out-of-stock', labelKey: 'outOfStockFilter' },
] as const;

export const RATING_OPTIONS = [4, 3, 2, 1, 0] as const;

type ProductFilterDrawerProps = {
  filterPanelRef: RefObject<HTMLDivElement | null>;
  closeButtonRef: RefObject<HTMLButtonElement | null>;
  tempFilters: ProductFilters;
  productTypes: ProductTypeModel[];
  selectedPricePreset: string | null;
  selectedTypes: string[];
  selectedAvailability: string[];
  pricePresets: readonly PricePreset[];
  priceError: boolean;
  matchingProductCount: number;
  activeFilterCount: number;
  getOptionCount: (key: FilterKey, option?: string) => number | undefined;
  onClose: () => void;
  onApply: () => void;
  onClear: () => void;
  onSelectPricePreset: (preset: PricePreset) => void;
  onUpdateTempFilter: (key: keyof ProductFilters, value: unknown) => void;
  onUpdateMultiSelectFilter: (
    key: 'productType' | 'availability',
    value: string,
    selected: string[]
  ) => void;
};

export default function ProductFilterDrawer({
  filterPanelRef,
  closeButtonRef,
  tempFilters,
  productTypes,
  selectedPricePreset,
  selectedTypes,
  selectedAvailability,
  pricePresets,
  priceError,
  matchingProductCount,
  activeFilterCount,
  getOptionCount,
  onClose,
  onApply,
  onClear,
  onSelectPricePreset,
  onUpdateTempFilter,
  onUpdateMultiSelectFilter,
}: ProductFilterDrawerProps): JSX.Element {
  const { t } = useTranslation('products');

  const getTypeLabel = (type: string): string =>
    t(`types.${type.toUpperCase()}`, { defaultValue: type });

  return (
    <div
      ref={filterPanelRef as RefObject<HTMLDivElement>}
      className="products-sidebar open"
      id="products-sidebar"
      role="dialog"
      aria-modal="true"
      aria-labelledby="products-filter-heading"
    >
      <div className="filter-drawer-header">
        <div>
          <h2 id="products-filter-heading">
            {t('filters')}
            {activeFilterCount > 0 ? ` · ${activeFilterCount}` : ''}
          </h2>
          <span className="filter-count-label">
            {activeFilterCount > 0
              ? t('activeFiltersCount', { count: activeFilterCount })
              : t('refineResults')}
          </span>
        </div>
        <button
          ref={closeButtonRef as RefObject<HTMLButtonElement>}
          className="close-button"
          type="button"
          onClick={onClose}
          aria-label={t('closeFilters')}
        >
          &times;
        </button>
      </div>
      <div className="filter-container">
        <section className="filter-section">
          <span className="filter-section-label">{t('filterPrice')}</span>
          <div className="price-presets" aria-label={t('pricePresets')}>
            {pricePresets.map(preset => (
              <button
                key={preset.key}
                type="button"
                className={`filter-pill ${selectedPricePreset === preset.key ? 'selected' : ''}`}
                aria-pressed={selectedPricePreset === preset.key}
                onClick={() => onSelectPricePreset(preset)}
              >
                {t(`pricePreset.${preset.translationKey}`, {
                  min: preset.minPrice,
                  max: preset.maxPrice,
                })}
              </button>
            ))}
          </div>
          <div className="price-inputs">
            <label htmlFor="filter-min-price">
              <span>{t('min')}</span>
              <span className="currency-input price-field">
                <span aria-hidden="true">$</span>
                <input
                  id="filter-min-price"
                  type="number"
                  min="0"
                  step="0.01"
                  inputMode="decimal"
                  value={tempFilters.minPrice ?? ''}
                  onChange={event =>
                    onUpdateTempFilter(
                      'minPrice',
                      event.target.value ? +event.target.value : undefined
                    )
                  }
                />
              </span>
            </label>
            <label htmlFor="filter-max-price">
              <span>{t('max')}</span>
              <span className="currency-input price-field">
                <span aria-hidden="true">$</span>
                <input
                  id="filter-max-price"
                  type="number"
                  min="0"
                  step="0.01"
                  inputMode="decimal"
                  value={tempFilters.maxPrice ?? ''}
                  onChange={event =>
                    onUpdateTempFilter(
                      'maxPrice',
                      event.target.value ? +event.target.value : undefined
                    )
                  }
                />
              </span>
            </label>
          </div>
          {priceError && (
            <p className="filter-error" role="alert">
              {t('priceError')}
            </p>
          )}
        </section>

        <section className="filter-section">
          <span className="filter-section-label">{t('filterItemType')}</span>
          <div
            className="filter-pills"
            role="group"
            aria-label={t('filterItemType')}
          >
            {productTypes.map(type => (
              <button
                key={type.typeName}
                type="button"
                className={`filter-pill ${selectedTypes.includes(type.typeName) ? 'selected' : ''}`}
                aria-pressed={selectedTypes.includes(type.typeName)}
                disabled={
                  getOptionCount('productType', type.typeName) === 0 &&
                  !selectedTypes.includes(type.typeName)
                }
                onClick={() =>
                  onUpdateMultiSelectFilter(
                    'productType',
                    type.typeName,
                    selectedTypes
                  )
                }
              >
                <span className="filter-pill-label">
                  {getTypeLabel(type.typeName)}
                </span>
                <span className="filter-option-count">
                  ({getOptionCount('productType', type.typeName) ?? '…'})
                </span>
              </button>
            ))}
          </div>
        </section>

        <section className="filter-section">
          <span className="filter-section-label">
            {t('filterDeliveryType')}
          </span>
          <div
            className="segmented-control"
            role="radiogroup"
            aria-label={t('filterDeliveryType')}
          >
            {DELIVERY_OPTIONS.map(option => (
              <label key={option.value || 'any'} className="segment-option">
                <input
                  type="radio"
                  name="delivery-type"
                  value={option.value}
                  checked={tempFilters.deliveryType === option.value}
                  disabled={
                    option.value !== '' &&
                    getOptionCount('deliveryType', option.value) === 0 &&
                    tempFilters.deliveryType !== option.value
                  }
                  onChange={event =>
                    onUpdateTempFilter('deliveryType', event.target.value)
                  }
                />
                <span>
                  <span className="filter-pill-label">
                    {t(option.labelKey)}
                  </span>
                  <span className="filter-option-count">
                    ({getOptionCount('deliveryType', option.value) ?? '…'})
                  </span>
                </span>
              </label>
            ))}
          </div>
        </section>

        <section className="filter-section">
          <span className="filter-section-label">
            {t('filterAvailability')}
          </span>
          <div className="availability-options">
            {AVAILABILITY_OPTIONS.map(option => (
              <label
                key={option.value}
                className={`check-option ${
                  getOptionCount('availability', option.value) === 0 &&
                  !selectedAvailability.includes(option.value)
                    ? 'disabled'
                    : ''
                }`}
              >
                <input
                  type="checkbox"
                  checked={selectedAvailability.includes(option.value)}
                  disabled={
                    getOptionCount('availability', option.value) === 0 &&
                    !selectedAvailability.includes(option.value)
                  }
                  onChange={() =>
                    onUpdateMultiSelectFilter(
                      'availability',
                      option.value,
                      selectedAvailability
                    )
                  }
                />
                <span className="check-indicator" aria-hidden="true" />
                <span>
                  <span className="filter-pill-label">
                    {t(option.labelKey)}
                  </span>
                  <span className="filter-option-count">
                    ({getOptionCount('availability', option.value) ?? '…'})
                  </span>
                </span>
              </label>
            ))}
          </div>
        </section>

        <section className="filter-section">
          <span className="filter-section-label">{t('filterRatingLabel')}</span>
          <div className="rating-options">
            {RATING_OPTIONS.map(value => (
              <label key={value} className="rating-option">
                <input
                  type="radio"
                  name="minimum-rating"
                  checked={tempFilters.minStars === value}
                  onChange={() => {
                    onUpdateTempFilter('minStars', value);
                    onUpdateTempFilter('maxStars', 5);
                  }}
                />
                <span className="rating-stars" aria-hidden="true">
                  {'\u2605'.repeat(value)}
                  {'\u2606'.repeat(5 - value)}
                </span>
                <span>
                  {value === 0
                    ? t('any')
                    : t('ratingAtLeast', { count: value })}
                </span>
              </label>
            ))}
          </div>
        </section>
      </div>
      <div className="filter-drawer-footer">
        <button type="button" className="clear-filter-button" onClick={onClear}>
          {t('clearAll')}
        </button>
        <button
          type="button"
          className="apply-filter-button"
          onClick={onApply}
          disabled={priceError}
        >
          {priceError
            ? t('fixPriceRange')
            : t('showResults', { count: matchingProductCount })}
        </button>
      </div>
    </div>
  );
}
