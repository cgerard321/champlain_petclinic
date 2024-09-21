import { useEffect, useState, FC } from 'react';

interface SlideshowProps {
  images: string[];
  interval: number;
}

const Slideshow: FC<SlideshowProps> = ({ images, interval }) => {
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
