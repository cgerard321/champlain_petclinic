import { useTranslation } from 'react-i18next';
import { useEffect, useRef, useState } from 'react';
import defaultProductImage from '@/assets/Products/defaultProductImage.svg';
import { FileDetails } from '@/shared/models/FileDetails';
import { getProduct } from '../api/getProduct';
import './Image.css';
import { ProductModel } from '../models/ProductModels/ProductModel';

type ImageLoadState = 'loading' | 'loaded' | 'error';

interface ImageContainerProps {
  image?: FileDetails | null;
  alt?: string;
  imageId?: string;
  productId?: string;
  imageUrl?: string;
  product?: ProductModel;
}

export default function ImageContainer({
  image,
  alt,
  imageId,
  productId,
  imageUrl,
}: ImageContainerProps): JSX.Element {
  const { t } = useTranslation('products');
  const [imageName, setImageName] = useState<string | null>(
    image?.fileName ?? null
  );
  const [imageType, setImageType] = useState<string | null>(
    image?.fileType ?? null
  );
  const [imageData, setImageData] = useState<string | null>(
    image?.fileData ?? null
  );
  const [loadState, setLoadState] = useState<ImageLoadState>('loading');
  const [isFallback, setIsFallback] = useState(false);
  const imageRef = useRef<HTMLImageElement>(null);

  useEffect(() => {
    let cancelled = false;

    setLoadState('loading');
    setIsFallback(false);
    if (!imageUrl && !image?.fileData) {
      setImageName(null);
      setImageType(null);
      setImageData(null);
    }

    async function loadImage(): Promise<void> {
      if (imageUrl) {
        if (!cancelled) {
          setImageName(null);
          setImageType(null);
          setImageData(null);
        }
        return;
      }

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
            if (!currentImage?.fileData) setLoadState('error');
          }
        } catch (error) {
          console.error('Error loading product image:', error);
          if (!cancelled) {
            setImageName(null);
            setImageType(null);
            setImageData(null);
            setLoadState('error');
          }
        }
        return;
      }

      if (!cancelled) setLoadState('error');
    }

    loadImage();

    return () => {
      cancelled = true;
    };
  }, [image?.fileData, image?.fileType, image?.fileName, imageUrl, productId]);

  const imageSource = imageUrl
    ? imageUrl
    : imageData
      ? `data:${imageType || 'image/png'};base64,${imageData}`
      : null;
  const isFetching =
    loadState === 'loading' &&
    !imageUrl &&
    !image?.fileData &&
    Boolean(productId);
  const showingFallback =
    isFallback || loadState === 'error' || (!imageSource && !isFetching);

  useEffect(() => {
    if (showingFallback || !imageSource) return;

    const imageElement = imageRef.current;
    if (imageElement?.complete && imageElement.naturalWidth > 0) {
      setLoadState('loaded');
    }
  }, [imageSource, showingFallback]);

  const handleImageLoad = (): void => {
    if (!showingFallback && imageSource) setLoadState('loaded');
  };

  const handleImageError = (): void => {
    if (!isFallback) {
      setIsFallback(true);
      setLoadState('error');
    }
  };

  return (
    <div className="image-container">
      {loadState === 'loading' && !showingFallback && (
        <span
          className="image-loading-spinner"
          role="status"
          aria-label={t('loading')}
        />
      )}
      <img
        ref={imageRef}
        src={
          showingFallback
            ? defaultProductImage
            : imageSource || defaultProductImage
        }
        alt={
          showingFallback
            ? alt
              ? `${alt} (image unavailable)`
              : 'Default product image'
            : alt || imageName || 'Product image'
        }
        className={
          loadState === 'loading' && !showingFallback
            ? 'image-loading'
            : undefined
        }
        loading="lazy"
        decoding="async"
        onLoad={handleImageLoad}
        onError={handleImageError}
      />
    </div>
  );
}
