import './ProductSearch.css';

interface ProductSearchProps {
  searchQuery: string;
  setSearchQuery: (value: string) => void;
}

export default function ProductSearch({
  searchQuery,
  setSearchQuery,
}: ProductSearchProps): JSX.Element {
  return (
    <div className="product-search">
      <input
        type="text"
        value={searchQuery}
        onChange={e => setSearchQuery(e.target.value)}
        placeholder="Search for a product..."
        className="search-input"
      />
    </div>
  );
}
