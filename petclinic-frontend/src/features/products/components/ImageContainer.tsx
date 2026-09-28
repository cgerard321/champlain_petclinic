import { useEffect, useState } from 'react';
import { FileDetails } from '@/shared/models/FileDetails';
import { getImage } from '../api/getImage';
import './Image.css';
import { ProductModel } from '../models/ProductModels/ProductModel';

interface ImageContainerProps {
  image?: FileDetails | null;
  imageId?: string;
  imageUrl?: string;
  product?: ProductModel;
}

export default function ImageContainer({
  image,
  imageId,
  imageUrl,
}: ImageContainerProps): JSX.Element {
  const [imageName, setImageName] = useState<string | null>(
    image?.fileName ?? null
  );
  const [imageType, setImageType] = useState<string | null>(
    image?.fileType ?? null
  );
  const [imageData, setImageData] = useState<string | null>(
    image?.fileData ?? null
  );

  useEffect(() => {
    async function loadImage(): Promise<void> {
      if (image?.fileData) {
        setImageName(image.fileName);
        setImageType(image.fileType);
        setImageData(image.fileData);
        return;
      }

      if (!imageId) {
        setImageName(null);
        setImageType(null);
        setImageData(null);
        return;
      }

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
  }, [image, imageId]);

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
