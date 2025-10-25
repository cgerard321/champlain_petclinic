import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryResponseModel } from '@/features/inventories/models/InventoryModels/InventoryResponseModel.ts';
import { ApiResponse } from '@/shared/models/ApiResponse.ts';

export const getAllInventories = async (): Promise<
  ApiResponse<InventoryResponseModel[]>
> => {
  try {
    const response = await axiosInstance.get<InventoryResponseModel[]>(
      '/inventories',
      {
        useV2: false,
        responseType: 'text',
        headers: {
          Accept: 'text/event-stream',
        },
      }
    );
    const raw = String(response.data ?? '');

    const items: InventoryResponseModel[] = raw
      .split(/\r?\n\r?\n/)
      .map(block => {
        const dataLines = block
          .split(/\r?\n/)
          .filter(line => line.startsWith('data:'))
          .map(line => line.slice(5).trim());

        if (dataLines.length === 0) return null;

        const jsonText = dataLines.join('\n').trim();
        if (!jsonText || jsonText === '__END__') return null; // optional sentinel support

        try {
          return JSON.parse(jsonText) as InventoryResponseModel;
        } catch (e) {
          console.error("Can't parse JSON from SSE event:", e, jsonText);
          return null;
        }
      })
      .filter((x): x is InventoryResponseModel => x !== null);

    return { data: items, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      .response?.data?.message;

    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Unable to fetch inventories. Please try again.';

    return { data: null, errorMessage };
  }
};

export const updateProductInventoryId = async (
  currentInventoryId: string,
  productId: string,
  newInventoryId: string
): Promise<ApiResponse<void>> => {
  try {
    await axiosInstance.put<void>(
      `/inventories/${currentInventoryId}/products/${productId}/updateInventoryId/${newInventoryId}`,
      undefined,
      { useV2: false }
    );
    return { data: undefined, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      .response?.data?.message;

    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Unable to move product to the selected inventory. Please try again.';

    return { data: null, errorMessage };
  }
};
