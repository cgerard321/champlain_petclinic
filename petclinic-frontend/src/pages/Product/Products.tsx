import { NavBar } from '@/layouts/AppNavBar.tsx';
import ProductsList from '@/features/products/ProductsList.tsx';
import './Products.css';
import TrendingList from '@/features/products/TrendingList.tsx';
import { useState, useMemo } from 'react';
import ProductSearch from '@/features/products/components/ProductSearch';
import StarRating from '@/features/products/components/StarRating';
import { ProductType } from '@/features/products/api/ProductTypeEnum';

export default function Products(): JSX.Element {
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [validationMessage, setValidationMessage] = useState<string>('');
  const [showSortOptions, setShowSortOptions] = useState(false);
  const [sortCriteria, setSortCriteria] = useState('default');

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

  const filters = useMemo(() => appliedFilters, [appliedFilters]);

  return (
    <div>
      <NavBar />
      <header className="header-container">
        <div className="overlay-text">
          <h1>Welcome to PetClinic Shop Page!</h1>
          <p>
            Discover a range of quality items to keep your pets healthy and
            happy.
          </p>
        </div>
        <img
          src="https://cdn.pixabay.com/photo/2018/10/01/09/21/pets-3715733_1280.jpg"
          alt="Pets"
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
          {'☰ Filters'}
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
            Sort By
          </button>
          {showSortOptions && (
            <div className="sort-options" role="menu" id="sort-menu">
              <button role="menuitem" onClick={() => handleSort('default')}>
                Sort by Default
              </button>
              <button role="menuitem" onClick={() => handleSort('rating-desc')}>
                Rating: High → Low
              </button>
              <button role="menuitem" onClick={() => handleSort('rating-asc')}>
                Rating: Low → High
              </button>
              <button role="menuitem" onClick={() => handleSort('price-desc')}>
                Price: High → Low
              </button>
              <button role="menuitem" onClick={() => handleSort('price-asc')}>
                Price: Low → High
              </button>
            </div>
          )}
        </div>
      </div>

      {isSidebarOpen && (
        <div className="overlay" onClick={handleOverlayClick}></div>
      )}

      {isSidebarOpen && (
        <div className={`products-sidebar${isSidebarOpen ? ' open' : ''}`}>
          <button
            className="close-button"
            onClick={toggleSidebar}
            aria-label="Close Filters"
          >
            &times;
          </button>
          <div className="filter-container">
            <h2>Filters</h2>

            <label>
              Min Price:
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
              Max Price:
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
              Item Type:
              <select
                value={tempFilters.productType}
                onChange={e => updateTempFilter('productType', e.target.value)}
              >
                <option value="">All Item Types</option>
                {Object.values(ProductType).map(type => (
                  <option key={type} value={type}>
                    {type.charAt(0).toUpperCase() + type.slice(1).toLowerCase()}
                  </option>
                ))}
              </select>
            </label>

            <label>
              Delivery Type:
              <select
                value={tempFilters.deliveryType}
                onChange={e => updateTempFilter('deliveryType', e.target.value)}
              >
                <option value="">All Delivery Types</option>
                <option value="DELIVERY">Delivery</option>
                <option value="PICKUP">Pickup</option>
                <option value="DELIVERY_AND_PICKUP">Delivery & Pickup</option>
                <option value="NO_DELIVERY_OPTION">No Delivery Option</option>
              </select>
            </label>

            <div className="star-rating-container">
              <h2>Filter by Star Rating</h2>
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

            <button onClick={applyFilters}>Apply</button>
            <button onClick={clearFilters}>Clear</button>
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
