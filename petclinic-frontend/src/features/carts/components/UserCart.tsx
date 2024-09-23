import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import CartItem from './CartItem';
import { ProductModel } from '../models/ProductModel';
import './UserCart.css';

interface CartResponseDTO {
  cartId: string;
  customerId: string;
  products: ProductModel[];
}

const UserCart = (): JSX.Element => {
  const { cartId } = useParams<{ cartId: string }>();
  const [cartItems, setCartItems] = useState<ProductModel[]>([]);
  const [fixedPrice, setFixedPrice] = useState<number[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true); // Loading state

  useEffect((): void => {
    const fetchCartItems = async (): Promise<void> => {
      try {
        // Use fetch API instead of axios
        const response = await fetch(`http://localhost:8080/api/v2/gateway/carts/${cartId}`, {
          headers: {
            Accept: 'application/json',
          },
          credentials: 'include', // Adjust this depending on your CORS and authentication needs
        });

        if (!response.ok) {
          throw new Error(`Error: ${response.status} ${response.statusText}`);
        }

        const data: CartResponseDTO = await response.json();
        const products = data.products.map((product) => ({
          ...product,
          quantity: 1, // Set a default quantity if it doesn't exist
        }));

        setCartItems(products);

        const initialPrices = products.map((item) => item.productSalePrice);
        setFixedPrice(initialPrices);
      } catch (err: any) {
        console.error('Error fetching cart items:', err);
        setError('Failed to fetch cart items');
      } finally {
        setLoading(false); // Stop loading once the fetch is done
      }
    };

    if (cartId) {
      fetchCartItems();
    } else {
      setError('Invalid cart ID');
      setLoading(false);
    }
  }, [cartId]);

  const changeItemQuantity = (event: React.ChangeEvent<HTMLInputElement>, index: number): void => {
    const newItems = [...cartItems];
    const newQuantity = +event.target.value;
    newItems[index].quantity = newQuantity;
    newItems[index].productSalePrice = fixedPrice[index] * newQuantity;
    setCartItems(newItems);
  };

  const deleteItem = (indexToDelete: number): void => {
    const newItems = cartItems.filter((_item, index) => index !== indexToDelete);
    setCartItems(newItems);
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>{error}</div>;
  }

  return (
      <div className="CartItems">
        <h1>User Cart</h1>
        <hr />
        <div className="CartItems-items">
          {cartItems.length > 0 ? (
              cartItems.map((item, index) => (
                  <CartItem
                      key={item.productId}
                      item={item}
                      index={index}
                      changeItemQuantity={changeItemQuantity}
                      deleteItem={deleteItem}
                  />
              ))
          ) : (
              <p>No products in the cart.</p>
          )}
        </div>
      </div>
  );
};

export default UserCart;
