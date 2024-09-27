import { useState } from 'react';
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';
import {searchProducts} from "@/features/inventories/api/searchProducts.ts";

interface useSearchProductsResponseModel {
    productList: ProductModel[];
    setProductList: (productList: ProductModel[]) => void;
    getProductList: (
        inventoryId: string,
        productName?: string,
        productDescription?: string
    ) => void;
}

export default function useSearchProducts(): useSearchProductsResponseModel {
    const [productList, setProductList] = useState<ProductModel[]>([]);

    const getProductList = async (
        inventoryId: string,
        productName?: string,
        productDescription?: string
    ): Promise<void> => {
        const data = await searchProducts(inventoryId, productName, productDescription);
        setProductList(data);
    };

    return {
        productList,
        setProductList,
        getProductList,
    };
}
