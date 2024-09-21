import React from 'react';
import { ProductModel } from '../models/ProductModel';
import './CartItem.css';

type CartItemProps = {
    index: number;
    item: ProductModel;
    deleteItem: (index: number) => void;
    changeItemQuantity: (
        event: React.ChangeEvent<HTMLInputElement>,
        index: number
    ) => void;
};

const formatPrice = (price: number): string => {
    return `$${price.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')}`;
};

const CartItem = ({ index, item, changeItemQuantity, deleteItem }: CartItemProps) => {
    const { productName, productDescription, productSalePrice, averageRating, quantity } = item;

    return (
        <div className="CartItem">
            <div className="CartItem-info">
                <div className="info-title">
                    <h2>{productName}</h2>
                </div>
                <div className="info-description">
                    <p>{productDescription}</p>
                </div>
                <div className="info-rating">
                    <p>Average Rating: {averageRating} / 5</p>
                </div>

                <div className="item-actions">
                    <div className="item-quantity">
                        <label htmlFor={`quantity-${index}`}>Quantity: </label>
                        <input
                            type="number"
                            id={`quantity-${index}`}
                            min="1"
                            value={quantity}
                            onChange={(event) => changeItemQuantity(event, index)}
                            pattern="\d*" //prevents non-numeric input
                        />
                    </div>
                    <div className="item-actions-divider">|</div>
                    <div className="item-delete" onClick={() => deleteItem(index)}>
                        <button className="delete-button">Delete</button>
                    </div>
                </div>
            </div>
            <div className="CartItem-price">
                {formatPrice(productSalePrice)}
            </div>
        </div>
    );
};

export default CartItem;
