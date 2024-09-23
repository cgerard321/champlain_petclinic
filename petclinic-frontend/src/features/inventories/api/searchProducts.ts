import axiosInstance from '@/shared/api/axiosInstance.ts';
import {ProductModel} from "@/features/inventories/models/ProductModels/ProductModel.ts";

export async function searchProducts(
    inventoryId: string,
    productName?: string,
    productDescription?: string,
): Promise<ProductModel[]> {
    const queryParams = new URLSearchParams();
    queryParams.append('inventoryId', inventoryId);
    if (productName) queryParams.append('productName', productName);
    if (productDescription)
        queryParams.append('productDescription', productDescription);

    const queryString = queryParams.toString();
    const url = queryString
        ? `/${inventoryId}/products/search?&${queryString}`
        : `/${inventoryId}/products/search`;

    const response = await axiosInstance.get<ProductModel[]>(
        axiosInstance.defaults.baseURL + url
    );
    return response.data;
}
