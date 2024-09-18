import { JSX, useEffect, useState } from 'react';
import './StarRating.css';

interface RatingProps {
  currentRating: number;
  updateRating: (rating: number) => void;
}

function StarRating({ currentRating, updateRating }: RatingProps): JSX.Element {
  const [stars, setStars] = useState([false, false, false, false, false]);

  const renderStars = (stopIndex: number): void => {
    const newStars = [];
    for (let i = 1; i <= 5; i++) {
      if (i <= stopIndex) {
        newStars[i] = true;
      } else {
        newStars[i] = false;
      }
    }
    setStars(newStars);
  };

  const starClick = (starIndex: number): void => {
    updateRating(starIndex);
    renderStars(starIndex);
  };

  useEffect(() => {
    renderStars(currentRating);
  }, [currentRating]);

  return (
    <div className="star-container">
      {stars.map((isVisible: boolean, index: number) => {
        return (
          // SVGs are weird, as you need to declare the SVG path inside HTML if you want CSS to work.
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="24"
            height="24"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
            key={index}
            className={`star ${isVisible ? 'shown' : ''}`}
            onClick={() => starClick(index)}
          >
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon>
          </svg>
        );
      })}
      {currentRating != 0 && (
        <div className="delete-button" onClick={() => starClick(0)}>
          <span>-</span>
        </div>
      )}
    </div>
  );
}

export default StarRating;
