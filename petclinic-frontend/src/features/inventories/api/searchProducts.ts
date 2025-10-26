import { ProductModel } from '@/features/inventories/models/ProductModels/ProductModel.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Status } from '@/features/inventories/models/ProductModels/Status.ts';
import { ApiResponse } from '@/shared/models/ApiResponse';

export async function searchProducts(
  inventoryId: string,
  productName?: string,
  productDescription?: string,
  status?: Status
): Promise<ApiResponse<ProductModel[]>> {
  try {
    const queryParams = new URLSearchParams();
    if (productName) queryParams.append('productName', productName);
    if (productDescription)
      queryParams.append('productDescription', productDescription);
    if (status) queryParams.append('status', status);

    const queryString = queryParams.toString();
    const url = queryString
      ? `/inventories/${inventoryId}/products/search?${queryString}`
      : `/inventories/${inventoryId}/products/search`;

    const response = await axiosInstance.get(url, {
      useV2: false,
      responseType: 'text',
    });

    const raw = String(response.data ?? '');
    if (!raw.trim()) {
      return { data: [], errorMessage: null };
    }

    const items: ProductModel[] = raw
      .split(/\r?\n\r?\n/)
      .map(block => {
        const dataLines = block
          .split(/\r?\n/)
          .filter(line => line.startsWith('data:'))
          .map(line => line.slice(5).trim());

        if (dataLines.length === 0) return null;

        const jsonText = dataLines.join('\n').trim();
        if (!jsonText || jsonText === '__END__') return null;

        try {
          const product = JSON.parse(jsonText) as ProductModel;
          product.productMargin = parseFloat(
            (product.productSalePrice - product.productPrice).toFixed(2)
          );
          return product;
        } catch (e) {
          console.error("Can't parse JSON from SSE event:", e, jsonText);
          return null;
        }
      })
      .filter((x): x is ProductModel => x !== null);

    return { data: items, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      ?.response?.data?.message;
    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Failed to load products. Please try again.';
    return { data: null, errorMessage };
  }
}
