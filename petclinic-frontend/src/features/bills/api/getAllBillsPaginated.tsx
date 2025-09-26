import axiosInstance from '@/shared/api/axiosInstance';
import { Bill } from '../models/Bill';

export async function getAllBillsPaginated(
  currentPage: number,
  listSize: number
): Promise<Bill[]> {
  //const url = `bills?page=${currentPage}&size=${listSize}`;

  //const response = await axiosInstance.get<Bill[]>(
  //    axiosInstance.defaults.baseURL + url
  //  );

  const response = await axiosInstance.get<Bill[]>(
    `/bills?page=${currentPage}&size=${listSize}`
    //{ responseType: 'stream' }
  );
  return response.data;
  //.split('data:')
  //  .map((payLoad: string) => {
  //   try {
  //    if (payLoad == '') return null;
  //    return JSON.parse(payLoad);
  //   } catch (err) {
  //     console.error("Can't parse JSON: " + err);
  //   }
  //  })
  //.filter((data?: JSON) => data !== null);
}
