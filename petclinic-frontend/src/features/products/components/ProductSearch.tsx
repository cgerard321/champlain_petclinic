import { useState, useEffect } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts';
import { ProductModel } from '@/features/products/models/ProductModels/ProductModel';
import './ProductSearch.css';

export default function ProductSearch(): JSX.Element {
  const [searchQuery, setSearchQuery] = useState('');
  const [products, setProducts] = useState<ProductModel[]>([]);
  const [filteredProducts, setFilteredProducts] = useState<ProductModel[]>([]);
  const [selectedProduct, setSelectedProduct] = useState<ProductModel | null>(
    null
  );

  const fetchProducts = async (): Promise<void> => {
    const allProducts = await getAllProducts();
    const listedProducts = allProducts.filter(product => !product.isUnlisted);
    setProducts(listedProducts);
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  useEffect(() => {
    if (searchQuery === '') {
      setFilteredProducts([]);
    } else {
      const filtered = products.filter(product =>
        product.productName.toLowerCase().includes(searchQuery.toLowerCase())
      );
      setFilteredProducts(filtered);
    }
  }, [searchQuery, products]);

  const handleInputChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ): void => {
    setSearchQuery(event.target.value);
    setSelectedProduct(null);
  };

  const handleProductClick = (product: ProductModel): void => {
    setSelectedProduct(product);
    setSearchQuery('');
    setFilteredProducts([]);
  };

  const handleCloseModal = (): void => {
    setSelectedProduct(null);
  };

  return (
    <div className="product-search">
      <form onSubmit={e => e.preventDefault()}>
        <input
          type="text"
          value={searchQuery}
          onChange={handleInputChange}
          placeholder="Search for a product..."
          className="search-input"
        />
      </form>
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
                <strong>Price:</strong> $
                {selectedProduct.productSalePrice.toFixed(2)}
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
