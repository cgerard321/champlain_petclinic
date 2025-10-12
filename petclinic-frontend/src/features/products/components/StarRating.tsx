import { JSX, useEffect, useState } from 'react';
import './StarRating.css';

interface RatingProps {
  currentRating: number;
  viewOnly: boolean;
  updateRating?: (rating: number, review: string | null) => void;
}

function StarRating({
  currentRating,
  viewOnly,
  updateRating,
}: RatingProps): JSX.Element {
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
    if (updateRating) updateRating(starIndex, null);
    renderStars(starIndex);
  };

  useEffect(() => {
    renderStars(currentRating);
  }, [currentRating]);

  return (
    <div className="star-container">
      {stars.map((isVisible: boolean, index: number) => {
        return (
          <div key={index} className="star-inner-container">
            <svg
              className={`empty-star star ${!viewOnly && 'star-hover star-pointer'}`}
              xmlns="http://www.w3.org/2000/svg"
              width="25"
              height="25"
              stroke="#222"
              strokeWidth="2"
              viewBox="0 0 32 32"
              fill="none"
              onClick={() => !viewOnly && starClick(index)}
            >
              <path
                d="M16 23.21l7.13 4.13-1.5-7.62a.9.9 0 0 1 .27-.83l5.64-5.29-7.64-.93a.9.9 0 0 1-.71-.52L16 5.1l-3.22 7a.9.9 0 0 1-.71.52l-7.6.93 5.63 5.29a.9.9 0 0 1 .27.83l-1.51 7.67zm0 2l-7.9 4.58a.9.9 0 0 1-1.34-.95l1.73-9-6.65-6.3A.9.9 0 0 1 2.36 12l9-1.08 3.81-8.32a.9.9 0 0 1 1.64 0l3.81 8.32 9 1.08a.9.9 0 0 1 .51 1.55l-6.66 6.3 1.68 9a.9.9 0 0 1-1.34.94z"
                fillRule="evenodd"
              ></path>
            </svg>
            <div
              className="partial-star"
              style={{
                width: isVisible
                  ? '100%'
                  : `${Math.floor(currentRating) + 1 == index ? Math.floor((currentRating % 1) * 100) : 0}%`,
              }}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="25"
                height="25"
                viewBox="0 0 32 32"
                fill="none"
                key={index}
                className={`star star-shown ${!viewOnly && 'star-hover star-pointer'}`}
                onClick={() => !viewOnly && starClick(index)}
              >
                <path
                  d="M16 25.19l-8.24 4.65a.9.9 0 0 1-1.33-1l1.8-9-6.86-6.26A.9.9 0 0 1 1.88 12l9.32-1.08 4-8.39a.9.9 0 0 1 1.63 0l4 8.39L30.12 12a.9.9 0 0 1 .5 1.56l-6.88 6.29 1.74 9a.9.9 0 0 1-1.33 1z"
                  fillRule="evenodd"
                ></path>
              </svg>
            </div>
          </div>
        );
      })}

      {currentRating != 0 && !viewOnly && (
        <div className="delete-button-rating" onClick={() => starClick(0)}>
          <span>-</span>
        </div>
      )}
    </div>
  );
}

export default StarRating;
