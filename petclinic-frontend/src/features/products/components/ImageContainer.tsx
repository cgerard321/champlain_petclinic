import { useEffect, useState } from 'react';
import { getImage } from '../api/getImage';
import './Image.css';
import { ProductModel } from '../models/ProductModels/ProductModel';

interface ImageContainerProps {
  imageId?: string;
  imageUrl?: string;
  product?: ProductModel;
}

export default function ImageContainer({
  imageId,
  imageUrl,
}: ImageContainerProps): JSX.Element {
  const [imageName, setImageName] = useState<string | null>(null);
  const [imageType, setImageType] = useState<string | null>(null);
  const [imageData, setImageData] = useState<string | null>(null);

  useEffect(() => {
    async function loadImage(): Promise<void> {
      if (!imageId) return;

      try {
        const image = await getImage(imageId);
        setImageName(image.imageName);
        setImageType(image.imageType);
        setImageData(image.imageData);
      } catch (error) {
        console.error('Error loading image:', error);
        throw new Error('Error fetching image');
      }
    }
    loadImage();
  }, [imageId]);

  return (
    <div className="image-container">
      {imageUrl ? (
        <img src={imageUrl} alt={imageName || 'Product image'} />
      ) : imageData ? (
        <img
          src={`data:${imageType};base64,${imageData}`}
          alt={imageName || 'Product image'}
        />
      ) : (
        <p>No image available</p>
      )}
    </div>
  );
}
