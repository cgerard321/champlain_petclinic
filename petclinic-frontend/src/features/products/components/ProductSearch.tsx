import './ProductSearch.css';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import { getAllProducts } from '@/features/products/api/getAllProducts';
import { useEffect, useState, JSX } from 'react';

interface ProductSearchProps {
  searchQuery: string;
  setSearchQuery: (value: string) => void;
}

export default function ProductSearch({
  searchQuery,
  setSearchQuery,
}: ProductSearchProps): JSX.Element {
  const [products, setProducts] = useState<ProductModel[]>([]);
  const [filteredProducts, setFilteredProducts] = useState<ProductModel[]>([]);
  const [selectedProduct, setSelectedProduct] = useState<ProductModel | null>(
    null
  );

  const fetchProducts = async (): Promise<void> => {
    const allProducts = await getAllProducts();
    setProducts(allProducts.filter(p => !p.isUnlisted));
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  useEffect(() => {
    if (!searchQuery) {
      setFilteredProducts([]);
    } else {
      setFilteredProducts(
        products.filter(product =>
          product.productName.toLowerCase().includes(searchQuery.toLowerCase())
        )
      );
    }
  }, [searchQuery, products]);

  // eslint-disable-next-line @typescript-eslint/explicit-function-return-type
  const handleProductClick = (product: ProductModel) => {
    setSelectedProduct(product);
    setSearchQuery('');
    setFilteredProducts([]);
  };

  const handleCloseModal = (): void => setSelectedProduct(null);

  return (
    <div className="product-search">
      <input
        type="text"
        value={searchQuery}
        onChange={e => setSearchQuery(e.target.value)}
        placeholder="Search for a product..."
        className="search-input"
      />

      {searchQuery && (
        <div className="search-results">
          {filteredProducts.length > 0 ? (
            <ul>
              {filteredProducts.map(product => (
                <li
                  key={product.productId}
                  onClick={() => handleProductClick(product)}
                >
                  <span className="product-name">{product.productName}</span>
                  <span className="product-price">
                    ${product.productSalePrice.toFixed(2)}
                  </span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="no-products">No products found</p>
          )}
        </div>
      )}

      {selectedProduct && (
        <>
          <div
            className="product-details-overlay"
            onClick={handleCloseModal}
          ></div>
          <div className="product-details-modal">
            <div className="product-details">
              <h3>{selectedProduct.productName}</h3>
              <p>
                <strong>Description:</strong>{' '}
                {selectedProduct.productDescription}
              </p>
              <p>
                <strong>Price:</strong>{' '}
                <span className="product-price">
                  ${selectedProduct.productSalePrice.toFixed(2)}
                </span>
              </p>
              <p>
                <strong>Quantity:</strong> {selectedProduct.productQuantity}
              </p>
              <p>
                <strong>Status:</strong> {selectedProduct.productStatus}
              </p>
            </div>
            <button className="close-btn" onClick={handleCloseModal}>
              Close
            </button>
          </div>
        </>
      )}
    </div>
  );
}
