import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import CartItem from './CartItem.tsx';
import { ProductModel } from '../models/ProductModel';
import './UserCart.css';

const UserCart = () => {
    const { cartId } = useParams<{ cartId: string }>();
    const [cartItems, setCartItems] = useState<ProductModel[]>([]);
    const [fixedPrice, setFixedPrice] = useState<number[]>([]);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchCartItems = async () => {
            try {
                const response = await axios.get(`/api/v2/gateway/carts/${cartId}`);
                const products = response.data.products; // Assuming 'products' is the field containing cart items
                setCartItems(products);

                // Store initial fixed prices for each item
                const initialPrices = products.map((item: ProductModel) => item.productSalePrice);
                setFixedPrice(initialPrices);
            } catch (error) {
                console.error('Error fetching cart items:', error);
                setError('Failed to fetch cart items');
            }
        };

        fetchCartItems();
    }, [cartId]);

    const changeItemQuantity = (
        event: React.ChangeEvent<HTMLInputElement>,
        index: number,
    ) => {
        const newItems = [...cartItems];
        const newQuantity = +event.target.value;
        newItems[index].quantity = newQuantity;
        newItems[index].productSalePrice = fixedPrice[index] * newQuantity;
        setCartItems(newItems);
    };

    const deleteItem = (indexToDelete: number) => {
        const newItems = cartItems.filter((_item, index) => index !== indexToDelete);
        setCartItems(newItems);
    };

    if (error) {
        return <div>{error}</div>;
    }

    return (
        <div className="CartItems">
            <h1>User Cart</h1>
            <hr />
            <div className="CartItems-items">
                {cartItems.map((item, index) => (
                    <CartItem
                        key={index}
                        index={index}
                        item={item}
                        changeItemQuantity={changeItemQuantity}
                        deleteItem={deleteItem}
                    />
                ))}
            </div>
        </div>
    );
};

export default UserCart;
