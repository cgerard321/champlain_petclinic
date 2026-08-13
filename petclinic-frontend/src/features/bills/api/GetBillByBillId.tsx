import { Bill } from '@/features/bills/models/Bill.ts';
import axiosInstance from '@/shared/api/axiosInstance.ts';
import axios, { AxiosResponse, AxiosError } from 'axios';
export async function getBillByBillId(billId: string): Promise<Bill | null> {
  const candidatePaths = [`/bills/admin/${billId}`, `/bills/${billId}`];
  const useV2Options = [false, true];

  for (const useV2 of useV2Options) {
    for (const path of candidatePaths) {
      try {
        const resp: AxiosResponse<unknown> = await axiosInstance.get(path, {
          responseType: 'text',
          useV2,
        });

        const { status, data } = resp;

        if (status === 200 && data != null) {
          if (typeof data === 'string') {
            try {
              return JSON.parse(data) as Bill;
            } catch {
              return null;
            }
          }
          return data as Bill;
        }
      } catch (err: unknown) {
        let status: number | undefined;
        if (axios.isAxiosError(err)) {
          const axiosErr = err as AxiosError;
          status = axiosErr.response?.status;
        }
        if (status === 404) {
          continue;
        }
        console.error(
          `getBillByBillId request error for ${path} (useV2=${useV2}):`,
          err
        );
        continue;
      }
    }
  }
  try {
    const env =
      (import.meta.env as Record<string, string | undefined> | undefined) ||
      undefined;
    const base = env?.VITE_BACKEND_URL;
    if (base) {
      const url = `${base.replace(/\/$/, '')}/bills/admin/${billId}`;
      try {
        const resp: AxiosResponse<unknown> = await axiosInstance.get(url, {
          responseType: 'text',
        });
        if (resp?.status === 200 && resp.data != null) {
          const d = resp.data;
          if (typeof d === 'string') {
            try {
              return JSON.parse(d) as Bill;
            } catch (_) {
              return null;
            }
          }
          return d as Bill;
        }
      } catch (e) {
        console.error('getBillByBillId fallback absolute URL error:', e);
      }
    }
  } catch (e) {}

  return null;
}
