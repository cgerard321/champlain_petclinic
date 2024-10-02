import { ProductModel } from "@/features/products/models/ProductModels/ProductModel.ts";
import { NavBar } from '@/layouts/AppNavBar.tsx';
import { JSX } from 'react';
import { useLocation } from 'react-router-dom';

export default function ProductDetails(): JSX.Element {
    const location = useLocation();
    const { product } = location.state as { product: ProductModel };

    return (
        <>
            <NavBar />
            <h1>{product.productName}</h1>
            <p>{product.productDescription}</p>
            <p>Price: ${product.productSalePrice.toFixed(2)}</p>
        </>
    );
}
