import { useEffect, useState } from 'react';
import { getImage } from '../api/getImage';

interface ImageContainerProps {
  imageId: string;
}

export default function ImageContainer({
  imageId,
}: ImageContainerProps): JSX.Element {
  const [imageName, setImageName] = useState<string | null>(null);
  const [imageType, setImageType] = useState<string | null>(null);
  const [imageData, setImageData] = useState<string | null>(null);

  useEffect(() => {
    async function loadImage(): Promise<void> {
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
    <div>
      {imageData && (
        <img
          src={`data:${imageType};base64,${imageData}`}
          alt={`${imageName}`}
        />
      )}
    </div>
  );
}