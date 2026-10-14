import { useTranslation } from 'react-i18next';
import { NavBar } from '@/layouts/AppNavBar.tsx';
import ProductsList from '@/features/products/ProductsList.tsx';
import './Products.css';
import TrendingList from '@/features/products/TrendingList.tsx';
import { useState, useMemo, useEffect } from 'react';
import ProductSearch from '@/features/products/components/ProductSearch';
import StarRating from '@/features/products/components/StarRating';
import { ProductTypeModel } from '@/features/products/models/ProductModels/ProductTypeModel.ts';
import { getProductTypes } from '@/features/products/api/getProductTypes.ts';

export default function Products(): JSX.Element {
  const { t } = useTranslation('products');
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [validationMessage, setValidationMessage] = useState<string>('');
  const [showSortOptions, setShowSortOptions] = useState(false);
  const [sortCriteria, setSortCriteria] = useState('default');
  const [productTypes, setProductTypes] = useState<ProductTypeModel[]>([]);

  const defaultFilters = useMemo(
    () => ({
      minPrice: undefined,
      maxPrice: undefined,
      ratingSort: 'default',
      minStars: 0,
      maxStars: 5,
      deliveryType: '',
      productType: '',
    }),
    []
  );

  const [tempFilters, setTempFilters] = useState(defaultFilters);

  const [appliedFilters, setAppliedFilters] = useState(defaultFilters);

  const toggleSidebar = (): void => setIsSidebarOpen(!isSidebarOpen);
  const handleOverlayClick = (): void => setIsSidebarOpen(false);

  const handleSort = (criteria: string): void => {
    setSortCriteria(criteria);
    setShowSortOptions(false);
  };

  const updateTempFilter = (key: string, value: unknown): void => {
    setTempFilters(prev => ({ ...prev, [key]: value }));
  };

  const applyFilters = (): void => {
    setAppliedFilters(tempFilters);
    setIsSidebarOpen(false);
  };

  const clearFilters = (): void => {
    setTempFilters(defaultFilters);
    setAppliedFilters(defaultFilters);
    setValidationMessage('');
  };

  useEffect(() => {
    getProductTypes()
      .then(setProductTypes)
      .catch(() => setProductTypes([]));
  }, []);

  const filters = useMemo(() => appliedFilters, [appliedFilters]);

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
          className="toggle-sidebar-button"
          onClick={toggleSidebar}
          aria-expanded={isSidebarOpen}
          aria-controls="products-sidebar"
        >
          {t('filterButton')}
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
        <div className="overlay" onClick={handleOverlayClick}></div>
      )}

      {isSidebarOpen && (
        <div className={`products-sidebar ${isSidebarOpen ? 'open' : ''}`}>
          <button
            className="close-button"
            onClick={toggleSidebar}
            aria-label={t('closeFilters')}
          >
            &times;
          </button>
          <div className="filter-container">
            <h2>{t('filters')}</h2>

            <label>
              {t('minPrice')}
              <input
                type="number"
                value={tempFilters.minPrice ?? ''}
                onChange={e =>
                  updateTempFilter(
                    'minPrice',
                    e.target.value ? +e.target.value : undefined
                  )
                }
              />
            </label>

            <label>
              {t('maxPrice')}
              <input
                type="number"
                value={tempFilters.maxPrice ?? ''}
                onChange={e =>
                  updateTempFilter(
                    'maxPrice',
                    e.target.value ? +e.target.value : undefined
                  )
                }
              />
            </label>

            <label>
              {t('itemType')}
              <select
                value={tempFilters.productType}
                onChange={e => updateTempFilter('productType', e.target.value)}
              >
                <option value="">{t('allTypes')}</option>
                {productTypes.map(type => (
                  <option key={type.typeName} value={type.typeName}>
                    {t(`types.${type.typeName.toUpperCase()}`, {
                      defaultValue: type.typeName,
                    })}
                  </option>
                ))}
              </select>
            </label>

            <label>
              {t('deliveryType')}
              <select
                value={tempFilters.deliveryType}
                onChange={e => updateTempFilter('deliveryType', e.target.value)}
              >
                <option value="">{t('allDelivery')}</option>
                <option value="DELIVERY">{t('delivery')}</option>
                <option value="PICKUP">{t('pickup')}</option>
                <option value="DELIVERY_AND_PICKUP">
                  {t('deliveryPickup')}
                </option>
                <option value="NO_DELIVERY_OPTION">{t('noDelivery')}</option>
              </select>
            </label>

            <div className="star-rating-container">
              <h2>{t('filterRating')}</h2>
              <StarRating
                currentRating={tempFilters.minStars}
                viewOnly={false}
                updateRating={value => updateTempFilter('minStars', value)}
              />
              <StarRating
                currentRating={tempFilters.maxStars}
                viewOnly={false}
                updateRating={value => updateTempFilter('maxStars', value)}
              />
            </div>

            <button onClick={applyFilters}>{t('apply')}</button>
            <button onClick={clearFilters}>{t('clear')}</button>
            {validationMessage && (
              <span style={{ color: 'red' }}>{validationMessage}</span>
            )}
          </div>
        </div>
      )}

      <ProductsList
        view="catalog"
        searchQuery={searchQuery}
        filters={filters}
        sortCriteria={sortCriteria}
      />

      <div className="block">
        <hr />
      </div>
      <TrendingList />
      <div className="block">
        <hr />
      </div>

      <ProductsList view="extras" searchQuery={searchQuery} filters={filters} />
    </div>
  );
}
