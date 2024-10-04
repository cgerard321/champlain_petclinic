import axiosInstance from '@/shared/api/axiosInstance.ts';
import { Bill } from '@/features/bills/models/Bill.ts';

export async function getAllPaidBills(): Promise<Bill[]> {
    const response = await axiosInstance.get('' + '/bills/admin/paid', {
        responseType: 'stream',
    });
    return response.data
        .split('data:')
        .map((payLoad: string) => {
            try {
                if (payLoad == '') return null;
                return JSON.parse(payLoad);
            } catch (err) {
                console.error("Can't parse JSON: " + err);
            }
        })
        .filter((data?: JSON) => data !== null);
}
