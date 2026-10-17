import { useTranslation } from 'react-i18next';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import ProductsList from '@/features/products/ProductsList.tsx';
import './Products.css';
import TrendingList from '@/features/products/TrendingList.tsx';
import { useState, useMemo, useEffect, useRef, useCallback } from 'react';
import ProductSearch from '@/features/products/components/ProductSearch';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import { ProductTypeModel } from '@/features/products/models/ProductModels/ProductTypeModel.ts';
import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import { getProductTypes } from '@/features/products/api/getProductTypes.ts';
import {
  localizedProduct,
  normalizeProductSearch,
} from '@/features/products/utils/localizedProduct';

type ProductFilters = {
  minPrice?: number;
  maxPrice?: number;
  ratingSort: string;
  minStars: number;
  maxStars: number;
  deliveryType: string;
  productType: string;
  availability: string;
};

const PRICE_PRESETS = [
  {
    key: 'under25',
    translationKey: 'under',
    minPrice: undefined,
    maxPrice: 25,
  },
  {
    key: '25to50',
    translationKey: 'range',
    minPrice: 25,
    maxPrice: 50,
  },
  {
    key: '50to100',
    translationKey: 'range',
    minPrice: 50,
    maxPrice: 100,
  },
  {
    key: '100plus',
    translationKey: 'over',
    minPrice: 100,
    maxPrice: undefined,
  },
] as const;

export default function Products(): JSX.Element {
  const { t, i18n } = useTranslation('products');
  const language = i18n.resolvedLanguage || i18n.language || 'en';
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [showSortOptions, setShowSortOptions] = useState(false);
  const [sortCriteria, setSortCriteria] = useState('default');
  const [productTypes, setProductTypes] = useState<ProductTypeModel[]>([]);
  const [allProducts, setAllProducts] = useState<ProductModel[]>([]);

  const defaultFilters = useMemo<ProductFilters>(
    () => ({
      minPrice: undefined,
      maxPrice: undefined,
      ratingSort: 'default',
      minStars: 0,
      maxStars: 5,
      deliveryType: '',
      productType: '',
      availability: '',
    }),
    []
  );

  const [tempFilters, setTempFilters] = useState(defaultFilters);

  const [appliedFilters, setAppliedFilters] = useState(defaultFilters);
  const [selectedPricePreset, setSelectedPricePreset] = useState<string | null>(
    null
  );

  const filterPanelRef = useRef<HTMLDivElement>(null);
  const closeButtonRef = useRef<HTMLButtonElement>(null);
  const filterButtonRef = useRef<HTMLButtonElement>(null);

  const closeSidebar = useCallback(
    (keepPendingFilters = false): void => {
      if (!keepPendingFilters) {
        setTempFilters(appliedFilters);
        setSelectedPricePreset(
          PRICE_PRESETS.find(
            preset =>
              preset.minPrice === appliedFilters.minPrice &&
              preset.maxPrice === appliedFilters.maxPrice
          )?.key ?? null
        );
      }
      setIsSidebarOpen(false);
      requestAnimationFrame(() => filterButtonRef.current?.focus());
    },
    [appliedFilters]
  );
  const toggleSidebar = (): void => {
    if (isSidebarOpen) closeSidebar();
    else setIsSidebarOpen(true);
  };
  const handleOverlayClick = (): void => closeSidebar();

  const activeFilterCount = useMemo(() => {
    return [
      appliedFilters.minPrice !== undefined ||
        appliedFilters.maxPrice !== undefined,
      appliedFilters.productType !== '',
      appliedFilters.deliveryType !== '',
      appliedFilters.minStars > 0 || appliedFilters.maxStars < 5,
      appliedFilters.availability !== '',
    ].filter(Boolean).length;
  }, [appliedFilters]);

  useEffect(() => {
    if (!isSidebarOpen) return;

    const previousPosition = document.body.style.position;
    const previousTop = document.body.style.top;
    const previousLeft = document.body.style.left;
    const previousRight = document.body.style.right;
    const previousWidth = document.body.style.width;
    const previousOverflowY = document.documentElement.style.overflowY;
    const scrollPosition = window.scrollY;

    document.body.style.position = 'fixed';
    document.body.style.top = `-${scrollPosition}px`;
    document.body.style.left = '0';
    document.body.style.right = '0';
    document.body.style.width = '100%';
    document.documentElement.style.overflowY = 'scroll';

    closeButtonRef.current?.focus();
    const handleEscape = (event: KeyboardEvent): void => {
      if (event.key === 'Escape') closeSidebar();
    };
    document.addEventListener('keydown', handleEscape);
    return () => {
      document.removeEventListener('keydown', handleEscape);
      document.body.style.position = previousPosition;
      document.body.style.top = previousTop;
      document.body.style.left = previousLeft;
      document.body.style.right = previousRight;
      document.body.style.width = previousWidth;
      document.documentElement.style.overflowY = previousOverflowY;
      window.scrollTo({
        top: scrollPosition,
        left: 0,
        behavior: 'instant',
      });
    };
  }, [closeSidebar, isSidebarOpen]);

  useEffect(() => {
    getAllProducts()
      .then(products => setAllProducts(products.filter(p => !p.isUnlisted)))
      .catch(() => setAllProducts([]));
  }, []);

  const handleSort = (criteria: string): void => {
    setSortCriteria(criteria);
    setShowSortOptions(false);
  };

  const updateTempFilter = (key: string, value: unknown): void => {
    setTempFilters(prev => ({ ...prev, [key]: value }));
  };

  const priceError =
    tempFilters.minPrice !== undefined &&
    tempFilters.maxPrice !== undefined &&
    tempFilters.minPrice > tempFilters.maxPrice;

  const applyFilters = (): void => {
    if (priceError) return;
    setAppliedFilters(tempFilters);
    closeSidebar(true);
  };

  const clearFilters = (): void => {
    setTempFilters(defaultFilters);
    setAppliedFilters(defaultFilters);
    setSelectedPricePreset(null);
  };

  const selectedTypes = tempFilters.productType
    ? tempFilters.productType.split(',').filter(Boolean)
    : [];
  const selectedAvailability = tempFilters.availability
    ? tempFilters.availability.split(',').filter(Boolean)
    : [];

  const selectPricePreset = (preset: (typeof PRICE_PRESETS)[number]): void => {
    setSelectedPricePreset(preset.key);
    setTempFilters(previous => ({
      ...previous,
      minPrice: preset.minPrice,
      maxPrice: preset.maxPrice,
    }));
  };

  const matchesFilterOption = (
    product: ProductModel,
    excludedFilter: 'productType' | 'deliveryType' | 'availability',
    option?: string
  ): boolean => {
    if (
      excludedFilter !== 'productType' &&
      tempFilters.productType &&
      !tempFilters.productType.split(',').includes(product.productType)
    ) {
      return false;
    }
    if (
      tempFilters.minPrice !== undefined &&
      product.productSalePrice < tempFilters.minPrice
    ) {
      return false;
    }
    if (
      tempFilters.maxPrice !== undefined &&
      product.productSalePrice > tempFilters.maxPrice
    ) {
      return false;
    }
    if (
      tempFilters.minStars > 0 &&
      product.averageRating < tempFilters.minStars
    ) {
      return false;
    }
    if (
      tempFilters.maxStars < 5 &&
      product.averageRating > tempFilters.maxStars
    ) {
      return false;
    }
    if (
      excludedFilter !== 'deliveryType' &&
      tempFilters.deliveryType &&
      String(product.deliveryType).toUpperCase() !==
        String(tempFilters.deliveryType).toUpperCase()
    ) {
      return false;
    }
    if (excludedFilter === 'productType' && option) {
      return product.productType === option;
    }
    if (excludedFilter === 'deliveryType' && option) {
      return (
        String(product.deliveryType).toUpperCase() ===
        String(option).toUpperCase()
      );
    }
    if (excludedFilter === 'availability' && option) {
      return option === 'in-stock'
        ? product.productQuantity > 0 &&
            product.productStatus !== 'OUT_OF_STOCK'
        : option === 'pre-order'
          ? product.productStatus === 'PRE_ORDER'
          : product.productQuantity <= 0 ||
            product.productStatus === 'OUT_OF_STOCK';
    }
    if (excludedFilter !== 'availability' && tempFilters.availability) {
      const selected = tempFilters.availability.split(',');
      const isInStock =
        product.productQuantity > 0 && product.productStatus !== 'OUT_OF_STOCK';
      const matchesAvailability = selected.some(value =>
        value === 'in-stock'
          ? isInStock
          : value === 'pre-order'
            ? product.productStatus === 'PRE_ORDER'
            : !isInStock
      );
      if (!matchesAvailability) return false;
    }
    return true;
  };

  const getOptionCount = (
    filter: 'productType' | 'deliveryType' | 'availability',
    option?: string
  ): number | undefined =>
    allProducts.length === 0
      ? undefined
      : allProducts.filter(product =>
          matchesFilterOption(product, filter, option)
        ).length;

  const matchingProductCount = allProducts.filter(product => {
    const selectedProductTypes = tempFilters.productType
      ? tempFilters.productType.split(',').filter(Boolean)
      : [];
    const matchesProductType =
      selectedProductTypes.length === 0 ||
      selectedProductTypes.includes(product.productType);
    const query = normalizeProductSearch(searchQuery);
    const matchesSearch =
      query === '' ||
      normalizeProductSearch(localizedProduct(product, language).name).includes(
        query
      ) ||
      normalizeProductSearch(
        t(`types.${(product.productType || '').toUpperCase()}`, {
          defaultValue: product.productType || '',
        })
      ).includes(query);

    return (
      matchesProductType &&
      matchesSearch &&
      matchesFilterOption(product, 'productType')
    );
  }).length;

  const updateMultiSelectFilter = (
    key: 'productType' | 'availability',
    value: string,
    selected: string[]
  ): void => {
    const next = selected.includes(value)
      ? selected.filter(item => item !== value)
      : [...selected, value];
    updateTempFilter(key, next.join(','));
  };

  const removeFilter = (key: string, value?: string): void => {
    const next = { ...appliedFilters };
    if (key === 'productType' || key === 'availability') {
      const selected = (next[key] || '').split(',').filter(Boolean);
      next[key] = selected.filter(item => item !== value).join(',');
    } else if (key === 'rating') {
      next.minStars = 0;
      next.maxStars = 5;
    } else if (key === 'price') {
      next.minPrice = undefined;
      next.maxPrice = undefined;
      setSelectedPricePreset(null);
    } else if (key === 'minPrice') {
      next.minPrice = undefined;
    } else if (key === 'maxPrice') {
      next.maxPrice = undefined;
    } else if (key === 'deliveryType') {
      next.deliveryType = '';
    }
    setAppliedFilters(next);
    setTempFilters(next);
  };

  const getTypeLabel = (type: string): string =>
    t(`types.${type.toUpperCase()}`, { defaultValue: type });
  const getAvailabilityLabel = (availability: string): string =>
    ({
      'in-stock': t('inStockFilter'),
      'pre-order': t('preOrderFilter'),
      'out-of-stock': t('outOfStockFilter'),
    })[availability] ?? availability;
  const getDeliveryLabel = (delivery: string): string =>
    ({
      DELIVERY: t('delivery'),
      PICKUP: t('pickup'),
      DELIVERY_AND_PICKUP: t('deliveryPickup'),
      NO_DELIVERY_OPTION: t('noDelivery'),
    })[delivery] ?? delivery;

  const activeFilterChips: { key: string; value?: string; label: string }[] =
    [];
  if (
    appliedFilters.minPrice !== undefined ||
    appliedFilters.maxPrice !== undefined
  ) {
    const min = appliedFilters.minPrice;
    const max = appliedFilters.maxPrice;
    activeFilterChips.push({
      key: 'price',
      label:
        min !== undefined && max !== undefined
          ? t('priceRange', { min, max })
          : min !== undefined
            ? t('priceFrom', { min })
            : t('priceUnder', { max }),
    });
  }
  appliedFilters.productType
    .split(',')
    .filter(Boolean)
    .forEach(type =>
      activeFilterChips.push({
        key: 'productType',
        value: type,
        label: getTypeLabel(type),
      })
    );
  if (appliedFilters.deliveryType) {
    activeFilterChips.push({
      key: 'deliveryType',
      label: getDeliveryLabel(appliedFilters.deliveryType),
    });
  }
  appliedFilters.availability
    .split(',')
    .filter(Boolean)
    .forEach(value =>
      activeFilterChips.push({
        key: 'availability',
        value,
        label: getAvailabilityLabel(value),
      })
    );
  if (appliedFilters.minStars > 0) {
    activeFilterChips.push({
      key: 'rating',
      label: t('ratingAtLeast', { count: appliedFilters.minStars }),
    });
  }

  useEffect(() => {
    getProductTypes()
      .then(setProductTypes)
      .catch(() => setProductTypes([]));
  }, []);

  const filters = appliedFilters;

  return (
    <div>
      <NavBar />
      <header className="header-container">
        <div className="overlay-text">
          <h1>{t('welcome')}</h1>
          <p>{t('intro')}</p>
        </div>
        <img
          src="https://cdn.pixabay.com/photo/2018/10/01/09/21/pets-3715733_1280.jpg"
          alt={t('pets')}
          className="full-width-image"
        />
      </header>

      <div className="search-and-filter">
        <button
          ref={filterButtonRef}
          className="toggle-sidebar-button"
          type="button"
          onClick={toggleSidebar}
          aria-expanded={isSidebarOpen}
          aria-controls="products-sidebar"
        >
          {t('filterButton')}
          {activeFilterCount > 0 ? ` (${activeFilterCount})` : ''}
        </button>

        <div className="search-wrapper">
          <ProductSearch
            searchQuery={searchQuery}
            setSearchQuery={setSearchQuery}
          />
        </div>

        <div className="sort-dropdown">
          <button
            className="sort-button"
            type="button"
            onClick={() => setShowSortOptions(prev => !prev)}
            aria-haspopup="menu"
            aria-controls="sort-menu"
            aria-expanded={showSortOptions}
          >
            {t('sort')}
          </button>
          {showSortOptions && (
            <div className="sort-options" role="menu" id="sort-menu">
              <button role="menuitem" onClick={() => handleSort('default')}>
                {t('sortDefault')}
              </button>
              <button role="menuitem" onClick={() => handleSort('rating-desc')}>
                {t('ratingDesc')}
              </button>
              <button role="menuitem" onClick={() => handleSort('rating-asc')}>
                {t('ratingAsc')}
              </button>
              <button role="menuitem" onClick={() => handleSort('price-desc')}>
                {t('priceDesc')}
              </button>
              <button role="menuitem" onClick={() => handleSort('price-asc')}>
                {t('priceAsc')}
              </button>
            </div>
          )}
        </div>
      </div>

      {isSidebarOpen && (
        <div
          className="overlay"
          onClick={handleOverlayClick}
          aria-hidden="true"
        />
      )}

      {isSidebarOpen && (
        <div
          ref={filterPanelRef}
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
              ref={closeButtonRef}
              className="close-button"
              type="button"
              onClick={() => closeSidebar()}
              aria-label={t('closeFilters')}
            >
              &times;
            </button>
          </div>
          <div className="filter-container">
            <section className="filter-section">
              <span className="filter-section-label">{t('filterPrice')}</span>
              <div className="price-presets" aria-label={t('pricePresets')}>
                {PRICE_PRESETS.map(preset => (
                  <button
                    key={preset.key}
                    type="button"
                    className={`filter-pill ${selectedPricePreset === preset.key ? 'selected' : ''}`}
                    aria-pressed={selectedPricePreset === preset.key}
                    onClick={() => selectPricePreset(preset)}
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
                      onChange={e => {
                        setSelectedPricePreset(null);
                        updateTempFilter(
                          'minPrice',
                          e.target.value ? +e.target.value : undefined
                        );
                      }}
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
                      onChange={e => {
                        setSelectedPricePreset(null);
                        updateTempFilter(
                          'maxPrice',
                          e.target.value ? +e.target.value : undefined
                        );
                      }}
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
              <span className="filter-section-label">
                {t('filterItemType')}
              </span>
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
                      updateMultiSelectFilter(
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
                {[
                  ['', t('any')],
                  ['DELIVERY', t('delivery')],
                  ['PICKUP', t('pickup')],
                  ['DELIVERY_AND_PICKUP', t('deliveryPickup')],
                  ['NO_DELIVERY_OPTION', t('noDelivery')],
                ].map(([value, label]) => (
                  <label key={value || 'any'} className="segment-option">
                    <input
                      type="radio"
                      name="delivery-type"
                      value={value}
                      checked={tempFilters.deliveryType === value}
                      disabled={
                        value !== '' &&
                        getOptionCount('deliveryType', value) === 0 &&
                        tempFilters.deliveryType !== value
                      }
                      onChange={e =>
                        updateTempFilter('deliveryType', e.target.value)
                      }
                    />
                    <span>
                      <span className="filter-pill-label">{label}</span>
                      <span className="filter-option-count">
                        ({getOptionCount('deliveryType', value) ?? '…'})
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
                {[
                  ['in-stock', t('inStockFilter')],
                  ['pre-order', t('preOrderFilter')],
                  ['out-of-stock', t('outOfStockFilter')],
                ].map(([value, label]) => (
                  <label
                    key={value}
                    className={`check-option ${
                      getOptionCount('availability', value) === 0 &&
                      !selectedAvailability.includes(value)
                        ? 'disabled'
                        : ''
                    }`}
                  >
                    <input
                      type="checkbox"
                      checked={selectedAvailability.includes(value)}
                      disabled={
                        getOptionCount('availability', value) === 0 &&
                        !selectedAvailability.includes(value)
                      }
                      onChange={() =>
                        updateMultiSelectFilter(
                          'availability',
                          value,
                          selectedAvailability
                        )
                      }
                    />
                    <span className="check-indicator" aria-hidden="true" />
                    <span>
                      <span className="filter-pill-label">{label}</span>
                      <span className="filter-option-count">
                        ({getOptionCount('availability', value) ?? '…'})
                      </span>
                    </span>
                  </label>
                ))}
              </div>
            </section>

            <section className="filter-section">
              <span className="filter-section-label">
                {t('filterRatingLabel')}
              </span>
              <div className="rating-options">
                {[
                  [4, t('ratingAtLeast', { count: 4 })],
                  [3, t('ratingAtLeast', { count: 3 })],
                  [2, t('ratingAtLeast', { count: 2 })],
                  [1, t('ratingAtLeast', { count: 1 })],
                  [0, t('any')],
                ].map(([value, label]) => (
                  <label key={value} className="rating-option">
                    <input
                      type="radio"
                      name="minimum-rating"
                      checked={tempFilters.minStars === value}
                      onChange={() => {
                        updateTempFilter('minStars', value);
                        updateTempFilter('maxStars', 5);
                      }}
                    />
                    <span className="rating-stars" aria-hidden="true">
                      {'\u2605'.repeat(Number(value))}
                      {'\u2606'.repeat(5 - Number(value))}
                    </span>
                    <span>{label}</span>
                  </label>
                ))}
              </div>
            </section>
          </div>
          <div className="filter-drawer-footer">
            <button
              type="button"
              className="clear-filter-button"
              onClick={clearFilters}
            >
              {t('clearAll')}
            </button>
            <button
              type="button"
              className="apply-filter-button"
              onClick={applyFilters}
              disabled={priceError}
            >
              {priceError
                ? t('fixPriceRange')
                : t('showResults', { count: matchingProductCount })}
            </button>
          </div>
        </div>
      )}

      {activeFilterChips.length > 0 && (
        <div className="active-filter-bar" aria-label={t('activeFilters')}>
          <span className="active-filter-title">{t('activeFilters')}</span>
          {activeFilterChips.map(chip => (
            <button
              key={`${chip.key}-${chip.value ?? chip.label}`}
              type="button"
              className="active-filter-chip"
              onClick={() => removeFilter(chip.key, chip.value)}
              aria-label={t('removeFilter', { label: chip.label })}
            >
              {chip.label} <span aria-hidden="true">&times;</span>
            </button>
          ))}
          <button
            type="button"
            className="active-filter-clear"
            onClick={clearFilters}
          >
            {t('clearAll')}
          </button>
        </div>
      )}

      <ProductsList
        view="catalog"
        searchQuery={searchQuery}
        filters={filters}
        sortCriteria={sortCriteria}
        onClearFilters={clearFilters}
      />

      <div className="block">
        <hr />
      </div>
      <TrendingList />
      <div className="block">
        <hr />
      </div>

      <ProductsList
        view="extras"
        searchQuery={searchQuery}
        filters={appliedFilters}
      />
    </div>
  );
}
