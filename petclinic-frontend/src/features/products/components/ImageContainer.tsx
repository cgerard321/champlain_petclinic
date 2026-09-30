import { useEffect, useState } from 'react';
import { FileDetails } from '@/shared/models/FileDetails';
import { getImage } from '../api/getImage';
import { getProduct } from '../api/getProduct';
import './Image.css';
import { ProductModel } from '../models/ProductModels/ProductModel';

interface ImageContainerProps {
  image?: FileDetails | null;
  imageId?: string;
  productId?: string;
  imageUrl?: string;
  product?: ProductModel;
}

export default function ImageContainer({
  image,
  imageId,
  productId,
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
    let cancelled = false;

    async function loadImage(): Promise<void> {
      if (image?.fileData) {
        if (!cancelled) {
          setImageName(image.fileName);
          setImageType(image.fileType);
          setImageData(image.fileData);
        }
        return;
      }

      if (productId) {
        try {
          const product = await getProduct(productId);
          const currentImage = product.image;

          if (!cancelled) {
            setImageName(currentImage?.fileName ?? null);
            setImageType(currentImage?.fileType ?? null);
            setImageData(currentImage?.fileData ?? null);
          }
        } catch (error) {
          console.error('Error loading product image:', error);
          if (!cancelled) {
            setImageName(null);
            setImageType(null);
            setImageData(null);
          }
        }
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
        if (!cancelled) {
          setImageName(null);
          setImageType(null);
          setImageData(null);
        }
      }
    }
    loadImage();

    return () => {
      cancelled = true;
    };
  }, [image, imageId, productId]);

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
