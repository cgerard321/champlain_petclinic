import { useState } from 'react';
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';
import { searchProducts } from '@/features/inventories/api/searchProducts.ts';
import { Status } from '@/features/inventories/models/ProductModels/Status.ts';

interface useSearchProductsResponseModel {
  productList: ProductModel[];
  setProductList: (productList: ProductModel[]) => void;
  getProductList: (
    inventoryId: string,
    productName?: string,
    productDescription?: string,
    status?: Status
  ) => void;
}

export default function useSearchProducts(): useSearchProductsResponseModel {
  const [productList, setProductList] = useState<ProductModel[]>([]);

  const getProductList = async (
    inventoryId: string,
    productName?: string,
    productDescription?: string,
    status?: Status
  ): Promise<void> => {
    const res = await searchProducts(
      inventoryId,
      productName,
      productDescription,
      status
    );

    if (res.errorMessage) {
      setProductList([]);
      return;
    }

    setProductList(res.data ?? []);
  };

  return {
    productList,
    setProductList,
    getProductList,
  };
}
