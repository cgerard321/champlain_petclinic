import React, { useState, useEffect } from 'react';

interface SlideshowProps {
  images: string[];
  interval: number;
}

const Slideshow: React.FC<SlideshowProps> = ({ images, interval }) => {
  const [currentIndex, setCurrentIndex] = useState(0);

  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentIndex((currentIndex + 1) % images.length);
    }, interval);

    return () => {
      clearInterval(timer);
    };
  }, [currentIndex, images, interval]);

  return (
    <div className="slideshow-container">
      <img src={images[currentIndex]} alt="Pet Clinic Slideshow" />
    </div>
  );
};

export default Slideshow;
