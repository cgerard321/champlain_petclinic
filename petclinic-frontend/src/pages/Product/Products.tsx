import { NavBar } from '@/layouts/AppNavBar.tsx';
import ProductsList from '@/features/products/ProductsList.tsx';
import './Products.css';
import TrendingList from '@/features/products/TrendingList';
import { useState } from 'react';
import ProductSearch from '@/features/products/components/ProductSearch';
import StarRating from '@/features/products/components/StarRating';
import { ProductType } from '@/features/products/api/ProductTypeEnum';

export default function Products(): JSX.Element {
  const [searchQuery, setSearchQuery] = useState<string>('');

  // filters
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [minPrice, setMinPrice] = useState<number | undefined>();
  const [maxPrice, setMaxPrice] = useState<number | undefined>();
  const [ratingSort, setRatingSort] = useState<string>('default');
  const [minStars, setMinStars] = useState<number>(0);
  const [maxStars, setMaxStars] = useState<number>(5);
  const [deliveryType, setDeliveryType] = useState<string>('');
  const [productType, setProductType] = useState<string>('');
  const [validationMessage, setValidationMessage] = useState<string>('');

  const toggleSidebar = (): void => setIsSidebarOpen(!isSidebarOpen);
  const handleOverlayClick = (): void => setIsSidebarOpen(false);
  const [showSortOptions, setShowSortOptions] = useState(false);
  const [sortCriteria, setSortCriteria] = useState('default');
  const handleSort = (criteria: string): void => {
    setSortCriteria(criteria);
    setShowSortOptions(false);
  };

  const clearFilters = (): void => {
    setMinPrice(undefined);
    setMaxPrice(undefined);
    setRatingSort('default');
    setMinStars(0);
    setMaxStars(5);
    setDeliveryType('');
    setProductType('');
    setValidationMessage('');
  };

  const filters = {
    minPrice,
    maxPrice,
    ratingSort,
    minStars,
    maxStars,
    deliveryType,
    productType,
  };

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
        {!isSidebarOpen && (
          <button
            className="toggle-sidebar-button"
            onClick={toggleSidebar}
            aria-expanded={isSidebarOpen}
            aria-controls="sidebar"
          >
            &#9776; Filters
          </button>
        )}

        <div className="search-wrapper">
          <ProductSearch
            searchQuery={searchQuery}
            setSearchQuery={setSearchQuery}
          />
        </div>

        {/*<button
          className="toggle-sidebar-button"
          type="button"
          aria-label="Sort By"
        >
          Sort By
        </button>*/}
        <div className="sort-dropdown">
          <button
            className="sort-button"
            type="button"
            onClick={() => setShowSortOptions(prev => !prev)}
          >
            Sort By
          </button>
          {showSortOptions && (
            <div className="sort-options">
              <button onClick={() => handleSort('default')}>Sort by</button>
              <button onClick={() => handleSort('rating-desc')}>
                Rating: High → Low
              </button>
              <button onClick={() => handleSort('rating-asc')}>
                Rating: Low → High
              </button>
              <button onClick={() => handleSort('price-desc')}>
                Price: High → Low
              </button>
              <button onClick={() => handleSort('price-asc')}>
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
        <div className={`sidebar ${isSidebarOpen ? 'open' : ''}`} id="sidebar">
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
                value={minPrice ?? ''}
                onChange={e =>
                  setMinPrice(e.target.value ? +e.target.value : undefined)
                }
              />
            </label>

            <label>
              Max Price:
              <input
                type="number"
                value={maxPrice ?? ''}
                onChange={e =>
                  setMaxPrice(e.target.value ? +e.target.value : undefined)
                }
              />
            </label>

            <label>
              Item Type:
              <select
                value={productType}
                onChange={e => setProductType(e.target.value)}
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
                value={deliveryType}
                onChange={e => setDeliveryType(e.target.value)}
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
                currentRating={minStars}
                viewOnly={false}
                updateRating={setMinStars}
              />
              <StarRating
                currentRating={maxStars}
                viewOnly={false}
                updateRating={setMaxStars}
              />
            </div>

            {/*<select
              value={ratingSort}
              onChange={e => setRatingSort(e.target.value)}
            >
              <option value="default">Sort by Rating</option>
              <option value="asc">Low to High</option>
              <option value="desc">High to Low</option>
            </select>*/}

            <button onClick={toggleSidebar}>Apply</button>
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

      {!searchQuery && (
        <div className="trending-list-container-gold">
          <h2 className="section-header">Trending</h2>
          <TrendingList />
        </div>
      )}

      <div className="block">
        <hr />
      </div>

      <ProductsList view="extras" searchQuery={searchQuery} filters={filters} />
    </div>
  );
}
