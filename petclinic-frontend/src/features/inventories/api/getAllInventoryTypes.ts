import axiosInstance from '@/shared/api/axiosInstance.ts';
import { InventoryType } from '@/features/inventories/models/InventoryType.ts';
import { ApiResponse } from '@/shared/models/ApiResponse.ts';

export async function getAllInventoryTypes(): Promise<
  ApiResponse<InventoryType[]>
> {
  try {
    const response = await axiosInstance.get('/inventories/types', {
      useV2: false,
      responseType: 'text',
    });
    const raw = String(response.data ?? '');

    const items: InventoryType[] = raw
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
          return JSON.parse(jsonText) as InventoryType;
        } catch (e) {
          console.error("Can't parse JSON from SSE event:", e, jsonText);
          return null;
        }
      })
      .filter((x): x is InventoryType => x !== null);

    return { data: items, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      .response?.data?.message;

    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Unable to add inventory. Please check your information and try again.';

    return { data: null, errorMessage };
  }
}
