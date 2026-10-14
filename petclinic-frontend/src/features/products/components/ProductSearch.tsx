import { useTranslation } from 'react-i18next';
import './ProductSearch.css';

interface ProductSearchProps {
  searchQuery: string;
  setSearchQuery: (value: string) => void;
}

export default function ProductSearch({
  searchQuery,
  setSearchQuery,
}: ProductSearchProps): JSX.Element {
  const { t } = useTranslation('products');
  return (
    <div className="product-search">
      <input
        type="text"
        value={searchQuery}
        onChange={e => setSearchQuery(e.target.value)}
        placeholder={t('search')}
        className="search-input"
      />
    </div>
  );
}
