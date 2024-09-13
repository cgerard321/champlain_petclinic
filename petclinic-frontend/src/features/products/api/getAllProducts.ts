import axiosInstance from '@/shared/api/axiosInstance.ts';
import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';

export async function getAllProducts(): Promise<ProductModel[]> {
        const response = await axiosInstance.get<ProductModel[]>(
            '/products/display'
        );
        return response.data;

}
