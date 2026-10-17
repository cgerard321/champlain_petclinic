import { useTranslation } from 'react-i18next';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import ProductsList from '@/features/products/ProductsList.tsx';
import TrendingList from '@/features/products/TrendingList.tsx';
import ProductSearch from '@/features/products/components/ProductSearch';
import { ProductTypeModel } from '@/features/products/models/ProductModels/ProductTypeModel.ts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import { getProductTypes } from '@/features/products/api/getProductTypes.ts';
import ProductFilterDrawer, {
  PricePreset,
} from '@/features/products/components/ProductFilterDrawer';
import ActiveFilterChips, {
  ActiveFilterChip,
} from '@/features/products/components/ActiveFilterChips';
import { useBodyScrollLock } from '@/features/products/hooks/useBodyScrollLock';
import {
  FilterKey,
  ProductFilters,
  matchesFilters,
} from '@/features/products/utils/productFilters';
import './Products.css';

const PRICE_PRESETS: readonly PricePreset[] = [
  { key: 'under25', translationKey: 'under', maxPrice: 25 },
  { key: '25to50', translationKey: 'range', minPrice: 25, maxPrice: 50 },
  { key: '50to100', translationKey: 'range', minPrice: 50, maxPrice: 100 },
  { key: '100plus', translationKey: 'over', minPrice: 100 },
];

export default function Products(): JSX.Element {
  const { t, i18n } = useTranslation('products');
  const language = i18n.resolvedLanguage || i18n.language || 'en';
  const [searchQuery, setSearchQuery] = useState('');
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

  useBodyScrollLock(isSidebarOpen, closeSidebar, closeButtonRef);

  useEffect(() => {
    getAllProducts()
      .then(products =>
        setAllProducts(products.filter(product => !product.isUnlisted))
      )
      .catch(() => setAllProducts([]));
    getProductTypes()
      .then(setProductTypes)
      .catch(() => setProductTypes([]));
  }, []);

  const activeFilterCount = useMemo(
    () =>
      [
        appliedFilters.minPrice !== undefined ||
          appliedFilters.maxPrice !== undefined,
        appliedFilters.productType !== '',
        appliedFilters.deliveryType !== '',
        (appliedFilters.minStars ?? 0) > 0 ||
          (appliedFilters.maxStars ?? 5) < 5,
        appliedFilters.availability !== '',
      ].filter(Boolean).length,
    [appliedFilters]
  );
  const selectedTypes =
    tempFilters.productType?.split(',').filter(Boolean) ?? [];
  const selectedAvailability =
    tempFilters.availability?.split(',').filter(Boolean) ?? [];
  const priceError =
    tempFilters.minPrice !== undefined &&
    tempFilters.maxPrice !== undefined &&
    tempFilters.minPrice > tempFilters.maxPrice;

  const updateTempFilter = (
    key: keyof ProductFilters,
    value: unknown
  ): void => {
    if (key === 'minPrice' || key === 'maxPrice') setSelectedPricePreset(null);
    setTempFilters(previous => ({ ...previous, [key]: value }));
  };
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
  const selectPricePreset = (preset: PricePreset): void => {
    setSelectedPricePreset(preset.key);
    setTempFilters(previous => ({
      ...previous,
      minPrice: preset.minPrice,
      maxPrice: preset.maxPrice,
    }));
  };
  const clearFilters = (): void => {
    setTempFilters(defaultFilters);
    setAppliedFilters(defaultFilters);
    setSelectedPricePreset(null);
  };
  const applyFilters = (): void => {
    if (!priceError) {
      setAppliedFilters(tempFilters);
      closeSidebar(true);
    }
  };
  const getOptionCount = (
    key: FilterKey,
    option?: string
  ): number | undefined =>
    allProducts.length === 0
      ? undefined
      : allProducts.filter(product =>
          matchesFilters(product, { ...tempFilters, [key]: option })
        ).length;
  const matchingProductCount = allProducts.filter(product =>
    matchesFilters(product, tempFilters, { query: searchQuery, language, t })
  ).length;

  const activeFilterChips = useMemo<ActiveFilterChip[]>(() => {
    const chips: ActiveFilterChip[] = [];
    const { minPrice, maxPrice } = appliedFilters;
    if (minPrice !== undefined || maxPrice !== undefined)
      chips.push({
        key: 'price',
        label:
          minPrice !== undefined && maxPrice !== undefined
            ? t('priceRange', { min: minPrice, max: maxPrice })
            : minPrice !== undefined
              ? t('priceFrom', { min: minPrice })
              : t('priceUnder', { max: maxPrice }),
      });
    appliedFilters.productType
      ?.split(',')
      .filter(Boolean)
      .forEach(value =>
        chips.push({
          key: 'productType',
          value,
          label: t(`types.${value.toUpperCase()}`, { defaultValue: value }),
        })
      );
    if (appliedFilters.deliveryType) {
      const labels: Record<string, string> = {
        DELIVERY: 'delivery',
        PICKUP: 'pickup',
        DELIVERY_AND_PICKUP: 'deliveryPickup',
        NO_DELIVERY_OPTION: 'noDelivery',
      };
      chips.push({
        key: 'deliveryType',
        label: t(labels[appliedFilters.deliveryType] ?? 'delivery'),
      });
    }
    appliedFilters.availability
      ?.split(',')
      .filter(Boolean)
      .forEach(value => {
        const labels: Record<string, string> = {
          'in-stock': 'inStockFilter',
          'out-of-stock': 'outOfStockFilter',
        };
        chips.push({
          key: 'availability',
          value,
          label: t(labels[value] ?? value),
        });
      });
    if ((appliedFilters.minStars ?? 0) > 0)
      chips.push({
        key: 'rating',
        label: t('ratingAtLeast', { count: appliedFilters.minStars }),
      });
    return chips;
  }, [appliedFilters, t]);

  const removeFilter = (key: string, value?: string): void => {
    if (key === 'all') {
      clearFilters();
      return;
    }
    const next = { ...appliedFilters };
    if (key === 'productType' || key === 'availability')
      next[key] = (next[key] ?? '')
        .split(',')
        .filter(item => item && item !== value)
        .join(',');
    else if (key === 'price') {
      next.minPrice = undefined;
      next.maxPrice = undefined;
    } else if (key === 'rating') {
      next.minStars = 0;
      next.maxStars = 5;
    } else if (key === 'deliveryType') next.deliveryType = '';
    setAppliedFilters(next);
    setTempFilters(next);
    setSelectedPricePreset(null);
  };
  const toggleSidebar = (): void =>
    isSidebarOpen ? closeSidebar() : setIsSidebarOpen(true);

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
            onClick={() => setShowSortOptions(previous => !previous)}
            aria-haspopup="menu"
            aria-controls="sort-menu"
            aria-expanded={showSortOptions}
          >
            {t('sort')}
          </button>
          {showSortOptions && (
            <div className="sort-options" role="menu" id="sort-menu">
              {[
                ['default', 'sortDefault'],
                ['rating-desc', 'ratingDesc'],
                ['rating-asc', 'ratingAsc'],
                ['price-desc', 'priceDesc'],
                ['price-asc', 'priceAsc'],
              ].map(([value, label]) => (
                <button
                  key={value}
                  role="menuitem"
                  onClick={() => {
                    setSortCriteria(value);
                    setShowSortOptions(false);
                  }}
                >
                  {t(label)}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
      {isSidebarOpen && (
        <div
          className="overlay"
          onClick={() => closeSidebar()}
          aria-hidden="true"
        />
      )}
      {isSidebarOpen && (
        <ProductFilterDrawer
          filterPanelRef={filterPanelRef}
          closeButtonRef={closeButtonRef}
          tempFilters={tempFilters}
          productTypes={productTypes}
          selectedPricePreset={selectedPricePreset}
          selectedTypes={selectedTypes}
          selectedAvailability={selectedAvailability}
          pricePresets={PRICE_PRESETS}
          priceError={priceError}
          matchingProductCount={matchingProductCount}
          activeFilterCount={activeFilterCount}
          getOptionCount={getOptionCount}
          onClose={() => closeSidebar()}
          onApply={applyFilters}
          onClear={clearFilters}
          onSelectPricePreset={selectPricePreset}
          onUpdateTempFilter={updateTempFilter}
          onUpdateMultiSelectFilter={updateMultiSelectFilter}
        />
      )}
      <ActiveFilterChips chips={activeFilterChips} onRemove={removeFilter} />
      <ProductsList
        view="catalog"
        searchQuery={searchQuery}
        filters={appliedFilters}
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
