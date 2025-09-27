import { NavBar } from '@/layouts/AppNavBar.tsx';
import ProductsList from '@/features/products/ProductsList.tsx';
import './Products.css';
import TrendingList from '@/features/products/TrendingList';
import ProductSearch from '@/features/products/components/ProductSearch';
import StarRating from '@/features/products/components/StarRating';
import { ProductType } from '@/features/products/api/ProductTypeEnum';
import { useState } from 'react';

export default function Products(): JSX.Element {
  const [isSidebarOpen, setIsSidebarOpen] = useState<boolean>(false);
  const [minPrice, setMinPrice] = useState<number | undefined>(undefined);
  const [maxPrice, setMaxPrice] = useState<number | undefined>(undefined);
  const [ratingSort, setRatingSort] = useState<string>('default');
  const [minStars, setMinStars] = useState<number>(0);
  const [maxStars, setMaxStars] = useState<number>(5);
  const [validationMessage, setValidationMessage] = useState<string>('');
  const [deliveryType, setDeliveryType] = useState<string>('');
  const [productType, setProductType] = useState<string>('');

  const toggleSidebar = (): void => setIsSidebarOpen(!isSidebarOpen);
  const handleOverlayClick = (): void => setIsSidebarOpen(false);

  const validationStars = async (
    minStars: number,
    maxStars: number
  ): Promise<void> => {
    if (minStars >= maxStars) {
      setValidationMessage(
        'Minimum stars cannot be greater than or equal to maximum stars.'
      );
    } else {
      setValidationMessage('');
    }
  };

  const clearFilters = (): void => {
    setMinPrice(undefined);
    setMaxPrice(undefined);
    setRatingSort('');
    setMaxStars(5);
    setMinStars(0);
    setValidationMessage('');
    setDeliveryType('');
    setProductType('');
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

      {isSidebarOpen && (
        <div className="overlay" onClick={handleOverlayClick}></div>
      )}

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
          <ProductSearch />
        </div>
      </div>

      {isSidebarOpen && (
        <div
          className={`sidebar ${isSidebarOpen ? 'open' : ''}`}
          id="sidebar"
          aria-hidden={!isSidebarOpen}
        >
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
                value={minPrice ?? typeof 'number'}
                onChange={e =>
                  setMinPrice(
                    e.target.value ? parseFloat(e.target.value) : undefined
                  )
                }
                min="0"
                placeholder="e.g., 10"
              />
            </label>
            <label>
              Max Price:
              <input
                type="number"
                value={maxPrice ?? typeof 'number'}
                onChange={e =>
                  setMaxPrice(
                    e.target.value ? parseFloat(e.target.value) : undefined
                  )
                }
                min="0"
                placeholder="e.g., 100"
              />
            </label>
            <label>
              Item Type:
              <select
                value={productType}
                onChange={e => setProductType(e.target.value)}
              >
                <option value="">Select Item Type</option>
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
                <option value="">Sort by Delivery Type</option>
                <option value="DELIVERY">Delivery</option>
                <option value="PICKUP">Pickup</option>
                <option value="DELIVERY_AND_PICKUP">Delivery & Pickup</option>
                <option value="NO_DELIVERY_OPTION">No Delivery Option</option>
              </select>
            </label>
            <div className="star-rating-container">
              <h2>Filter by Star Rating</h2>
              <div className="star-row">
                <label>Min Stars:</label>
                <StarRating
                  currentRating={minStars}
                  viewOnly={false}
                  updateRating={rating => {
                    setMinStars(rating);
                    validationStars(rating, maxStars);
                  }}
                />
              </div>
              <div className="star-row">
                <label>Max Stars:</label>
                <StarRating
                  currentRating={maxStars}
                  viewOnly={false}
                  updateRating={rating => {
                    setMaxStars(rating);
                    validationStars(minStars, rating);
                  }}
                />
              </div>
              {validationMessage && (
                <div style={{ color: 'red' }}>{validationMessage}</div>
              )}
            </div>
            <select
              name="rating"
              value={ratingSort}
              onChange={e => setRatingSort(e.target.value)}
            >
              <option value="default">Sort by Rating</option>
              <option value="asc">Low to High</option>
              <option value="desc">High to Low</option>
            </select>
            <button
              className="apply-filter-button"
              onClick={() => setIsSidebarOpen(false)}
            >
              Apply
            </button>
            <button className="clear-filter-button" onClick={clearFilters}>
              Clear
            </button>
          </div>
        </div>
      )}

      <ProductsList
        view="catalog"
        filters={{
          minPrice,
          maxPrice,
          ratingSort,
          minStars,
          maxStars,
          deliveryType,
          productType,
        }}
      />

      <div className="block">
        <hr />
      </div>

      <div className="trending-list-container-gold">
        <h2 className="section-header">Trending</h2>
        <TrendingList />
      </div>

      <div className="block">
        <hr />
      </div>

      <ProductsList view="extras" />
    </div>
  );
}
