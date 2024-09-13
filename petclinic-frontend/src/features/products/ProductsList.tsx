import { useState, useEffect, JSX } from 'react';
import { getAllProducts } from '@/features/products/api/getAllProducts.ts';
import './ProductList.css';
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';

export default function ProductList(): JSX.Element {
    const [productList, setProductList] = useState<ProductModel[]>([]);

    const fetchProducts = async (): Promise<void> => {
            const products = await getAllProducts();
            setProductList(products);
    };



    useEffect(() => {
        fetchProducts();
    }, []);

    return (
        <div>
            <div className="grid">
                {productList.map((product) => (
                    <div className="card" key={product.productId}>
                        <h2>{product.productName}</h2>
                        <p>{product.productDescription}</p>
                        <p>Price: ${product.productSalePrice.toFixed(2)}</p>
                        <p>Reviews: </p>
                        <p>Quantity: </p>
                    </div>
                ))}
            </div>
        </div>
    );
}
