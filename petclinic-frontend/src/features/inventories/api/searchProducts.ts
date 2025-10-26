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
    const qs = new URLSearchParams();
    if (productName) qs.append('productName', productName);
    if (productDescription) qs.append('productDescription', productDescription);
    if (status) qs.append('status', status);

    const url = qs.toString()
      ? `/inventories/${inventoryId}/products/search?${qs.toString()}`
      : `/inventories/${inventoryId}/products/search`;

    const res = await axiosInstance.get<string>(url, {
      useV2: false,
      responseType: 'text',
      transformResponse: [(v: unknown) => String(v ?? '')],
    });

    const items = parseProductsStream(res.data);
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

const parseProductsStream = (rawIn: unknown): ProductModel[] => {
  const raw = String(rawIn ?? '').trim();
  if (!raw) return [];

  // Plain JSON fallback
  if (raw.startsWith('[') || raw.startsWith('{')) {
    try {
      const j = JSON.parse(raw);
      return Array.isArray(j) ? (j as ProductModel[]) : [j as ProductModel];
    } catch {
      return [];
    }
  }

  // SSE: split by blank lines, join "data:" lines, JSON.parse each block
  const items: ProductModel[] = [];
  raw.split(/\r?\n\r?\n/).forEach(block => {
    const jsonText = block
      .split(/\r?\n/)
      .filter(line => line.startsWith('data:'))
      .map(line => line.slice(5).trim())
      .join('\n')
      .trim();

    if (!jsonText || jsonText === 'heartbeat' || jsonText === '__END__') return;

    try {
      const v = JSON.parse(jsonText);
      if (Array.isArray(v)) items.push(...(v as ProductModel[]));
      else items.push(v as ProductModel);
    } catch {
      // ignore malformed chunk
    }
  });

  return items;
};
