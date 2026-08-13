import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Inventory } from '@/features/inventories/models/Inventory.ts';
import { ApiResponse } from '@/shared/models/ApiResponse';

export async function searchInventories(
  currentPage: number,
  listSize: number,
  inventoryName?: string,
  inventoryType?: string,
  inventoryDescription?: string,
  importantOnly?: boolean
): Promise<ApiResponse<Inventory[]>> {
  try {
    const queryParams = new URLSearchParams();
    if (inventoryName) queryParams.append('inventoryName', inventoryName);
    if (inventoryType) queryParams.append('inventoryType', inventoryType);
    if (inventoryDescription)
      queryParams.append('inventoryDescription', inventoryDescription);
    if (importantOnly) queryParams.append('importantOnly', 'true');

    const queryString = queryParams.toString();
    const url = queryString
      ? `/inventories?page=${currentPage}&size=${listSize}&${queryString}`
      : `/inventories?page=${currentPage}&size=${listSize}`;

    const response = await axiosInstance.get<Inventory[]>(url, {
      useV2: false,
      responseType: 'text',
    });
    const raw = String(response.data ?? '');

    const items: Inventory[] = raw
      .split(/\r?\n\r?\n/) // split SSE events
      .map(block => {
        // collapse multiple data: lines in the same event
        const dataLines = block
          .split(/\r?\n/)
          .filter(line => line.startsWith('data:'))
          .map(line => line.slice(5).trim());

        if (dataLines.length === 0) return null;

        const jsonText = dataLines.join('\n').trim();
        if (!jsonText || jsonText === '__END__') return null; // optional sentinel support

        try {
          const parsed = JSON.parse(jsonText);
          if (Array.isArray(parsed)) {
            // when the server sends an array as one event, return a marker object; we'll expand below
            return parsed as Inventory[];
          }
          return parsed as Inventory;
        } catch (e) {
          console.error("Can't parse JSON from SSE event:", e, jsonText);
          return null;
        }
      })
      .filter((x): x is Inventory | Inventory[] => x !== null)
      // flatten optional array payloads while preserving order
      .reduce<Inventory[]>((acc, chunk) => {
        if (Array.isArray(chunk)) acc.push(...chunk);
        else acc.push(chunk);
        return acc;
      }, []);

    return { data: items, errorMessage: null };
  } catch (error: unknown) {
    const maybeMsg = (error as { response?: { data?: { message?: unknown } } })
      ?.response?.data?.message;
    const errorMessage =
      typeof maybeMsg === 'string' && maybeMsg.trim()
        ? maybeMsg.trim()
        : 'Failed to load inventories. Please try again.';
    return { data: null, errorMessage };
  }
}
